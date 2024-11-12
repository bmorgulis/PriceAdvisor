package com.example.itemmgr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for handling items, processing, and downloads.
 */
@Controller
@RequestMapping("/")
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    private ApplicationContext context;

    public ItemController() {

    }

    /**
     * Shuts down the Spring Boot application.
     */
    @PostMapping("/shutdown")
    public void shutdownApplication() {
        SpringApplication.exit(context, () -> 0);
    }
}
