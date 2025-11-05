package com.test.memoryleak.model;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Heavy object designed to consume memory between 512B-2KB for very gradual growth over 3-4 hours
 */
public class HeavyObject {
    private final String id;
    private final byte[] data;
    private final String metadata;
    private final List<String> tags;
    private final Map<String, String> properties;
    private final long timestamp;
    
    public HeavyObject(String id) {
        this.id = id;
        this.timestamp = System.currentTimeMillis();

        // Random size between 500B-2KB for very gradual memory consumption over 3-4 hours
        int dataSize = ThreadLocalRandom.current().nextInt(512, 2048); // 512B-2KB
        this.data = new byte[dataSize];
        ThreadLocalRandom.current().nextBytes(this.data);
        
        // Additional memory consumption
        this.metadata = generateMetadata();
        this.tags = generateTags();
        this.properties = generateProperties();
    }
    
    private String generateMetadata() {
        StringBuilder sb = new StringBuilder();
        sb.append("Metadata_").append(id).append("_");
        sb.append(UUID.randomUUID().toString()).append("; ");
        return sb.toString();
    }
    
    private List<String> generateTags() {
        List<String> tagList = new ArrayList<>();
        tagList.add("Tag_" + id + "_" + UUID.randomUUID().toString());
        return tagList;
    }
    
    private Map<String, String> generateProperties() {
        Map<String, String> props = new HashMap<>();
        props.put("prop_" + id, "value_" + UUID.randomUUID().toString() + "_" + System.nanoTime());
        return props;
    }
    
    // Getters
    public String getId() { return id; }
    public byte[] getData() { return data; }
    public String getMetadata() { return metadata; }
    public List<String> getTags() { return tags; }
    public Map<String, String> getProperties() { return properties; }
    public long getTimestamp() { return timestamp; }
    
    public int getApproximateSize() {
        return data.length + metadata.length() + (tags.size() * 50) + (properties.size() * 100);
    }
}