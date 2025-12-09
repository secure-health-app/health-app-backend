# health-app-backend

## How to run the backend (Spring Boot)

---

### In GitHub Codespaces:
Use this command to start Docker, set Java 17, and run the backend:

```bash
docker compose up -d && export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 && ./mvnw spring-boot:run
```

### Locally:

```bash
docker compose up -d
mvn spring-boot:run
```
