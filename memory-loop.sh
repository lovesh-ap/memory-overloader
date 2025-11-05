#!/bin/bash

# Memory Leak Test - Continuous API Calls Script
# This script provides various ways to loop curl requests to trigger memory growth

BASE_URL="http://localhost:8080/api"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check if application is running
check_app() {
    if curl -s "$BASE_URL/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Application is running${NC}"
        return 0
    else
        echo -e "${RED}❌ Application is not running. Please start it first:${NC}"
        echo "   java -Xms256m -Xmx512m -XX:+UseG1GC -XX:+PrintGC -jar target/memory-leak-app-1.0.0.jar"
        return 1
    fi
}

# Function to display help
show_help() {
    echo -e "${BLUE}Memory Leak Test - Continuous API Calls${NC}"
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Options:"
    echo "  simple         - Basic infinite loop (1 second delay)"
    echo "  slow           - Slow loop for long-term testing (30 second delay)"
    echo "  fast           - Fast loop (0.1 second delay)"
    echo "  monitored      - Loop with memory monitoring"
    echo "  counted [N]    - Run N requests (default: 50)"
    echo "  parallel [N]   - Run N parallel requests (default: 10)"
    echo "  stress         - High-stress testing mode"
    echo "  watch          - Real-time memory monitoring"
    echo "  clear          - Clear all caches and reset memory"
    echo "  help           - Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 simple                    # Basic infinite loop"
    echo "  $0 slow                     # Long-term gradual testing (3-4 hours)"
    echo "  $0 counted 100              # Run 100 requests"
    echo "  $0 parallel 5               # 5 parallel requests"
    echo "  $0 monitored                # Loop with monitoring"
    echo "  $0 clear                    # Clear all caches"
}

# Basic infinite loop
simple_loop() {
    echo -e "${GREEN}Starting simple infinite loop (Ctrl+C to stop)${NC}"
    counter=1
    while true; do
        echo -e "${BLUE}Request $counter${NC}"
        curl -X POST "$BASE_URL/process" 2>/dev/null || echo -e "${RED}Request failed${NC}"
        echo ""
        counter=$((counter + 1))
        sleep 1
    done
}

# Slow loop for long-term testing (3-4 hours)
slow_loop() {
    echo -e "${GREEN}Starting slow loop for long-term testing (30 second delays)${NC}"
    echo -e "${YELLOW}This mode is designed for 3-4 hour gradual memory growth${NC}"
    echo -e "${YELLOW}Ctrl+C to stop${NC}"
    
    counter=1
    start_time=$(date +%s)
    
    while true; do
        current_time=$(date +%s)
        elapsed=$((current_time - start_time))
        elapsed_hours=$((elapsed / 3600))
        elapsed_mins=$(((elapsed % 3600) / 60))
        
        echo -e "${BLUE}Request $counter (Running for ${elapsed_hours}h ${elapsed_mins}m)${NC}"
        
        # Get memory stats before request
        response=$(curl -s -X POST "$BASE_URL/process")
        if [ $? -eq 0 ]; then
            # Extract memory info
            used_memory=$(echo "$response" | grep -o '"usedMemoryMB":[0-9]*' | cut -d':' -f2)
            max_memory=$(echo "$response" | grep -o '"maxMemoryMB":[0-9]*' | cut -d':' -f2)
            cache_size=$(echo "$response" | grep -o '"primaryCacheSize":[0-9]*' | cut -d':' -f2)
            
            if [ ! -z "$used_memory" ] && [ ! -z "$max_memory" ]; then
                memory_percent=$((used_memory * 100 / max_memory))
                echo -e "  Memory: ${YELLOW}${used_memory}MB/${max_memory}MB (${memory_percent}%)${NC} | Cache: ${YELLOW}${cache_size}${NC}"
            fi
        else
            echo -e "${RED}Request failed${NC}"
        fi
        
        echo ""
        counter=$((counter + 1))
        sleep 30  # 30 second delay for gradual growth
    done
}

