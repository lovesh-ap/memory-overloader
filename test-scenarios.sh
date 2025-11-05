#!/bin/bash

# Memory Leak Test Application - Test Scenarios

echo "🧪 Memory Leak Test Application - Test Scenarios"
echo "================================================="

BASE_URL="http://localhost:8080/api"

# Function to make HTTP request and format JSON response
make_request() {
    local method=$1
    local endpoint=$2
    local description=$3
    
    echo ""
    echo "📋 Test: $description"
    echo "🔗 $method $BASE_URL$endpoint"
    echo "---"
    
    if [ "$method" = "GET" ]; then
        curl -s "$BASE_URL$endpoint" | python3 -m json.tool 2>/dev/null || curl -s "$BASE_URL$endpoint"
    else
        curl -s -X "$method" "$BASE_URL$endpoint" | python3 -m json.tool 2>/dev/null || curl -s -X "$method" "$BASE_URL$endpoint"
    fi
    
    echo ""
    echo "⏱️  Waiting 2 seconds..."
    sleep 2
}

# Check if application is running
check_health() {
    echo "🔍 Checking application health..."
    if curl -s "$BASE_URL/health" > /dev/null; then
        echo "✅ Application is running"
        return 0
    else
        echo "❌ Application is not running. Please start it first:"
        echo "   java -Xms256m -Xmx512m -XX:+UseG1GC -XX:+PrintGC -jar target/memory-leak-app-1.0.0.jar"
        return 1
    fi
}

# Test scenarios
run_memory_leak_tests() {
    echo ""
    echo "🚀 Starting Memory Leak Tests"
    echo "=============================="
    
    # Initial stats
    make_request "GET" "/stats" "Initial Memory Statistics"
    
    # Process multiple requests to grow memory
    for i in {1..5}; do
        make_request "POST" "/process" "Process Request #$i (Grow Memory ~1MB)"
    done
    
    # Check stats after memory growth
    make_request "GET" "/stats" "Memory Statistics After Growth"
    
    # Health check
    make_request "GET" "/health" "Health Check"
    
    # Process more requests
    for i in {6..10}; do
        make_request "POST" "/process" "Process Request #$i (Continue Growing)"
    done
    
    # Final stats
    make_request "GET" "/stats" "Final Memory Statistics"
    
    # Health check
    make_request "GET" "/health" "Final Health Check"
    
    echo ""
    echo "🧹 Optional: Clear caches"
    read -p "Do you want to clear all caches? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        make_request "POST" "/clear" "Clear All Caches"
        make_request "GET" "/stats" "Stats After Clear"
    fi
}

# Main execution
main() {
    if [ "$1" = "health" ]; then
        make_request "GET" "/health" "Health Check Only"
    elif [ "$1" = "stats" ]; then
        make_request "GET" "/stats" "Statistics Only"
    elif [ "$1" = "single" ]; then
        make_request "POST" "/process" "Single Memory Growth Test"
        make_request "GET" "/stats" "Stats After Single Test"
    elif [ "$1" = "clear" ]; then
        make_request "POST" "/clear" "Clear All Caches"
    else
        if check_health; then
            run_memory_leak_tests
        fi
    fi
}

# Run with command line argument or default
main "$1"