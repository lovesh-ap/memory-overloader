# Memory Leak Test Application

A Spring Boot application that demonstrates controlled memory leaks for educational and testing purposes.

## � Quick Start

### Prerequisites
- Java 8+ and Maven 3.6+
- 4GB+ RAM recommended

### 1. Build & Run
```bash
# Build the application
mvn clean package

# Start the application (default port: 8080)
java -Xms256m -Xmx512m -XX:+UseG1GC -XX:+PrintGC -jar target/memory-leak-app-1.0.0.jar

# OR run on custom port (e.g., 9090)
java -Xms256m -Xmx512m -XX:+UseG1GC -XX:+PrintGC -Dserver.port=9090 -jar target/memory-leak-app-1.0.0.jar
```

### 2. Test the Memory Leak
```bash
# Check application health
curl http://localhost:8080/api/health

# Trigger memory growth (run multiple times)
curl -X POST http://localhost:8080/api/process

# View memory statistics
curl http://localhost:8080/api/stats

# Clear caches (for testing)
curl -X POST http://localhost:8080/api/clear
```

### 3. Automated Testing
```bash
# Make script executable
chmod +x memory-loop.sh

# Simple continuous testing (1 second intervals)
./memory-loop.sh simple

# Long-term gradual testing (30 second intervals)
./memory-loop.sh slow

# Auto-restart mode (handles crashes automatically)
./memory-loop.sh auto-restart

# View all options
./memory-loop.sh help
```

## 📊 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/process` | Create memory leak objects (~5-15KB) |
| GET | `/api/stats` | View detailed memory statistics |
| POST | `/api/clear` | Clear all caches |
| GET | `/api/health` | Health check with memory usage |

## 🎯 Expected Behavior

- **Memory Growth**: Gradual increase over 3-4 hours
- **GC Activity**: Increasing frequency as memory fills up
- **Eventual Crash**: OutOfMemoryError after sustained load

## ⚠️ Important Notes

- **Educational Purpose**: Designed for memory leak demonstration only
- **Monitor Resources**: Watch system memory during testing
- **Clean Shutdown**: Use `/api/clear` before stopping the application

---

**Quick Demo:**
```bash
mvn clean package && java -Xms256m -Xmx512m -XX:+UseG1GC -jar target/memory-leak-app-1.0.0.jar &
sleep 10 && ./memory-loop.sh slow
```
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

This application effectively demonstrates how memory leaks occur, persist, and impact application performance in a controlled environment.