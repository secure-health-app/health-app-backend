# SmartGuardian Backend

AI-powered health monitoring and fall detection system for elderly care.

## How to run the backend (Spring Boot)

---

### In GitHub Codespaces:
Use this command to start Docker, set Java 17, and run the backend:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
./mvnw clean spring-boot:run
```

**Note:** The app runs in the foreground. To test endpoints, open a new terminal and use curl or Postman.

### Locally:

```bash
docker compose up -d
mvn spring-boot:run
```

**Note:** The app runs in the foreground. To test endpoints, open a new terminal and use curl or Postman.
