package com.bikpicture.app.locations;

import com.bikpicture.app.locations.dto.Location;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Profile({"dev", "prod"})
@RestController
public class LocationsController {

    @CrossOrigin
    @PostMapping("/locationSearch")
    public List<Location> searchLocations(@RequestParam SearchCategory category, @RequestBody List<String> subCategories) {
        return null;
    }
}
