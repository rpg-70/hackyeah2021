package com.bikpicture.app.scoring;

import com.bikpicture.app.bik.AddressDto;
import com.bikpicture.app.bik.BikApi;
import com.bikpicture.app.googlemaps.PlacesApi;
import com.bikpicture.app.googlemaps.PlacesTypes;
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
import java.util.concurrent.TimeUnit;

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
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(required = false, defaultValue = "1000") int radius,
            @RequestParam(required = false) PlacesTypes type,
            @RequestParam(required = false) String keyword
    ) {
        try {
            int numberOfPlaces = 0;
            int resultSize;
            String pageToken = null;
            do {
                ResponseDto responseDto = placesApi.findNearbyPlaces(String.format("%s,%s", latitude, longitude), radius, type, keyword, pageToken);
                pageToken = responseDto.getNext_page_token();
                resultSize = responseDto.getResults().size();
                numberOfPlaces += resultSize;
                TimeUnit.SECONDS.sleep(PlacesApi.TIMEOUT);
            } while (numberOfPlaces >= MAX_PLACES || resultSize >= PlacesApi.PAGE_SIZE);
            return ResponseEntity.ok(numberOfPlaces);
        } catch (FeignException | InterruptedException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping(path = "/location/business")
    public ResponseEntity getScoreForBusinessLocation(
            @RequestParam String streetName,
            @RequestParam int streetNumber
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
