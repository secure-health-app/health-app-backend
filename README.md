# SmartGuardian Backend

## Overview

This repository contains the Spring Boot backend API for **SmartGuardian**, a final-year software development project focused on fall detection, wearable health monitoring, and emergency alerting.

The backend acts as the central coordination layer between:

- Raspberry Pi fall detection device  
- React Progressive Web App frontend  
- PostgreSQL database  
- Fitbit Web API integration

It manages authentication, alert workflows, wearable data ingestion, anomaly detection, and secure communication across the platform.

---

## Features

- JWT user authentication  
- User registration and login  
- Secure Raspberry Pi device authentication  
- Fall alert ingestion API  
- Caregiver acknowledgement workflow  
- Fitbit OAuth integration  
- Fitbit heart rate / sleep / activity retrieval  
- Health anomaly detection logic  
- PostgreSQL persistent storage  
- RESTful API architecture  
- Cloud deployment on Render

---

## Tech Stack

- Java 17  
- Spring Boot  
- Spring Security  
- JWT Authentication  
- PostgreSQL  
- Docker  
- Maven
- Render

---

## Local Development Setup

### 1. Start PostgreSQL

```bash
docker compose up -d
```

### 2. Configure Environment Variables

Create a `.env` file:

```bash
JWT_SECRET=your_secret
DEVICE_API_KEY=your_device_key
FITBIT_CLIENT_ID=your_client_id
FITBIT_CLIENT_SECRET=your_client_secret
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/healthdb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password
```

### 3. Run backend

```bash
mvn spring-boot:run
```

Runs locally on:
`http://localhost:8080`

---

## Deployment

Final prototype deployed on Render using HTTPS with a managed PostgreSQL database.

**Example API Endpoints**

| Method | Endpoint | Description |
|--------|---------|-------------|
| POST | /api/auth/signin | User login |
| POST | /api/auth/signup | User registration |
| POST | /api/alerts/fall | Raspberry Pi fall alert |
| GET | /api/alerts | Retrieve alerts |
| GET | /api/fitbit/dashboard | Fitbit dashboard metrics |
| GET | /api/auth/fitbit/connect | Start Fitbit OAuth |

---

## Database Stores

- Users
- Alerts 
- Fitbit summaries
- Device records
- Health metrics

---

## Architecture Role

The backend:

- Receives fall alerts from Raspberry Pi
- Authenticates users and devices
- Retrieves Fitbit data
- Performs anomaly checks
- Stores data in PostgreSQL
- Serves frontend dashboard APIs

---

## Author

Louise Deeth

BSc (Hons) Software Development

Atlantic Technological University



