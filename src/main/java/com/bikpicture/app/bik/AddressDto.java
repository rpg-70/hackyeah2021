package com.bikpicture.app.bik;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDto {
    String code;
    String city;
    String street;
    String building_number;
}
