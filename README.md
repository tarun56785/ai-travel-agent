# Autonomous Global Event & Travel Concierge

An enterprise-grade, event-driven AI travel agent built with Spring Boot, React, and Google Vertex AI. This application features a secure, Zero-Trust architecture, real-time streaming LLM responses, and autonomous tool-calling to live global APIs.

## Project Overview

This project goes beyond a standard ChatGPT wrapper by implementing a true AI Agent architecture. The AI has persistent memory, the ability to autonomously trigger backend Java methods, and the authorization to reach out to live B2B internet APIs to gather real-world data before responding to the user.

## Core Architecture & Data Flow

### 1. The Reactive UI & Security Gate (Frontend)
The user interacts with a custom Glassmorphic React dashboard, governed by a centralized Zustand state manager. Before the user can access the system, they are routed through a secure Keycloak OAuth2 login screen (Authorization Code Flow). Once authenticated, the React app holds a JWT (JSON Web Token) to prove its identity.

### 2. The AI Brain & Live Senses (Backend)
When the user sends a message, React opens a Server-Sent Events (SSE) stream to the Spring Boot backend, passing the JWT.
* **Validation:** Spring Boot strictly validates the token's Issuer (`iss`) and signatures.
* **Memory:** The `ChatMemoryAdvisor` intercepts the request, retrieves the specific user's conversation history, and injects it into the prompt to cure the AI's "goldfish memory."
* **Execution:** The request is sent to Google Vertex AI. If the user asks about weather or flights, Vertex AI autonomously triggers Spring Boot `@Tool` methods.
* **B2B Integration:** For flights, Spring Boot executes an OAuth2 Client Credentials handshake with the Amadeus Enterprise API to fetch live Euro/USD pricing. It fetches live weather via the OpenWeather REST API.
* **Streaming:** The final, context-aware response is streamed word-by-word back to the React UI, instantly updating the interactive Leaflet map.

### 3. Event-Driven Fulfillment (Infrastructure)
When the AI finalizes a trip, it does not lock up the user's chat interface. Instead, it fires an asynchronous message into an **Apache Kafka** topic. A separate Spring Boot worker listens to this topic, picks up the ticket, and saves the finalized itinerary permanently into a **PostgreSQL** database via Spring Data JPA.

### 4. Precision Data Fetching (GraphQL)
To populate the user's "Saved Itineraries" dashboard, the React frontend bypasses standard REST endpoints and queries a custom **GraphQL** controller. This completely eliminates "over-fetching" by allowing the UI to request only the specific database columns it needs to render the glassmorphic cards.

## Tech Stack

* **Frontend:** React, Vite, Material UI (Glassmorphism), React-Leaflet, Zustand
* **Backend:** Java 21, Spring Boot 3, Spring AI, Spring Security, Spring Data JPA, GraphQL
* **Infrastructure:** Docker Compose, PostgreSQL, Apache Kafka, Keycloak
* **External APIs:** Google Vertex AI, OpenWeatherMap, Amadeus Flight Search

## Getting Started (Local Development)

### 1. Infrastructure Bootup
Ensure Docker is running, then spin up the required database and messaging services:
```bash
docker-compose up -d