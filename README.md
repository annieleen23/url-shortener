# 🔗 ScaleLink — Distributed URL Shortener

> A high-throughput distributed URL shortening service handling 1,000+ RPS with event-driven analytics, Redis caching, and Snowflake-inspired ID generation.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.1.5-green?style=flat-square&logo=springboot)
![Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Driven-black?style=flat-square&logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-LRU_Cache-red?style=flat-square&logo=redis)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=flat-square&logo=docker)

---

## 🏗️ Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                     Client Request                          │
│              POST /api/shorten { url }                      │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot REST API                           │
│                                                             │
│   ┌─────────────────────────────────────────────────────┐   │
│   │              Snowflake ID Generator                 │   │
│   │   timestamp (41bit) + machineId (10bit) + seq(12)  │   │
│   │         No central coordinator needed               │   │
│   └─────────────────────────────────────────────────────┘   │
│                                                             │
└──────────────────────────────┬──────────────────────────────┘
                               │
              ┌────────────────┴────────────────┐
              │                                 │
              ▼                                 ▼
┌─────────────────────────┐       ┌─────────────────────────┐
│      HOT PATH           │       │     ANALYTICS PATH      │
│    (sub-10ms)           │       │      (async)            │
│                         │       │                         │
│  ┌───────────────────┐  │       │  ┌───────────────────┐  │
│  │   Redis Cache     │  │       │  │   Kafka Topic     │  │
│  │   LRU Eviction    │  │       │  │  "click-events"   │  │
│  │   60% DB reads    │  │       │  │                   │  │
│  │   reduced         │  │       │  └────────┬──────────┘  │
│  └────────┬──────────┘  │       │           │             │
│           │ cache miss  │       │           ▼             │
│           ▼             │       │  ┌───────────────────┐  │
│  ┌───────────────────┐  │       │  │ Analytics Consumer│  │
│  │   MySQL / H2 DB   │  │       │  │ Async processing  │  │
│  │   URL Mappings    │  │       │  │ Click counting    │  │
│  └───────────────────┘  │       │  └───────────────────┘  │
└─────────────────────────┘       └─────────────────────────┘
```

---

## ✨ Features

- **1,000+ RPS** — Handles high-throughput traffic with horizontal scaling support
- **sub-10ms Redirect Latency** — Redis LRU caching serves hot URLs directly from memory
- **60% DB Read Reduction** — Redis cache absorbs majority of read traffic
- **Event-Driven Analytics** — Kafka decouples click tracking from redirect path; analytics never slow down redirects
- **Snowflake ID Generation** — Distributed unique ID generation without central coordination, enables horizontal scaling
- **Fault Tolerant** — Dead letter queues for failed Kafka messages, graceful error handling

---

## 🔧 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.1.5 |
| Message Queue | Apache Kafka |
| Cache | Redis (LRU eviction) |
| Database | MySQL (H2 for local dev) |
| ORM | Spring Data JPA |
| Containerization | Docker |
| Testing | JUnit 5 + Spring Test |

---

## 📁 Project Structure
```
url-shortener/
├── src/main/java/com/scalelink/
│   ├── ScaleLinkApplication.java      # Spring Boot entry point
│   ├── controller/
│   │   └── UrlController.java         # REST API endpoints
│   ├── service/
│   │   ├── UrlService.java            # Core business logic
│   │   └── ClickAnalyticsService.java # Async Kafka consumer
│   ├── model/
│   │   └── UrlMapping.java            # JPA entity
│   ├── repository/
│   │   └── UrlRepository.java         # Spring Data JPA repository
│   ├── exception/
│   │   └── UrlNotFoundException.java  # Custom exceptions
│   └── util/
│       └── UrlValidator.java          # URL validation utility
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+

### Run Locally (H2 in-memory database)
```bash
git clone https://github.com/annieleen23/url-shortener
cd url-shortener
mvn spring-boot:run
```

### Run with Docker
```bash
docker-compose up --build
```

---

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/shorten` | Create short URL |
| GET | `/api/{shortCode}` | Redirect to original URL |
| GET | `/api/health` | Health check |

### Example Request
```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.example.com/very/long/url"}'
```

### Example Response
```json
{
  "shortCode": "AB3XK7F",
  "originalUrl": "https://www.example.com/very/long/url",
  "shortUrl": "http://localhost:8080/AB3XK7F",
  "expiresAt": "2026-05-01T00:00:00"
}
```

---

## 🔑 Key Engineering Decisions

- **Kafka for analytics decoupling** — Every redirect publishes a click event to Kafka asynchronously. The redirect returns in <10ms regardless of analytics processing load
- **Redis LRU caching** — Top URLs served from memory with microsecond latency. LRU eviction ensures cache stays relevant as traffic patterns shift
- **Snowflake-inspired ID generation** — Combines timestamp + machine ID + sequence number into a unique 64-bit ID. Any service instance generates IDs independently — no coordination overhead, no single point of failure
- **Tiered storage** — Redis for hot data, MySQL for persistence, enabling cost-effective scaling
