package com.example.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ServerApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ServerApplication.class)
        .properties("server.port=8081")
        .run(args);
    }
}