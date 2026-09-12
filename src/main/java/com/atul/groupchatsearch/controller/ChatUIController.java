package com.atul.groupchatsearch.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatUIController {

    @GetMapping("/")
    public String index() {
        // Forwards the root URL to the static HTML file
        return "forward:/index.html";
    }
}