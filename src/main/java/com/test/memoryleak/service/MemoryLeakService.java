package com.test.memoryleak.service;

import com.test.memoryleak.model.HeavyObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Memory leak service with multiple cache types and controlled growth
 */
@Service
public class MemoryLeakService {
    
    @Autowired
    private CircularReferenceManager circularReferenceManager;
    
    // Multiple cache types for maximum retention
    private final Map<String, HeavyObject> primaryCache = new ConcurrentHashMap<>();
    private final List<HeavyObject> retentionList = Collections.synchronizedList(new ArrayList<>());
    private final Queue<HeavyObject> retentionQueue = new LinkedList<>();
    private final Set<HeavyObject> retentionSet = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, List<HeavyObject>> categoryCache = new ConcurrentHashMap<>();
    
    // Statistics
    private final AtomicLong totalProcessedRequests = new AtomicLong(0);
    private final AtomicLong totalMemoryAllocated = new AtomicLong(0);
    
    /**
     * Process a request and grow memory by approximately 1MB
     * Creates 10-50 objects of 10KB-100KB each
     */
    public Map<String, Object> processRequest() {
        long requestId = totalProcessedRequests.incrementAndGet();
        
        // Create 10-50 objects per request
        int objectCount = ThreadLocalRandom.current().nextInt(10, 51);
        List<HeavyObject> newObjects = new ArrayList<>();
        
        for (int i = 0; i < objectCount; i++) {
            String objectId = "req_" + requestId + "_obj_" + i;
            HeavyObject heavyObject = new HeavyObject(objectId);
            newObjects.add(heavyObject);
            
            // Store in multiple caches for maximum retention
            storeInMultipleCaches(heavyObject);
            
            totalMemoryAllocated.addAndGet(heavyObject.getApproximateSize());
        }
        
        // Create circular references among new objects
        circularReferenceManager.createCircularReferences(newObjects);
        
        // Also create cross-references with existing objects
        createCrossReferences(newObjects);
        
        return buildResponseStats();
    }
    
    /**
     * Store object in multiple cache types to prevent GC
     */
    private void storeInMultipleCaches(HeavyObject object) {
        // Primary cache
        primaryCache.put(object.getId(), object);
        
        // Retention list (never cleared)
        retentionList.add(object);
        
        // Retention queue
        synchronized (retentionQueue) {
            retentionQueue.offer(object);
        }
        
        // Retention set
        retentionSet.add(object);
        
        // Category cache (by timestamp bucket)
        String category = "bucket_" + (object.getTimestamp() / 10000); // 10-second buckets
        categoryCache.computeIfAbsent(category, k -> new ArrayList<>()).add(object);
    }
    
    /**
     * Create cross-references between new objects and existing ones
     */
    private void createCrossReferences(List<HeavyObject> newObjects) {
        if (retentionList.size() < 10) return;
        
        // Pick some random existing objects
        int existingCount = Math.min(10, retentionList.size());
        List<HeavyObject> existingObjects = new ArrayList<>();
        
        for (int i = 0; i < existingCount; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(retentionList.size());
            existingObjects.add(retentionList.get(randomIndex));
        }
        
        // Create connections between new and existing
        for (HeavyObject newObj : newObjects) {
            for (HeavyObject existingObj : existingObjects) {
                if (ThreadLocalRandom.current().nextDouble() < 0.3) { // 30% chance
                    newObj.addConnection(existingObj);
                    existingObj.addConnection(newObj);
                }
            }
        }
    }
    
    /**
     * Get memory statistics
     */
    public Map<String, Object> getMemoryStats() {
        return buildResponseStats();
    }
    
    /**
     * Clear all caches (for testing)
     */
    public Map<String, Object> clearCaches() {
        primaryCache.clear();
        retentionList.clear();
        
        synchronized (retentionQueue) {
            retentionQueue.clear();
        }
        
        retentionSet.clear();
        categoryCache.clear();
        
        totalProcessedRequests.set(0);
        totalMemoryAllocated.set(0);
        
        // Force garbage collection
        System.gc();
        
        return buildResponseStats();
    }
    
    /**
     * Build comprehensive response with statistics
     */
    private Map<String, Object> buildResponseStats() {
        Map<String, Object> response = new HashMap<>();
        
        // Cache statistics
        Map<String, Object> cacheStats = new HashMap<>();
        cacheStats.put("primaryCacheSize", primaryCache.size());
        cacheStats.put("retentionListSize", retentionList.size());
        cacheStats.put("retentionQueueSize", retentionQueue.size());
        cacheStats.put("retentionSetSize", retentionSet.size());
        cacheStats.put("categoryCacheSize", categoryCache.size());
        cacheStats.put("totalCategoryObjects", 
            categoryCache.values().stream().mapToInt(List::size).sum());
        
        // Memory statistics
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memoryStats = new HashMap<>();
        memoryStats.put("totalMemoryMB", runtime.totalMemory() / (1024 * 1024));
        memoryStats.put("freeMemoryMB", runtime.freeMemory() / (1024 * 1024));
        memoryStats.put("usedMemoryMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        memoryStats.put("maxMemoryMB", runtime.maxMemory() / (1024 * 1024));
        
        // Application statistics
        Map<String, Object> appStats = new HashMap<>();
        appStats.put("totalRequests", totalProcessedRequests.get());
        appStats.put("approximateMemoryAllocatedMB", totalMemoryAllocated.get() / (1024 * 1024));
        
        // Circular reference statistics
        List<HeavyObject> allObjects = new ArrayList<>(retentionList);
        Map<String, Integer> circularStats = circularReferenceManager.getCircularReferenceStats(allObjects);
        
        response.put("cacheStats", cacheStats);
        response.put("memoryStats", memoryStats);
        response.put("appStats", appStats);
        response.put("circularReferenceStats", circularStats);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
}