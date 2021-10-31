package org.cloudstash;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile({"dev", "prod"})
@RestController
public class VersionController {

    @Value("${cloudstash.env}")
    private String env;

    @Value("${cloudstash.version}")
    private String version;

    @GetMapping("/version")
    public String hello() {
        return "CloudStash v" + version + "-" + env;
    }
}
