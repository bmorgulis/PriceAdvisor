package com.example.priceadvisor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/terms-of-use")
    public String termsOfUse() {
        return "terms-of-use"; // This will resolve to /src/main/resources/templates/terms-of-use.html
    }
}
