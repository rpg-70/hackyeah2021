package com.bikpicture.app.bik;

import lombok.Data;

@Data
public class GeoScoringAddressResponseDto {
    AddressDto inputDataAddress;
    AddressDto nearestPoint;
    AddressDto inputDataLocalization;
    String radius;
    String score;
}