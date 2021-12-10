package com.bikpicture.app.locations;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RestController;

@Profile({"dev", "prod"})
@RestController
public class LocationSearchController {


}
