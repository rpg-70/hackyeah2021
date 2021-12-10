package com.bikpicture.app.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile({"dev", "prod"})
@RestController
public class VersionController {

    @Value("${app.env}")
    private String env;

    @Value("${app.version}")
    private String version;

    @CrossOrigin
    @GetMapping("/version")
    public String version() {
        return "BikPicture v" + version + "-" + env;
    }

}
