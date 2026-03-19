# ScaleLink — Distributed URL Shortener

A high-throughput distributed URL shortening service with an event-driven analytics pipeline built with Java, Spring Boot, and Apache Kafka.

## Tech Stack

- **Backend:** Java, Spring Boot
- **Messaging:** Apache Kafka
- **Cache:** Redis (LRU eviction)
- **Database:** MySQL
- **Infrastructure:** Docker, AWS EC2

## Features

- High-concurrency short link generation using Snowflake-inspired ID algorithm
- Event-driven click analytics pipeline via Apache Kafka (decoupled from redirect serving)
- Redis LRU caching reducing database read load by ~60%
- Sub-10ms average redirect latency under concurrent load
- TTL-based link expiration management
- RESTful APIs for link creation, redirect, and real-time analytics

## Architecture
```
Client → Spring Boot API → Redis Cache → MySQL
                    ↓
              Kafka Producer
                    ↓
         Kafka Consumer → Analytics DB
```

## Getting Started
```bash
git clone https://github.com/annieleen23/url-shortener.git
cd url-shortener
docker-compose up
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/shorten` | Create short link |
| GET | `/{shortCode}` | Redirect to original URL |
| GET | `/api/analytics/{shortCode}` | Get click analytics |
| DELETE | `/api/links/{shortCode}` | Delete a link |

## Deployment

Containerized with Docker and deployed to AWS EC2 with health checks and auto-restart policies.
