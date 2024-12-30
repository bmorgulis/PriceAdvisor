package com.example.priceadvisor.controller;

import com.example.priceadvisor.service.RdsService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/rds")
public class RdsController {

    private final RdsService rdsService;

    public RdsController(RdsService rdsService) {
        this.rdsService = rdsService;
    }

    @GetMapping("/status/{dbInstanceIdentifier}") //this line is from chatgpt
    public String getRdsStatus() {
        return rdsService.getRdsStatus();
    }

    @PostMapping("/start/{dbInstanceIdentifier}") //this line is from chatgpt
    public void startRdsInstance(@PathVariable String dbInstanceIdentifier) {
        rdsService.startRdsInstance();
    }
}

