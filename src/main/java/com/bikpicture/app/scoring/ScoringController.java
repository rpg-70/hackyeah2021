package com.bikpicture.app.scoring;

import com.bikpicture.app.bik.AddressDto;
import com.bikpicture.app.bik.BikApi;
import com.bikpicture.app.googlemaps.PlacesApi;
import com.bikpicture.app.googlemaps.ResponseDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/scoring")
@RequiredArgsConstructor
@Slf4j
public class ScoringController {
    private final PlacesApi placesApi;
    private final BikApi bikApi;

    private final static int MAX_PLACES = 100;
    private final static String CITY = "Łódź";

    private final static String BIK_KEY = "36b99ed9ad5743a5a62be4a2bb5eb465";

    @GetMapping(path = "/location/personal")
    public ResponseEntity getScoreForPersonalLocation(
            @RequestParam String streetName,
            @RequestParam String streetNumber,
            @RequestParam(required = false, defaultValue = "1000") int radius,
            @RequestParam(required = false) List<String> keywords
    ) throws InterruptedException {
        ResponseDto addressDetails = placesApi.getAddressDetails(String.format("%s %s %s", streetName, streetNumber, CITY));
        Map<String, Map> geometry = (Map) addressDetails.getResults().get(0).get("geometry");
        Map location = geometry.get("location");
        double latitude = (double) location.get("lat");
        double longitude =(double) location.get("lng");
        int resultSize;
        String pageToken = null;
        List<Integer> numbersOfPlaces = new ArrayList<>();
        PersonalLocationResponseDto response = new PersonalLocationResponseDto();
        response.setCoordinates(new ArrayList<>());
        for(String keyword : keywords) {
            int numberOfPlaces = 0;
            do {
                ResponseDto nearbyPlaces = placesApi.findNearbyPlaces(String.format("%s,%s", latitude, longitude), radius, null, keyword, pageToken);
                pageToken = nearbyPlaces.getNext_page_token();
                resultSize = nearbyPlaces.getResults().size();
                numberOfPlaces += resultSize;
                List<AbstractMap.SimpleEntry> places = nearbyPlaces.getResults().stream().map(result -> {
                    Map<String, Map> geometry2 = (Map) result.get("geometry");
                    Map location2 = geometry2.get("location");
                    return new AbstractMap.SimpleEntry(location2.get("lat"), location2.get("lng"));
                }).collect(Collectors.toList());
                List<PersonalLocationResponseDto.Coordinates> tempCoordinates = places.stream()
                        .map(place -> PersonalLocationResponseDto.Coordinates.builder()
                                    .latitude((Double) place.getKey())
                                    .longitude((Double) place.getValue())
                                    .keyword(keyword)
                                    .build())
                        .collect(Collectors.toList());
                response.getCoordinates().addAll(tempCoordinates);
                if (resultSize >= PlacesApi.PAGE_SIZE) {
                TimeUnit.SECONDS.sleep(PlacesApi.TIMEOUT);
                }
            } while (numberOfPlaces >= MAX_PLACES || resultSize >= PlacesApi.PAGE_SIZE);
            numbersOfPlaces.add(numberOfPlaces);
        }
        response.setScore(numbersOfPlaces.stream()
                .mapToInt(Integer::intValue)
                .average()
                .getAsDouble());
        return ResponseEntity.ok(response);
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
