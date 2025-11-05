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
     * Process a request and grow memory very gradually (~10-30KB per request)
     * Creates 1-3 objects of 1KB-5KB each for sustainable 3-4 hour memory growth
     */
    public Map<String, Object> processRequest() {
        long requestId = totalProcessedRequests.incrementAndGet();
        
        // Create only 1-3 objects per request for very gradual growth
        int objectCount = ThreadLocalRandom.current().nextInt(1, 4);
        List<HeavyObject> newObjects = new ArrayList<>();
        
        for (int i = 0; i < objectCount; i++) {
            String objectId = "req_" + requestId + "_obj_" + i;
            HeavyObject heavyObject = new HeavyObject(objectId);
            newObjects.add(heavyObject);
            
            // Store in multiple caches for maximum retention
            storeInMultipleCaches(heavyObject);
            
            totalMemoryAllocated.addAndGet(heavyObject.getApproximateSize());
        }
        
        // Create circular references among new objects (now no-op due to removed circular refs)
        circularReferenceManager.createCircularReferences(newObjects);
        
        // Also create cross-references with existing objects (now no-op)
        createCrossReferences(newObjects);
        
        // Perform partial cleanup to prevent quick OOM while maintaining leak
        performPartialCleanup();
        
        return buildResponseStats();
    }
    
    /**
     * Store object in selective cache types to control memory growth
     * Reduced from 5 collections to 2-3 for sustainable growth
     */
    private void storeInMultipleCaches(HeavyObject object) {
        // Primary cache - always store
        primaryCache.put(object.getId(), object);
        
        // Retention set - always store (main leak source)
        retentionSet.add(object);
        
        // Conditionally store in other collections to control growth
        long requestId = totalProcessedRequests.get();
        
        // Store in list only every 3rd request
        if (requestId % 3 == 0) {
            retentionList.add(object);
        }
        
        // Store in queue only every 5th request  
        if (requestId % 5 == 0) {
            synchronized (retentionQueue) {
                retentionQueue.offer(object);
            }
        }
        
        // Store in category cache only every 10th request
        if (requestId % 10 == 0) {
            String category = "bucket_" + (object.getTimestamp() / 10000); // 10-second buckets
            categoryCache.computeIfAbsent(category, k -> new ArrayList<>()).add(object);
        }
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
        
        // Note: Cross-referencing removed since circular dependencies were eliminated
        // Objects will still be retained in multiple collections for memory leak demonstration
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
    
    /**
     * Performs partial cleanup to slow down memory growth while maintaining leak
     * More aggressive cleanup every 25 requests to sustain 3-4 hour growth
     */
    private void performPartialCleanup() {
        long requestCount = totalProcessedRequests.get();
        
        // Cleanup every 25 requests instead of 50
        if (requestCount % 25 != 0) {
            return;
        }
        
        // Clean more aggressively but still maintain growth
        int primaryCacheSize = primaryCache.size();
        int retentionListSize = retentionList.size();
        
        if (primaryCacheSize > 500) { // Start cleanup after 500 objects (lower threshold)
            // Remove 30% of random entries from primary cache
            List<String> keysToRemove = new ArrayList<>();
            int removeCount = Math.max(1, primaryCacheSize * 3 / 10); // 30%
            
            Iterator<String> iterator = primaryCache.keySet().iterator();
            while (iterator.hasNext() && keysToRemove.size() < removeCount) {
                keysToRemove.add(iterator.next());
            }
            
            for (String key : keysToRemove) {
                primaryCache.remove(key);
            }
        }
        
        if (retentionListSize > 300) { // Start cleanup after 300 objects (lower threshold)
            // Remove 40% of oldest entries from retention list
            int removeCount = Math.max(1, retentionListSize * 4 / 10); // 40%
            
            synchronized (retentionList) {
                for (int i = 0; i < removeCount && !retentionList.isEmpty(); i++) {
                    retentionList.remove(0); // Remove oldest
                }
            }
        }
        
        // Clean more queue entries
        synchronized (retentionQueue) {
            int queueSize = retentionQueue.size();
            if (queueSize > 200) {
                int removeCount = queueSize / 3; // Remove 33%
                for (int i = 0; i < removeCount && !retentionQueue.isEmpty(); i++) {
                    retentionQueue.poll();
                }
            }
        }
        
        // Clean some category cache entries but keep retentionSet for leak
        if (categoryCache.size() > 50) {
            Iterator<Map.Entry<String, List<HeavyObject>>> iterator = categoryCache.entrySet().iterator();
            int removeCount = categoryCache.size() / 4; // Remove 25%
            
            for (int i = 0; i < removeCount && iterator.hasNext(); i++) {
                iterator.next();
                iterator.remove();
            }
        }
        
        // Note: Never clean retentionSet to maintain memory leak
    }
}