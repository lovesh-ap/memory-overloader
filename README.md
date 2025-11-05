# Memory Leak Test Application

A focused Spring Boot application designed to demonstrate memory leaks, circular references, and garbage collection pressure through controlled memory growth patterns.

## 📋 Prerequisites

### System Requirements
- **Java 8 or higher** (OpenJDK or Oracle JDK)
- **Maven 3.6+** for building the application
- **4GB+ RAM** recommended for testing memory scenarios
- **Unix-like OS** (macOS, Linux) or Windows with bash support

### Development Environment Setup

#### 1. Verify Java Installation
```bash
# Check Java version (should be 1.8 or higher)
java -version

# Check JAVA_HOME (optional but recommended)
echo $JAVA_HOME
```

#### 2. Install Maven (if not already installed)

**macOS (using Homebrew):**
```bash
brew install maven
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install maven
```

**Manual Installation:**
```bash
# Download from https://maven.apache.org/download.cgi
# Extract and add to PATH
export PATH=/path/to/maven/bin:$PATH
```

#### 3. Verify Maven Installation
```bash
mvn --version
```

## 🛠 Local Setup Instructions

### Clone and Setup
```bash
# Navigate to your workspace
cd /your/workspace/directory

# If you already have the project, navigate to it
cd memory_overloader

# Verify project structure
ls -la
# Should show: pom.xml, src/, README.md, test-scenarios.sh
```

### Project Structure
```
memory_overloader/
├── pom.xml                          # Maven build configuration
├── README.md                        # This file
├── test-scenarios.sh               # Automated test scripts
├── src/
│   └── main/
│       ├── java/
│       │   └── com/test/memoryleak/
│       │       ├── MemoryLeakApplication.java      # Main application
│       │       ├── controller/
│       │       │   └── MemoryLeakController.java   # REST endpoints
│       │       ├── service/
│       │       │   ├── MemoryLeakService.java      # Core leak logic
│       │       │   └── CircularReferenceManager.java # Reference management
│       │       └── model/
│       │           └── HeavyObject.java            # Memory-heavy object
│       └── resources/
│           └── application.properties              # App configuration
└── target/                          # Build output (created after build)
```

## 🔧 Build Instructions

### 1. Clean Build
```bash
# Navigate to project root
cd /path/to/memory_overloader

# Clean any previous builds and compile
mvn clean compile

# Full build with tests
mvn clean package
```

### 2. Verify Build Success
```bash
# Check if JAR was created
ls -la target/memory-leak-app-1.0.0.jar

```

### 3. Build Troubleshooting

**Common Issues:**

**Java Version Mismatch:**
```bash
# If you get Java version errors
export JAVA_HOME=/path/to/java8
mvn clean package -Djava.version=1.8
```

**Maven Dependencies:**
```bash
# Force dependency refresh
mvn clean package -U

# Clear local repository cache
rm -rf ~/.m2/repository/com/test/memory-leak-app
mvn clean package
```

**Compilation Errors:**
```bash
# Verbose build for debugging
mvn clean package -X -e
```

## 🚀 Running the Application

### 1. Basic Startup
```bash
# With memory limits and GC logging
java -Xms256m -Xmx512m -XX:+UseG1GC -XX:+PrintGC -jar target/memory-leak-app-1.0.0.jar

# Enhanced GC logging
java -Xms256m -Xmx512m -XX:+UseG1GC -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -jar target/memory-leak-app-1.0.0.jar

# Background startup with logging
nohup java -Xms256m -Xmx512m -XX:+UseG1GC -XX:+PrintGC -jar target/memory-leak-app-1.0.0.jar > app.log 2>&1 &
```

### 3. Verify Application Startup
```bash
# Wait for startup (usually 10-15 seconds)
sleep 10

# Check health endpoint
curl http://localhost:8080/api/health

# Expected response:
# {"status":"OK","memoryUsagePercent":"6.93","usedMemoryMB":35,"maxMemoryMB":512,"timestamp":...}
```

## 🎯 Core Features

- **Memory Growth**: ~1MB per API call through 10-50 objects (10KB-100KB each)
- **Circular References**: Controlled circular reference chains using Reference Manager pattern
- **Multiple Cache Types**: 5 different cache types to maximize object retention
- **No Database**: Pure in-memory data structures for clean memory leak demonstration

## 📊 Memory Leak Patterns

### 1. Growing Collections (Never Shrink)
- `ConcurrentHashMap` primary cache
- `ArrayList` retention list  
- `LinkedList` retention queue
- `HashSet` retention set
- `Map<String, List>` category cache

### 2. Circular Reference Strategy (Pattern C)
- **Circular Chains**: A → B → C → A patterns
- **Bidirectional Links**: Random pairs with mutual references
- **Complex Webs**: Each object references multiple others
- **Cross-References**: New objects link to existing ones

### 3. Heavy Object Design
- Random size: 10KB-100KB per object
- Large byte arrays with random data
- Extensive metadata and string collections
- Multiple data structures per object

