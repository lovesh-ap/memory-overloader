package com.test.memoryleak.service;

import com.test.memoryleak.model.HeavyObject;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages circular references between HeavyObjects
 * Note: Circular references have been removed from HeavyObject for cleaner memory management
 */
@Component
public class CircularReferenceManager {
    
    /**
     * Creates circular reference chains among the provided objects
     * Note: This is now a no-op since circular references have been removed
     * @param objects List of objects to create circular references for
     */
    public void createCircularReferences(List<HeavyObject> objects) {
        // No-op: Circular references have been removed from HeavyObject
        // This method is kept for API compatibility
    }
    
    /**
     * Gets statistics about circular references
     * Note: Returns empty stats since circular references have been removed
     */
    public Map<String, Integer> getCircularReferenceStats(List<HeavyObject> objects) {
        Map<String, Integer> stats = new HashMap<>();
        
        // Return zero stats since circular references are removed
        stats.put("objectsWithReferences", 0);
        stats.put("totalConnections", 0);
        stats.put("circularChains", 0);
        stats.put("totalObjects", objects.size());
        
        return stats;
    }
}