package com.test.memoryleak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MemoryLeakApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemoryLeakApplication.class, args);
    }
}