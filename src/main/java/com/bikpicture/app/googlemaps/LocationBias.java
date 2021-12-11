package com.bikpicture.app.googlemaps;

public class LocationBias {
    public static String getValue(String latitude, String longitude, int radius) {
        return String.format("circle:%s@%s,%s", radius, latitude, longitude);
    }
}
