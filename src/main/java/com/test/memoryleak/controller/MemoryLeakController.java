package com.test.memoryleak.controller;

import com.test.memoryleak.service.MemoryLeakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for memory leak testing
 */
@RestController
@RequestMapping("/api")
public class MemoryLeakController {
    
    @Autowired
    private MemoryLeakService memoryLeakService;
    
    /**
     * Process request and grow memory by ~1MB
     * Creates 10-50 objects with circular references
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processRequest() {
        try {
            Map<String, Object> result = memoryLeakService.processRequest();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Memory allocation failed: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Get current memory statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = memoryLeakService.getMemoryStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Clear all caches (for testing)
     */
    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearCaches() {
        Map<String, Object> result = memoryLeakService.clearCaches();
        return ResponseEntity.ok(result);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        String status = memoryUsagePercent > 90 ? "CRITICAL" : 
                       memoryUsagePercent > 75 ? "WARNING" : "OK";
        
        Map<String, Object> healthResponse = new HashMap<>();
        healthResponse.put("status", status);
        healthResponse.put("memoryUsagePercent", String.format("%.2f", memoryUsagePercent));
        healthResponse.put("usedMemoryMB", usedMemory / (1024 * 1024));
        healthResponse.put("maxMemoryMB", maxMemory / (1024 * 1024));
        healthResponse.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(healthResponse);
    }
}