package com.bikpicture.app.googlemaps;

import com.bikpicture.app.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "googlePlacesApi", configuration = FeignConfig.class, url = "https://maps.googleapis.com/maps/api?key=${google.api.key}")
public interface PlacesApi {
    int TIMEOUT = 2; //seconds
    int PAGE_SIZE = 20;

    @GetMapping(path = "place/nearbysearch/json")
    ResponseDto findNearbyPlaces(
            @RequestParam String location,
            @RequestParam(required = false) int radius,
            @RequestParam(required = false) PlacesTypes type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String pagetoken
    );

    @GetMapping(path = "place/findplacefromtext/json?inputtype=textquery&fields=name")
    ResponseDto findPlacesFromText(
            @RequestParam String input,
            @RequestParam(required = false) String locationbias
    );

    @GetMapping(path = "/geocode/json")
    ResponseDto getAddressDetails(
            @RequestParam String address
    );
}