## 🚀 Quick Start

### Basic Workflow
```bash
# 1. Build the application
mvn clean package

# 2. Run with memory constraints and GC logging
java -Xms256m -Xmx512m -XX:+UseG1GC -XX:+PrintGC -jar target/memory-leak-app-1.0.0.jar

# 3. Test in another terminal
curl http://localhost:8080/api/health
```

### One-Line Setup
```bash
# Complete setup and run
mvn clean package && java -Xms256m -Xmx512m -XX:+UseG1GC -XX:+PrintGC -jar target/memory-leak-app-1.0.0.jar
```

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/process` | Grow memory by ~1MB with circular references |
| GET | `/api/stats` | View detailed memory and cache statistics |
| POST | `/api/clear` | Clear all caches (for testing) |
| GET | `/api/health` | Health check with memory usage status |

## 🧪 Testing Memory Leaks

### Manual Testing
```bash
# Check initial state
curl http://localhost:8080/api/stats

# Trigger memory growth (repeat multiple times)
curl -X POST http://localhost:8080/api/process

# Monitor memory pressure
curl http://localhost:8080/api/health
```

### Automated Test Scenarios
```bash
# Make test script executable
chmod +x test-scenarios.sh

# Run comprehensive test suite
./test-scenarios.sh

# Run specific tests
./test-scenarios.sh health    # Health check only
./test-scenarios.sh stats     # Statistics only  
./test-scenarios.sh single    # Single growth test
./test-scenarios.sh clear     # Clear caches
```

### Docker Setup (Optional)
```bash
# Create Dockerfile (if not present)
cat > Dockerfile << 'EOF'
FROM openjdk:8-jre-alpine
COPY target/memory-leak-app-1.0.0.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-XX:+UseG1GC", "-XX:+PrintGC", "-jar", "/app.jar"]
EOF

# Build and run with Docker
docker build -t memory-leak-app .
docker run -p 8080:8080 memory-leak-app
```

## 📈 Expected Behavior

### Memory Growth Pattern
- **Initial**: ~45MB heap usage
- **Per Request**: +1MB memory allocation
- **After 10 Requests**: ~55MB+ heap usage
- **GC Activity**: Increasing frequency and duration

### Circular Reference Impact
- Objects become unreachable but not collectible
- Multiple retention strategies prevent GC cleanup
- Cross-references create complex object graphs
- Memory leaks persist across GC cycles

### Performance Degradation
- Response times increase with memory pressure
- GC pauses become more frequent and longer
- Eventually leads to OutOfMemoryError
- Application becomes unresponsive

## 🔧 JVM Tuning for Testing

### Recommended JVM Flags
```bash
# Memory constraints
-Xms256m -Xmx512m

# G1 Garbage Collector with logging
-XX:+UseG1GC -XX:+PrintGC -XX:+PrintGCDetails

# Additional monitoring
-XX:+PrintGCTimeStamps -XX:+PrintGCApplicationStoppedTime

# Heap dump on OOM (optional)
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heap-dump.hprof
```

### Memory Analysis Tools
- **JVisualVM**: Monitor heap usage and GC activity
- **JProfiler**: Analyze object retention and memory leaks
- **Eclipse MAT**: Examine heap dumps for leak analysis
- **Actuator Endpoints**: `/actuator/heapdump` for heap dumps

## 🎪 Advanced Usage

### Stress Testing
```bash
# High-frequency requests
for i in {1..50}; do 
    curl -X POST http://localhost:8080/api/process; 
    sleep 0.1; 
done
```

### Memory Monitoring
```bash
# Watch memory stats in real-time
watch -n 1 'curl -s http://localhost:8080/api/health | jq .'
```

### Heap Analysis
```bash
# Generate heap dump
curl http://localhost:8080/actuator/heapdump > heap-dump.hprof
```

## ⚠️ Important Notes

- **Educational Purpose**: This application is designed for memory leak demonstration
- **Resource Intensive**: Monitor system resources during testing
- **Intentional Leaks**: All memory growth patterns are deliberate
- **Clean Shutdown**: Use `/api/clear` endpoint to reset state before shutdown

## 📋 Statistics Breakdown

The `/api/stats` endpoint provides comprehensive metrics:

```json
{
  "cacheStats": {
    "primaryCacheSize": 50,
    "retentionListSize": 50, 
    "retentionQueueSize": 50,
    "retentionSetSize": 50,
    "categoryCacheSize": 5,
    "totalCategoryObjects": 50
  },
  "memoryStats": {
    "totalMemoryMB": 256,
    "freeMemoryMB": 180,
    "usedMemoryMB": 76,
    "maxMemoryMB": 512
  },
  "circularReferenceStats": {
    "objectsWithReferences": 45,
    "totalConnections": 120,
    "circularChains": 10,
    "totalObjects": 50
  }
}
```

This application effectively demonstrates how memory leaks occur, persist, and impact application performance in a controlled environment.