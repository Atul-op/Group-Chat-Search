# Group Chat Semantic Search

A Spring Boot application that demonstrates **semantic (vector) search** over a simulated WhatsApp-style group chat. Instead of matching keywords, it embeds every message with Google's Gemini embedding model and stores the vectors in PostgreSQL using the **pgvector** extension, so you can search by *meaning* (e.g. searching "planning a trip" finds a message about fixing a Manali trip even if it never says the word "trip").

The chat is themed as a fictional college WhatsApp group ("SGSITS Canteen Mafia") with Hinglish filler messages plus a few realistic "needle in the haystack" messages that searches are designed to surface.

---

## How It Works

1. **Data generation** — `DataGenerationService` wipes the table and generates ~500 messages: mostly random Hinglish filler chatter, plus 5 specific "needle" messages (trip planning, project work, hackathon, interview prep, lab schedule). Each message is embedded via the Gemini embedding model and saved to Postgres.
2. **Storage** — Each `ChatMessage` row stores the sender, timestamp, text content, and a `vector(3072)` embedding column (pgvector).
3. **Search** — `SearchService` embeds the incoming query with the same model, then runs a cosine-distance nearest-neighbor query (`<=>` operator) against the stored vectors. It also does lightweight query parsing: if the query mentions a known user's name, it filters by sender; if it says "recent" or "last month", it filters to the last 30 days.
4. **Frontend** — A single static page (`index.html` + `app.js` + `style.css`) renders the full chat like a WhatsApp thread and provides a search sidebar. Typing a query and hitting Enter shows the top semantic matches; clicking a result scrolls to and highlights that message in the chat.

---

## Project Structure

```
src/main/java/com/atul/groupchatsearch/
├── GroupChatSearchApplication.java        # Spring Boot entry point
├── config/
│   └── GeminiEmbeddingConfig.java         # Custom Spring AI EmbeddingModel backed by Gemini's embedContent API
├── controller/
│   ├── ChatUIController.java              # Forwards "/" to the static index.html
│   └── SearchRestController.java          # REST API: generate-data, search, messages
├── dto/
│   ├── SearchRequest.java                 # { query, limit } request body
│   └── SearchResult.java                  # Response shape sent to the frontend (no raw vectors)
├── entity/
│   └── ChatMessage.java                   # JPA entity mapped to chat_messages table (incl. pgvector column)
├── repository/
│   └── ChatMessageRepository.java         # JPA + native pgvector queries (plain, by-sender, by-time-range)
└── service/
    ├── DataGenerationService.java         # Generates & embeds the simulated chat history
    └── SearchService.java                 # Embeds queries, routes to the right repository query, maps to DTOs

src/main/resources/
├── application.properties                 # DB connection, JPA/Hibernate settings
├── schema.sql                             # Creates the pgvector extension + chat_messages table
└── static/
    ├── index.html                         # Chat UI shell
    ├── app.js                             # Fetches messages, handles search, init-data button
    └── style.css                          # WhatsApp-style styling

docker-compose.yml                         # Spins up a Postgres instance with the pgvector extension
```

### REST API

| Method | Endpoint             | Description                                           |
|--------|-----------------------|--------------------------------------------------------|
| POST   | `/api/generate-data`  | Clears the table and generates + embeds ~500 messages |
| POST   | `/api/search`         | Body: `{ "query": "...", "limit": 5 }` — semantic search |
| GET    | `/api/messages`       | Returns all messages in chronological order            |

---

## Prerequisites

- **Java 17+** (Spring Boot 3.x requires it)
- **Maven** (or use the included `mvnw` wrapper, if present)
- **Docker** and **Docker Compose** (for the pgvector-enabled Postgres instance)
- A **Google Gemini API key** with access to the embedding model (`gemini-embedding-001`)

---

## Setup & Run

### 1. Start the database

```bash
docker compose up -d
```

This starts a `pgvector/pgvector:pg16` Postgres container on port `5432` with:
- DB: `chat_db`
- User: `chat_user`
- Password: `chat_password`

The `vector` extension and `chat_messages` table are created automatically on app startup via `schema.sql` (`spring.sql.init.mode=always`).

### 2. Add your Gemini API key

Open `src/main/java/com/atul/groupchatsearch/config/GeminiEmbeddingConfig.java` and replace the placeholder:

```java
Client client = Client.builder().apiKey("GEMINI API KEY HERE").build();
```

with your real key. For anything beyond local experimentation, pull this from an environment variable or `application.properties` instead of hardcoding it in source.

### 3. Build and run the app

```bash
mvn spring-boot:run
```

or build a jar and run it:

```bash
mvn clean package
java -jar target/*.jar
```

The app starts on **http://localhost:8080**.

### 4. Use it

1. Open `http://localhost:8080` in a browser.
2. Click **Init Data** to generate and embed the simulated chat history (this calls the Gemini embedding API ~500 times, so it may take a bit).
3. Once loaded, type a natural-language query into the search box (e.g. "trip planning", "interview questions", "hackathon deadline") and press **Enter**.
4. Click a search result to jump to and highlight that message in the chat window.

---

## Notes

- The embedding dimension is set to **3072** throughout (entity column, pgvector schema, `dimensions()` in the embedding config) to match `gemini-embedding-001`. If you swap embedding models, update the dimension in all three places (`schema.sql`, `ChatMessage.java`'s `@Array(length = ...)`, and `GeminiEmbeddingConfig.dimensions()`).
- `spring.jpa.hibernate.ddl-auto=none` is intentional — the schema is managed entirely by `schema.sql` because Hibernate's auto-DDL doesn't know how to create a `vector` column.
- The two Google GenAI Spring AI auto-configurations are explicitly excluded in `GroupChatSearchApplication` so the app uses the custom `GeminiEmbeddingConfig` bean instead of the library's default embedding auto-configuration.
