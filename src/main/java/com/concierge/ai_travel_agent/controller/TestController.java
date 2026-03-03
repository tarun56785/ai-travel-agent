package com.concierge.ai_travel_agent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/secure/test")
    public String secureEndpoint() {
        return "Success! The bouncer checked your VIP wristband and let you in!";
    }
}
