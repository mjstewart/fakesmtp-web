package app.configuration.sse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmailController {

    @GetMapping("/")
    public String get() {
        return "sse-example";
    }
}
