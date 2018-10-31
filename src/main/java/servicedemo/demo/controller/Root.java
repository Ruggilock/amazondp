package servicedemo.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Root {
    @RequestMapping("/")
    public String greeting() {
        String welcome= "welcome";

        return welcome;
    }
}
