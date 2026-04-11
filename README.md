# PulseDesk 🎯

An AI-powered support ticket triage system built with Java, Spring Boot, and Vaadin. PulseDesk collects user comments and automatically analyzes them using an AI API to decide whether a comment should become a support ticket — and if so, generates structured ticket data.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup & Installation](#setup--installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Using the Application](#using-the-application)
- [REST API](#rest-api)
- [Database](#database)
- [AI Integration](#ai-integration)
- [Troubleshooting](#troubleshooting)

---

## Overview

PulseDesk is a fictional internal platform that collects user feedback from different channels. It automatically triages incoming comments using AI:

- Users submit comments via the web UI or REST API
- Each comment is analyzed by an AI model
- If the comment describes a real issue, a support ticket is created with:
    - A generated **title**
    - A **category** (`BUG`, `FEATURE`, `BILLING`, `ACCOUNT`, `OTHER`)
    - A **priority** (`LOW`, `MEDIUM`, `HIGH`)
    - A short **summary**
- All comments and tickets are stored in an H2 in-memory database
- A Vaadin dashboard displays all tickets in real time

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.x |
| UI | Vaadin 25 |
| Database | H2 (in-memory) |
| ORM | Spring Data JPA / Hibernate |
| AI | Groq API (OpenAI-compatible) |
| Build | Maven |

---

## Project Structure

```
src/main/java/lt/linas_puplauskas/
├── Application.java               # Spring Boot entry point
├── MainView.java                  # Vaadin UI (comment form + ticket grid)
├── controller/
│   ├── CommentController.java     # REST: POST /comments, GET /comments
│   └── TicketController.java      # REST: GET /tickets, GET /tickets/{id}
├── model/
│   ├── Comment.java               # Comment entity
│   ├── Ticket.java                # Ticket entity
│   ├── TicketCategory.java        # Enum: BUG, FEATURE, BILLING, ACCOUNT, OTHER
│   ├── TicketPriority.java        # Enum: LOW, MEDIUM, HIGH
│   └── dto/
│       └── AIAnalysisResult.java  # DTO for AI response parsing
├── repository/
│   ├── CommentRepository.java     # JPA repository for comments
│   └── TicketRepository.java      # JPA repository for tickets
└── service/
    └── GroqService.java           # AI analysis via Groq API
```

---

## Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- A free **Groq API key** — sign up at [console.groq.com](https://console.groq.com)

---

## Setup & Installation

**1. Clone the repository**

```bash
git clone https://github.com/your-username/pulse-desk.git
cd pulse-desk
```

**2. Get a Groq API key**

- Go to [console.groq.com](https://console.groq.com)
- Sign up or log in
- Navigate to **API Keys** → **Create API Key**
- Copy the key (starts with `gsk_`)

**3. Configure the API key**

Open `src/main/resources/application.properties` and set:

```properties
groq.api.key=gsk_your_key_here
```

**4. Install dependencies**

```bash
mvn clean install -DskipTests
```

---

## Configuration

Full `application.properties` reference:

```properties
# Server
server.port=${PORT:8080}

# Vaadin
vaadin.launch-browser=true
vaadin.exclude-urls=/h2-console/**
logging.level.org.atmosphere=warn
spring.mustache.check-template-location=false
spring.main.allow-bean-definition-overriding=true

# H2 Database
spring.datasource.url=jdbc:h2:mem:pulse-desk;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Groq AI
groq.api.key=gsk_your_key_here

```

---

## Running the Application

```bash
mvn spring-boot:run
```

The app will start and automatically open your browser at:

```
http://localhost:8080
```

---

## Using the Application

### Web UI

The Vaadin dashboard has two sections:

1. **Submit a Comment** — type any user feedback in the text area and click Submit
2. **Tickets** — a live grid showing all tickets that were generated from comments

After submitting a comment:
- If the AI determines it is actionable, a ticket is created and appears in the grid immediately
- If it is just a compliment or general feedback, a notification confirms it was saved but no ticket was created

**Example comments to try:**

| Comment | Expected result |
|---|---|
| `"I was charged twice for my subscription this month"` | Ticket created — BILLING, HIGH |
| `"The login button doesn't work on mobile"` | Ticket created — BUG, HIGH |
| `"It would be great to have dark mode"` | Ticket created — FEATURE, LOW |
| `"Great app, love the new design!"` | No ticket — compliment |

---

## REST API

### Submit a comment

```http
POST /comments
Content-Type: application/json

"The app crashes every time I try to upload a file"
```

### Get all comments

```http
GET /comments
```

### Get all tickets

```http
GET /tickets
```

### Get a ticket by ID

```http
GET /tickets/{id}
```

#### Example ticket response

```json
{
  "id": 1,
  "title": "App crashes on file upload",
  "category": "BUG",
  "priority": "HIGH",
  "summary": "User reports the application crashes when attempting to upload a file.",
  "createdAt": "2026-04-10T14:30:00",
  "comment": {
    "id": 1,
    "content": "The app crashes every time I try to upload a file",
    "createdAt": "2026-04-10T14:30:00"
  }
}
```

You can test all endpoints using the built-in IntelliJ HTTP client or any REST tool like Postman or curl.

---

## Database

PulseDesk uses H2 in-memory database. Data is reset every time the application restarts.

### Viewing the database

The H2 web console is available while the app is running at:

```
http://localhost:8082
```

> The H2 console runs on port **8082**, separate from the app on **8080**.

Connect using:

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:tcp://localhost:9092/mem:pulse-desk` |
| Username | `sa` |
| Password | *(leave empty)* |

### Schema

**COMMENT table**

| Column | Type | Notes |
|---|---|---|
| ID | BIGINT | Primary key, auto-generated |
| CONTENT | VARCHAR(1000) | The original comment text |
| CREATED_AT | TIMESTAMP | Set on creation |

**TICKET table**

| Column | Type | Notes |
|---|---|---|
| ID | BIGINT | Primary key, auto-generated |
| TITLE | VARCHAR | AI-generated title |
| CATEGORY | VARCHAR | BUG / FEATURE / BILLING / ACCOUNT / OTHER |
| PRIORITY | VARCHAR | LOW / MEDIUM / HIGH |
| SUMMARY | VARCHAR(500) | AI-generated summary |
| CREATED_AT | TIMESTAMP | Set on creation |
| COMMENT_ID | BIGINT | Foreign key to COMMENT |

---

## AI Integration

PulseDesk uses the **Groq API** with the `llama-3.3-70b-versatile` model.

The AI receives a structured prompt for each comment and is instructed to respond in JSON format only. The response is parsed into an `AIAnalysisResult` DTO and used to decide whether to create a ticket.

**Prompt structure:**

```
You are a strict JSON generator.
Convert the user message into a ticket JSON with this structure:
{
  "shouldBeTicket": boolean,
  "title": string,
  "summary": string,
  "category": "BUG" | "FEATURE" | "BILLING" | "ACCOUNT" | "OTHER",
  "priority": "LOW" | "MEDIUM" | "HIGH"
}
Rules:
- NEVER return null values
- Always fill all fields
- If unsure, guess reasonable values
- Output ONLY JSON
```

To switch AI models, update `application.properties`:

```properties
# Other available Groq models (all free):
groq.api.model=llama-3.1-8b-instant       # faster, lighter
groq.api.model=mixtral-8x7b-32768         # good at structured output
groq.api.model=llama-3.3-70b-versatile    # best quality (default)
```

---

## Troubleshooting

**App won't start — bean definition conflict**

Add to `application.properties`:
```properties
spring.main.allow-bean-definition-overriding=true
```

**H2 console not accessible**

Make sure the app is running, then go to `http://localhost:8082` directly (not from within the Vaadin app). The H2 TCP server must be started as a Spring bean.

**AI always returns "no ticket needed"**

Check the console logs for `=== RAW AI RESPONSE ===`. If it is empty or malformed, verify your Groq API key is correct in `application.properties`.

**Vaadin UI not loading**

Run `mvn clean install` to rebuild the frontend bundle, then restart.

---

## Built for the IBM Internship Technical Challenge