# Fast loop
fast_loop() {
    echo -e "${GREEN}Starting fast loop (Ctrl+C to stop)${NC}"
    counter=1
    while true; do
        echo -e "${BLUE}Fast Request $counter${NC}"
        curl -X POST "$BASE_URL/process" 2>/dev/null || echo -e "${RED}Request failed${NC}"
        counter=$((counter + 1))
        sleep 0.1
    done
}

# Loop with monitoring
monitored_loop() {
    echo -e "${GREEN}Starting monitored loop (Ctrl+C to stop)${NC}"
    counter=1
    while true; do
        echo -e "${BLUE}=== Request $counter ===${NC}"
        response=$(curl -s -X POST "$BASE_URL/process")
        
        if [ $? -eq 0 ]; then
            # Extract key metrics using grep and cut (works without jq)
            used_memory=$(echo "$response" | grep -o '"usedMemoryMB":[0-9]*' | cut -d':' -f2)
            cache_size=$(echo "$response" | grep -o '"primaryCacheSize":[0-9]*' | cut -d':' -f2)
            total_requests=$(echo "$response" | grep -o '"totalRequests":[0-9]*' | cut -d':' -f2)
            
            echo -e "Memory: ${YELLOW}${used_memory}MB${NC} | Cache: ${YELLOW}${cache_size}${NC} | Total: ${YELLOW}${total_requests}${NC}"
            
            # Warning for high memory usage
            if [ "$used_memory" -gt 400 ]; then
                echo -e "${RED}⚠️  High memory usage detected: ${used_memory}MB${NC}"
            fi
        else
            echo -e "${RED}Request failed${NC}"
        fi
        
        counter=$((counter + 1))
        sleep 2
    done
}

# Counted requests
counted_loop() {
    local count=${1:-50}
    echo -e "${GREEN}Running $count requests${NC}"
    
    for i in $(seq 1 $count); do
        echo -e "${BLUE}Request $i/$count${NC}"
        response=$(curl -s -X POST "$BASE_URL/process")
        
        if [ $? -eq 0 ]; then
            # Extract metrics
            used_memory=$(echo "$response" | grep -o '"usedMemoryMB":[0-9]*' | cut -d':' -f2)
            cache_size=$(echo "$response" | grep -o '"primaryCacheSize":[0-9]*' | cut -d':' -f2)
            
            echo -e "Memory: ${YELLOW}${used_memory}MB${NC} | Cache: ${YELLOW}${cache_size}${NC}"
        else
            echo -e "${RED}Request $i failed${NC}"
        fi
        
        sleep 1
    done
    
    echo -e "${GREEN}Completed $count requests${NC}"
}

# Parallel requests
parallel_requests() {
    local count=${1:-10}
    echo -e "${GREEN}Running $count parallel requests${NC}"
    
    for i in $(seq 1 $count); do
        curl -s -X POST "$BASE_URL/process" &
        echo -e "${BLUE}Started parallel request $i${NC}"
    done
    
    echo -e "${YELLOW}Waiting for all requests to complete...${NC}"
    wait
    echo -e "${GREEN}All parallel requests completed${NC}"
}

# Stress testing
stress_test() {
    echo -e "${RED}⚡ Starting stress test mode${NC}"
    echo -e "${YELLOW}Phase 1: 20 fast requests${NC}"
    
    for i in $(seq 1 20); do
        echo -e "${BLUE}Stress request $i/20${NC}"
        curl -X POST "$BASE_URL/process" 2>/dev/null &
        sleep 0.05
    done
    wait
    
    echo -e "${YELLOW}Phase 2: 10 parallel requests${NC}"
    for i in $(seq 1 10); do
        curl -s -X POST "$BASE_URL/process" &
    done
    wait
    
    echo -e "${GREEN}Stress test completed${NC}"
    
    # Show final stats
    echo -e "${BLUE}Final memory stats:${NC}"
    curl -s "$BASE_URL/stats" | grep -E '"usedMemoryMB"|"primaryCacheSize"|"totalRequests"'
}

