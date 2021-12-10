package com.bikpicture.app.locations;

import com.bikpicture.app.locations.dto.Autocomplete;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Profile({"dev", "prod"})
@RestController
public class AutocompleteController {
    private Autocomplete autocomplete;

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();

        autocomplete = mapper.readValue(getClass().getResourceAsStream("/data/autocomplete.yaml"), Autocomplete.class);
    }

    @CrossOrigin
    @GetMapping("/autocomplete/modes")
    public List<String> getModes() {
        return autocomplete.getModes();
    }

    @CrossOrigin
    @GetMapping("/autocomplete/modes/searchLocation")
    public List<String> getSearchLocationGroups() {
        return autocomplete.getSearchLocationMode().getLocationGroups();
    }

    @CrossOrigin
    @GetMapping("/autocomplete/modes/searchLocation/smallBusiness")
    public List<String> getSearchLocationGroupsSmallBusiness() {
        return autocomplete.getSearchLocationMode().getSmallBusiness();
    }
}
