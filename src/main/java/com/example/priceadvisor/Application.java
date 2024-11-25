package com.example.priceadvisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Entry point for the Spring Boot application.
 * This class contains the main method to launch the application and
 * configuration setup for the application context.
 */
@SpringBootApplication
public class Application {

    // Logger for this class
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final int PORT = 8080; // Port number to check for existing instances

    /**
     * Launches the Spring Boot application.
     *
     * @param args command-line arguments for the application (not used).
     */
    public static void main(String[] args) {
        if (isPortInUse(PORT)) {
            logger.error("Another instance of the application is already running on port {}.", PORT);
            System.exit(1);
        }

        try {
            logger.info("Starting the Spring Boot application.");
            SpringApplication.run(Application.class, args);
            logger.info("At http://localhost:{}, Spring Boot application started successfully.", PORT);
        } catch (Exception e) {
            logger.error("An error occurred while starting the Spring Boot application.", e);
            System.exit(1); // Exit with a non-zero status to indicate failure
        }
    }

    /**
     * Checks if the specified port is in use.
     *
     * @param port the port number to check.
     * @return true if the port is in use, false otherwise.
     */
    private static boolean isPortInUse(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return false; // Port is available
        } catch (IOException e) {
            return true; // Port is in use
        }
    }
}