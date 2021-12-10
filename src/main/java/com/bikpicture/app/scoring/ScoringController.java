package com.bikpicture.app.scoring;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.bikpicture.app.googlemaps.PlacesApi;
import com.bikpicture.app.googlemaps.PlacesTypes;
import com.bikpicture.app.googlemaps.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/scoring")
@RequiredArgsConstructor
@Slf4j
public class ScoringController {
    private final PlacesApi placesApi;

    private final static int MAX_PLACES = 100;

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
                ResponseDto response = placesApi.findNearbyPlaces(String.format("%s,%s", latitude, longitude), radius, type, keyword, pageToken);
                pageToken = response.getNext_page_token();
                resultSize = response.getResults().size();
                numberOfPlaces += resultSize;
                TimeUnit.SECONDS.sleep(PlacesApi.TIMEOUT);
            } while (numberOfPlaces >= MAX_PLACES || resultSize >= PlacesApi.PAGE_SIZE);
            return ResponseEntity.ok(numberOfPlaces);
        } catch (FeignException | InterruptedException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
