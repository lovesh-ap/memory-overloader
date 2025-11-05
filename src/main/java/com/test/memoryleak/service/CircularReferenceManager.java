package com.test.memoryleak.service;

import com.test.memoryleak.model.HeavyObject;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages circular references between HeavyObjects
 * Strategy C: Controlled circular reference creation
 */
@Component
public class CircularReferenceManager {
    
    /**
     * Creates circular reference chains among the provided objects
     * @param objects List of objects to create circular references for
     */
    public void createCircularReferences(List<HeavyObject> objects) {
        if (objects.size() < 2) return;
        
        // Strategy 1: Create circular chains (A -> B -> C -> A)
        createCircularChains(objects);
        
        // Strategy 2: Create bidirectional references
        createBidirectionalReferences(objects);
        
        // Strategy 3: Create complex webs
        createComplexWeb(objects);
    }
    
    /**
     * Creates circular chains: A -> B -> C -> A
     */
    private void createCircularChains(List<HeavyObject> objects) {
        if (objects.size() < 3) return;
        
        // Create multiple smaller circular chains
        int chainSize = Math.min(5, objects.size() / 2);
        for (int start = 0; start < objects.size() - chainSize; start += chainSize) {
            List<HeavyObject> chain = objects.subList(start, Math.min(start + chainSize, objects.size()));
            
            for (int i = 0; i < chain.size(); i++) {
                HeavyObject current = chain.get(i);
                HeavyObject next = chain.get((i + 1) % chain.size());
                current.setReference(next);
            }
        }
    }
    
    /**
     * Creates bidirectional connections between random pairs
     */
    private void createBidirectionalReferences(List<HeavyObject> objects) {
        int connectionCount = Math.min(objects.size() / 2, 10);
        
        for (int i = 0; i < connectionCount; i++) {
            int index1 = ThreadLocalRandom.current().nextInt(objects.size());
            int index2 = ThreadLocalRandom.current().nextInt(objects.size());
            
            if (index1 != index2) {
                HeavyObject obj1 = objects.get(index1);
                HeavyObject obj2 = objects.get(index2);
                
                obj1.addConnection(obj2);
                obj2.addConnection(obj1);
            }
        }
    }
    
    /**
     * Creates complex web where each object references multiple others
     */
    private void createComplexWeb(List<HeavyObject> objects) {
        for (HeavyObject obj : objects) {
            int connectionCount = ThreadLocalRandom.current().nextInt(2, Math.min(6, objects.size()));
            
            Set<Integer> usedIndices = new HashSet<>();
            for (int i = 0; i < connectionCount; i++) {
                int randomIndex;
                do {
                    randomIndex = ThreadLocalRandom.current().nextInt(objects.size());
                } while (usedIndices.contains(randomIndex) || objects.get(randomIndex) == obj);
                
                usedIndices.add(randomIndex);
                obj.addConnection(objects.get(randomIndex));
            }
        }
    }
    
    /**
     * Gets statistics about circular references
     */
    public Map<String, Integer> getCircularReferenceStats(List<HeavyObject> objects) {
        Map<String, Integer> stats = new HashMap<>();
        
        int objectsWithReferences = 0;
        int totalConnections = 0;
        int circularChains = 0;
        
        for (HeavyObject obj : objects) {
            if (obj.getReference() != null || !obj.getConnections().isEmpty()) {
                objectsWithReferences++;
            }
            
            totalConnections += obj.getConnections().size();
            
            // Check for circular chains
            if (obj.getReference() != null && hasCircularChain(obj, new HashSet<>())) {
                circularChains++;
            }
        }
        
        stats.put("objectsWithReferences", objectsWithReferences);
        stats.put("totalConnections", totalConnections);
        stats.put("circularChains", circularChains);
        stats.put("totalObjects", objects.size());
        
        return stats;
    }
    
    private boolean hasCircularChain(HeavyObject start, Set<HeavyObject> visited) {
        if (visited.contains(start)) {
            return true;
        }
        
        visited.add(start);
        HeavyObject next = start.getReference();
        
        if (next != null) {
            return hasCircularChain(next, visited);
        }
        
        return false;
    }
}