package com.bikpicture.app.googlemaps;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ResponseDto {
    private String status;
    private String next_page_token;
    private List<Map> results;
}
