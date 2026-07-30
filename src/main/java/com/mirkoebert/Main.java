package com.mirkoebert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    // Example of a bean definition (replace with your actual logic)
    @Bean
    public MyService myService() {
        return new MyServiceImpl(); 
    }
}

