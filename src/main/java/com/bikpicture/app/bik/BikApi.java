package com.bikpicture.app.bik;

import com.bikpicture.app.config.FeignConfig;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "bikApi", url = "https://gateway.oapi.bik.pl", configuration = FeignConfig.class)
public interface BikApi {
    @PostMapping("/bik-api-5/geoscore-adres")
    GeoScoringAddressResponseDto getGeoscoreForAddress(
            @RequestBody AddressDto addressDto,
            @RequestHeader(name = "BIK-OAPI-Key") String key
    );
}
