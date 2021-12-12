package com.bikpicture.app.scoring;

import com.bikpicture.app.bik.AddressDto;
import com.bikpicture.app.bik.BikApi;
import com.bikpicture.app.googlemaps.PlacesApi;
import com.bikpicture.app.googlemaps.ResponseDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = "/scoring")
@RequiredArgsConstructor
@Slf4j
public class ScoringController {
    private final PlacesApi placesApi;
    private final BikApi bikApi;

    private final static int MAX_PLACES = 60;
    private final static String CITY = "Łódź";

    private final static String BIK_KEY = "36b99ed9ad5743a5a62be4a2bb5eb465";

    @GetMapping(path = "/location/personal")
    public ResponseEntity getScoreForPersonalLocation(
            @RequestParam String streetName,
            @RequestParam String streetNumber,
            @RequestParam(required = false, defaultValue = "100") int radius,
            @RequestParam(required = false) List<String> keywords
    ) throws InterruptedException {
        ResponseDto addressDetails = placesApi.getAddressDetails(String.format("%s %s %s", streetName, streetNumber, CITY));
        Map<String, Map> geometry = (Map) addressDetails.getResults().get(0).get("geometry");
        Map location = geometry.get("location");
        double latitude = (double) location.get("lat");
        double longitude =(double) location.get("lng");
        int resultSize = 0;
        String pageToken = null;
        String lastId = null;
        List<Integer> numbersOfPlaces = new ArrayList<>();
        PersonalLocationResponseDto response = new PersonalLocationResponseDto();
        response.setCoordinates(new ArrayList<>());
        int timeout = PlacesApi.TIMEOUT;
        for (int i = 0; i < keywords.size(); i++) {
            String keyword = keywords.get(i);
            int numberOfPlaces = 0;
            do {
                ResponseDto nearbyPlaces;
                if(pageToken == null) {
                    nearbyPlaces = placesApi.findNearbyPlaces(String.format("%s,%s", latitude, longitude), radius, null, keyword);
                } else {
                    nearbyPlaces = placesApi.findNearbyPlaces(pageToken);
                }
                if (nearbyPlaces.getResults().size() == 0 ||
                        nearbyPlaces.getResults().get(0).get("place_id").equals(lastId)) {
                    timeout /= 2;
                    log.warn(String.format("Next token is not ready yet, waiting additional %s ms", timeout));
                    TimeUnit.MILLISECONDS.sleep(timeout);
                    continue;
                } else {
                    log.info(String.format("Got %s results", nearbyPlaces.getResults().size()));
                    log.info(String.format("%s...", nearbyPlaces.getResults().get(0).get("name")));
                }
                timeout = PlacesApi.TIMEOUT;
                lastId = (String) nearbyPlaces.getResults().get(0).get("place_id");
                pageToken = nearbyPlaces.getNext_page_token();
                resultSize = nearbyPlaces.getResults().size();
                numberOfPlaces += resultSize;
                List<PersonalLocationResponseDto.Coordinates> places = nearbyPlaces.getResults().stream().map(result -> {
                    Map<String, Map> geometry2 = (Map) result.get("geometry");
                    Map location2 = geometry2.get("location");
                    return PersonalLocationResponseDto.Coordinates.builder()
                            .latitude((Double) location2.get("lat"))
                            .longitude((Double) location2.get("lng"))
                            .keyword(keyword)
                            .name((String) result.get("name"))
                            .build();
                }).collect(Collectors.toList());
                response.getCoordinates().addAll(places);
                if (resultSize >= PlacesApi.PAGE_SIZE) {
                    TimeUnit.MILLISECONDS.sleep(timeout);
                }
            } while (resultSize == PlacesApi.PAGE_SIZE && numberOfPlaces < MAX_PLACES);
            numbersOfPlaces.add(numberOfPlaces);
        }

        Map<Integer, Double> numbersWithWeights = new HashMap<>();
        for (int i = 0; i < numbersOfPlaces.size(); i++) {
            double value = numbersOfPlaces.get(i);
            int weight = (numbersOfPlaces.size() - i);
            numbersWithWeights.put(weight, value);
        }
        /*
        double score = numbersOfPlaces.stream()
                .mapToInt(Integer::intValue)
                .average()
                .getAsDouble();
        */
        double score = calculateWeightedAverage(numbersWithWeights);
        response.setScore(score);
        response.setCoordinates(response.getCoordinates()
                .stream()
                .sorted(Comparator.comparing(PersonalLocationResponseDto.Coordinates::getName))
                .collect(Collectors.toList()));
        return ResponseEntity.ok(response);
    }

    static double calculateWeightedAverage(Map<Integer, Double> map) {
        double num = 0;
        double denom = 0;
        for (Map.Entry<Integer, Double> entry : map.entrySet()) {
            num += entry.getValue() * entry.getKey();
            denom += entry.getKey();
        }
        return num / denom;
    }

    @GetMapping(path = "/location/business")
    public ResponseEntity getScoreForBusinessLocation(
            @RequestParam String streetName,
            @RequestParam String streetNumber
    ) throws IOException {
        ResponseDto responseDto = placesApi.getAddressDetails(String.format("%s %s %s", streetName, streetNumber, CITY));
        String postalCode = responseDto.getResults()
                .get(0)
                .get("formatted_address")
                .toString()
                .split(",")[1]
                .split(" ")[1]
                .replace("-", "");
        AddressDto addressDto = AddressDto.builder()
                .city(CITY)
                .street(streetName)
                .building_number(String.valueOf(streetNumber))
                .code(postalCode)
                .build();

        int geoScore = 0;
        try {
            geoScore = Integer.parseInt(bikApi.getGeoscoreForAddress(addressDto, BIK_KEY).getScore());
        } catch (FeignException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok(geoScore);
    }
}
