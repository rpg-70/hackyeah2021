package com.bikpicture.app.locations.dto;

import lombok.Data;

import java.util.List;

@Data
public class Autocomplete {
    private List<String> modes;
    private SearchLocationMode searchLocationMode;
}
