# SmartGuardian Backend

## Overview

This repository contains the Spring Boot backend API for the SmartGuardian system.  
The backend handles authentication, fall alerts, Fitbit health data processing, and communication between the Raspberry Pi device and the frontend dashboard.

The API securely stores health data, processes fall detection alerts, and provides endpoints for the SmartGuardian frontend.

---

## Features

- JWT authentication  
- User registration and login  
- Fall alert API from Raspberry Pi  
- Fitbit health data ingestion  
- Caregiver acknowledgement  
- Secure device authentication  
- PostgreSQL database storage  
- RESTful API endpoints  

## Tech Stack

- Java 17  
- Spring Boot  
- Spring Security  
- JWT Authentication  
- PostgreSQL  
- Docker  
- Maven

## Running the Backend

### 1. Start PostgreSQL

```bash
docker compose up -d
```

### 2. Run backend

```bash
mvn spring-boot:run
```

Server runs on:

http://localhost:8080

### Environment Variables

Create a .env file:

```bash
JWT_SECRET=your_secret
DEVICE_API_KEY=your_device_key
FITBIT_CLIENT_ID=your_client_id
FITBIT_CLIENT_SECRET=your_client_secret
```

### API Overview

| Method | Endpoint | Description |
|--------|---------|-------------|
| POST | /auth/login | User login |
| POST | /alerts | Create fall alert |
| GET | /alerts | Get alert history |
| POST | /device/alert | Raspberry Pi fall detection |
| GET | /fitbit | Fitbit data |

### Database

PostgreSQL database stores:

- Users
- Alerts
- Fitbit metrics
- Devices
- Health records

### Architecture Role

The backend acts as the central communication layer:

- Receives fall alerts from Raspberry Pi
- Retrieves Fitbit health data
- Stores health metrics in PostgreSQL
- Sends processed data to frontend
- Handles authentication and security

### Author

Louise Deeth

BSc (Hons) Software Development

Atlantic Technological University



