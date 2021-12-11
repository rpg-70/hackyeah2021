package com.bikpicture.app.scoring;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class PersonalLocationResponseDto {
    private double score;
    private List<Coordinates> coordinates;

    @Data
    @Builder
    public static class Coordinates {
        private double latitude;
        private double longitude;
        private String keyword;
        private String name;
    }
}
