package org.hackyeah;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Base64;

import static j2html.TagCreator.*;

@Profile({"dev", "prod"})
@RestController
public class VersionController {

    @Value("${app.env}")
    private String env;

    @Value("${app.version}")
    private String version;

    @GetMapping("/version")
    public String version() {
        return "HackYeah 2021 v" + version + "-" + env;
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String splash() throws IOException {
        byte[] bytes = getClass().getResourceAsStream("/img/waiting.gif").readAllBytes();
        String imageData = Base64.getEncoder().encodeToString(bytes);

        return body(
                h1("Ready for HackYeah 2021? ;-)"),
                img().withSrc("data:image/gif;base64, " + imageData),
                p("Backendzik już czeka :)"),
                p(version())
        ).render();
    }
}
