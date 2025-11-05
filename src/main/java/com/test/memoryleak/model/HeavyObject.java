package com.test.memoryleak.model;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Heavy object designed to consume memory between 10KB-100KB
 */
public class HeavyObject {
    private final String id;
    private final byte[] data;
    private final String metadata;
    private final List<String> tags;
    private final Map<String, String> properties;
    private final long timestamp;
    
    // For circular references
    private HeavyObject reference;
    private final List<HeavyObject> connections;
    
    public HeavyObject(String id) {
        this.id = id;
        this.timestamp = System.currentTimeMillis();
        this.connections = new ArrayList<>();

        // Random size between 1KB-10KB
        int dataSize = ThreadLocalRandom.current().nextInt(1024, 10240); // 1KB-10KB
        this.data = new byte[dataSize];
        ThreadLocalRandom.current().nextBytes(this.data);
        
        // Additional memory consumption
        this.metadata = generateMetadata();
        this.tags = generateTags();
        this.properties = generateProperties();
    }
    
    private String generateMetadata() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("Metadata_").append(id).append("_").append(i).append("_");
            sb.append(UUID.randomUUID().toString()).append("; ");
        }
        return sb.toString();
    }
    
    private List<String> generateTags() {
        List<String> tagList = new ArrayList<>();
        int tagCount = ThreadLocalRandom.current().nextInt(50, 200);
        for (int i = 0; i < tagCount; i++) {
            tagList.add("Tag_" + id + "_" + i + "_" + UUID.randomUUID().toString());
        }
        return tagList;
    }
    
    private Map<String, String> generateProperties() {
        Map<String, String> props = new HashMap<>();
        int propCount = ThreadLocalRandom.current().nextInt(20, 100);
        for (int i = 0; i < propCount; i++) {
            props.put("prop_" + i, "value_" + UUID.randomUUID().toString() + "_" + System.nanoTime());
        }
        return props;
    }
    
    // Methods for circular references
    public void setReference(HeavyObject reference) {
        this.reference = reference;
    }
    
    public void addConnection(HeavyObject connection) {
        this.connections.add(connection);
    }
    
    public HeavyObject getReference() {
        return reference;
    }
    
    public List<HeavyObject> getConnections() {
        return connections;
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