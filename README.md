# PayFlow — Payment Processing System

A production-grade payment processing system built with Java, Spring Boot, and Apache Kafka — inspired by real-world fintech systems like PayPal and Razorpay.

## Architecture

Client → API Gateway → Payment Service → H2 Database
                              ↓
                         Apache Kafka
                    ↙         ↓          ↘
            Ledger       Notification   Fraud
            Service       Service      Detection

## Tech Stack

- Java 23 + Spring Boot 3.2.5
- Apache Kafka — async event streaming
- Spring Data JPA + H2 Database
- Lombok — boilerplate reduction
- Docker — containerized Kafka + Zookeeper
- Maven — build tool

## Key Features

- REST API for payment initiation and retrieval
- Idempotency key protection — prevents duplicate payments
- Kafka event streaming — payment events published and consumed asynchronously
- Fraud detection — velocity check (blocks sender after 3 payments per minute)
- Notification service — payment success and fraud alerts

## Getting Started

### Prerequisites
- Java 23+
- Maven 3.9+
- Docker Desktop

### Run Kafka
docker compose up -d

### Run the Application
mvn spring-boot:run

### API Endpoints

Create Payment
POST http://localhost:8080/api/v1/payments

Body:
{
    "senderId": "user_001",
    "receiverId": "user_002",
    "amount": 500.00,
    "currency": "INR",
    "idempotencyKey": "txn_001"
}

Get Payment
GET http://localhost:8080/api/v1/payments/{id}

## Design Patterns Used

- Idempotency Pattern — safe retries without duplicate processing
- Event-Driven Architecture — decoupled services via Kafka
- Velocity Check — real-time fraud detection
- Repository Pattern — clean data access layer
- Builder Pattern — via Lombok @Builder

## Author

Anbalagan M — Java Backend Developer
GitHub: https://github.com/anbum2121