# Real-time monitoring
watch_memory() {
    echo -e "${GREEN}Real-time memory monitoring (Ctrl+C to stop)${NC}"
    while true; do
        clear
        echo -e "${BLUE}=== Memory Leak Application Status ===${NC}"
        echo -e "${YELLOW}$(date)${NC}"
        echo ""
        
        health_response=$(curl -s "$BASE_URL/health")
        if [ $? -eq 0 ]; then
            status=$(echo "$health_response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
            memory_percent=$(echo "$health_response" | grep -o '"memoryUsagePercent":"[^"]*"' | cut -d'"' -f4)
            used_mb=$(echo "$health_response" | grep -o '"usedMemoryMB":[0-9]*' | cut -d':' -f2)
            max_mb=$(echo "$health_response" | grep -o '"maxMemoryMB":[0-9]*' | cut -d':' -f2)
            
            echo -e "Status: ${GREEN}$status${NC}"
            echo -e "Memory Usage: ${YELLOW}$memory_percent%${NC} (${used_mb}MB / ${max_mb}MB)"
        else
            echo -e "${RED}Unable to connect to application${NC}"
        fi
        
        sleep 2
    done
}

# Clear all caches
clear_caches() {
    echo -e "${YELLOW}Clearing all caches and resetting memory...${NC}"
    
    # Get stats before clearing
    echo -e "${BLUE}Memory stats before clearing:${NC}"
    before_response=$(curl -s "$BASE_URL/stats")
    if [ $? -eq 0 ]; then
        used_memory=$(echo "$before_response" | grep -o '"usedMemoryMB":[0-9]*' | cut -d':' -f2)
        cache_size=$(echo "$before_response" | grep -o '"primaryCacheSize":[0-9]*' | cut -d':' -f2)
        total_requests=$(echo "$before_response" | grep -o '"totalRequests":[0-9]*' | cut -d':' -f2)
        
        echo -e "Memory: ${YELLOW}${used_memory}MB${NC} | Cache: ${YELLOW}${cache_size}${NC} | Requests: ${YELLOW}${total_requests}${NC}"
    else
        echo -e "${RED}Failed to get stats before clearing${NC}"
    fi
    
    # Clear caches
    echo -e "${BLUE}Sending clear request...${NC}"
    clear_response=$(curl -s -X POST "$BASE_URL/clear")
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Caches cleared successfully${NC}"
        
        # Show stats after clearing
        echo -e "${BLUE}Memory stats after clearing:${NC}"
        used_memory=$(echo "$clear_response" | grep -o '"usedMemoryMB":[0-9]*' | cut -d':' -f2)
        cache_size=$(echo "$clear_response" | grep -o '"primaryCacheSize":[0-9]*' | cut -d':' -f2)
        total_requests=$(echo "$clear_response" | grep -o '"totalRequests":[0-9]*' | cut -d':' -f2)
        
        echo -e "Memory: ${YELLOW}${used_memory}MB${NC} | Cache: ${YELLOW}${cache_size}${NC} | Requests: ${YELLOW}${total_requests}${NC}"
        
        # Wait a moment and force GC
        echo -e "${YELLOW}Waiting for garbage collection...${NC}"
        sleep 3
        
        # Final health check
        final_health=$(curl -s "$BASE_URL/health")
        if [ $? -eq 0 ]; then
            final_memory=$(echo "$final_health" | grep -o '"usedMemoryMB":[0-9]*' | cut -d':' -f2)
            memory_percent=$(echo "$final_health" | grep -o '"memoryUsagePercent":"[^"]*"' | cut -d'"' -f4)
            echo -e "${GREEN}Final memory usage: ${final_memory}MB (${memory_percent}%)${NC}"
        fi
    else
        echo -e "${RED}❌ Failed to clear caches${NC}"
    fi
}

# Main script logic
case "${1:-help}" in
    "simple")
        check_app && simple_loop
        ;;
    "slow")
        check_app && slow_loop
        ;;
    "fast")
        check_app && fast_loop
        ;;
    "monitored")
        check_app && monitored_loop
        ;;
    "counted")
        check_app && counted_loop "$2"
        ;;
    "parallel")
        check_app && parallel_requests "$2"
        ;;
    "stress")
        check_app && stress_test
        ;;
    "watch")
        check_app && watch_memory
        ;;
    "clear")
        check_app && clear_caches
        ;;
    "help"|*)
        show_help
        ;;
esac