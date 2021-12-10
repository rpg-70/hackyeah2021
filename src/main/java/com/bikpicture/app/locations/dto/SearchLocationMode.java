package com.bikpicture.app.locations.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchLocationMode {
    private List<String> locationGroups;
    private List<String> smallBusiness;
}
