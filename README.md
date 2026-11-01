# RepoVision

**AI-powered codebase explorer for understanding GitHub repositories through semantic search and repository-grounded conversations.**

RepoVision connects to GitHub, indexes source code into a vector database, and lets developers explore unfamiliar repositories through natural-language questions. It combines a Spring Boot backend, Next.js frontend, PostgreSQL/pgvector, GitHub OAuth, and Retrieval-Augmented Generation (RAG) into a full-stack developer platform.

## Features

- **GitHub OAuth** — Authenticate with GitHub and securely connect repositories.
- **Repository Sync** — Discover and synchronize repositories through the GitHub REST API.
- **Asynchronous Indexing** — Fetch, filter, chunk, embed, and index repository source code without blocking application requests.
- **Semantic Code Search** — Retrieve repository-specific source context using OpenAI embeddings and pgvector similarity search.
- **RAG Chat** — Ask questions about a repository and receive LLM responses grounded in retrieved source code.
- **Streaming Responses** — Stream generated responses to the frontend using Server-Sent Events (SSE).
- **Persistent Conversations** — Store chat sessions and messages for continued repository exploration.
- **Source Citations** — Associate generated responses with the source files used as retrieval context.
- **Encrypted Credentials** — Encrypt GitHub access tokens before database persistence.
- **Repository Isolation** — Scope vector retrieval to the selected repository using metadata filtering.

---

## Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Spring AI
- PostgreSQL
- pgvector
- GitHub REST API
- OpenAI API
- Maven

### Frontend

- Next.js
- React
- TypeScript
- Tailwind CSS
- TanStack Query

### Infrastructure

- Docker
- Docker Compose
- PostgreSQL 16 + pgvector
- Git / GitHub

---

## Architecture

```text
                         ┌──────────────────────┐
                         │       GitHub         │
                         │ OAuth + Repository   │
                         │        API           │
                         └──────────┬───────────┘
                                    │
                                    ▼
┌─────────────────┐        ┌───────────────────────┐
│                 │  HTTP  │                       │
│     Next.js     │◄──────►│      Spring Boot      │
│     Frontend    │  SSE   │        Backend        │
│                 │        │                       │
└─────────────────┘        └──────┬─────────┬──────┘
                                  │         │
                    ┌─────────────┘         └─────────────┐
                    ▼                                     ▼
          ┌──────────────────┐                  ┌─────────────────┐
          │    PostgreSQL    │                  │     OpenAI      │
          │                  │                  │                 │
          │ Users            │                  │ Embeddings      │
          │ Repositories     │                  │ LLM Generation  │
          │ Chat Sessions    │                  │                 │
          │ Chat Messages    │                  └─────────────────┘
          └────────┬─────────┘
                   │
                   ▼
          ┌──────────────────┐
          │     pgvector     │
          │                  │
          │ Source-code      │
          │ embeddings       │
          └──────────────────┘
```

---

## How It Works

### 1. Authenticate

Users sign in through GitHub OAuth. RepoVision retrieves the authenticated GitHub profile and stores the user locally.

GitHub access tokens are encrypted before being persisted to PostgreSQL.

### 2. Sync Repositories

RepoVision uses the authenticated user's GitHub credentials to retrieve repositories and synchronize repository metadata with the application database.

### 3. Index Source Code

When a repository is indexed, RepoVision runs an asynchronous ingestion pipeline:

```text
GitHub Repository
       │
       ▼
Fetch Repository Tree
       │
       ▼
Filter Supported Source Files
       │
       ▼
Fetch File Contents
       │
       ▼
Split Source into Chunks
       │
       ▼
Generate OpenAI Embeddings
       │
       ▼
Store Vectors + Metadata
       │
       ▼
PostgreSQL / pgvector
```

The indexing pipeline uses configurable limits including:

```properties
app.indexing.max-file-bytes=102400
app.indexing.chunk-size=800
app.indexing.chunk-overlap=100
app.github.api-delay-ms=50
```

Large files are excluded, source text is divided into overlapping chunks, and embeddings are processed in batches to reduce external API overhead.

### 4. Retrieve Relevant Code

When a developer asks a question, RepoVision embeds the query and performs vector similarity search against the selected repository.

Retrieval is metadata-scoped by repository so source code from unrelated repositories cannot enter the context.

The highest-ranking source chunks are assembled into context for the language model.

### 5. Generate a Grounded Answer

```text
Developer Question
        │
        ▼
Query Embedding
        │
        ▼
pgvector Similarity Search
        │
        ▼
Top Repository Context
        │
        ▼
Prompt Construction
        │
        ▼
LLM
        │
        ▼
SSE Response Stream
        │
        ▼
Next.js Chat Interface
```

RepoVision therefore uses the LLM as the generation layer while repository retrieval supplies the codebase-specific knowledge.

---

## Project Structure

```text
RepoVision/
│
├── backend/
│   ├── src/main/java/RepoVision/backend/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── security/
│   │   └── services/
│   │
│   ├── src/main/resources/
│   └── pom.xml
│
├── client/
│   ├── app/
│   ├── components/
│   ├── hooks/
│   ├── lib/
│   ├── public/
│   └── package.json
│
├── docker/
│   └── postgres/
│       └── init-extensions.sql
│
├── docker-compose.yml
└── README.md
```

---

## Local Development

### Prerequisites

Make sure the following are installed:

- Java 21
- Node.js
- npm
- Docker Desktop
- Git

You will also need:

- GitHub OAuth credentials
- OpenAI API credentials

### 1. Clone the Repository

```bash
git clone <repository-url>
cd RepoVision
```

### 2. Start PostgreSQL

RepoVision uses PostgreSQL with the pgvector extension through Docker.

```bash
docker compose up -d
```

Verify the container:

```bash
docker compose ps
```

The local configuration exposes PostgreSQL on:

```text
localhost:5433
```

### 3. Configure the Backend

Configure the required environment variables:

```text
GITHUB_CLIENT_ID
GITHUB_CLIENT_SECRET
OPENAI_API_KEY
TOKEN_ENCRYPTOR_PASSWORD
TOKEN_ENCRYPTOR_SALT
FRONTEND_URL
CORS_ALLOWED_ORIGINS
```

For local development:

```text
FRONTEND_URL=http://localhost:3000
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

The GitHub OAuth redirect URI should point to:

```text
http://localhost:8080/login/oauth2/code/github
```

Never commit OAuth secrets, API keys, encryption passwords, or production credentials to source control.

### 4. Start the Backend

```bash
cd backend
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The API runs locally on port `8080`.

### 5. Start the Frontend

From another terminal:

```bash
cd client
npm install
npm run dev
```

The frontend runs locally on port `3000`.

---

## GitHub OAuth Flow

```text
User
 │
 ▼
RepoVision Login
 │
 ▼
Spring Security
 │
 ▼
GitHub OAuth
 │
 ▼
Authorization Callback
 │
 ▼
Fetch GitHub Profile
 │
 ▼
Encrypt Access Token
 │
 ▼
Persist User
 │
 ▼
Authenticated Dashboard
```

The application requests GitHub access required for user identity and repository operations. Tokens are encrypted before database storage and decrypted only when GitHub API access is required.

---

## RAG Pipeline

RepoVision's core AI functionality follows a Retrieval-Augmented Generation architecture:

```text
Repository
   ↓
Source Files
   ↓
Filtering
   ↓
Chunking
   ↓
Embedding
   ↓
pgvector
   ↓
Similarity Search
   ↓
Top-K Context
   ↓
Prompt
   ↓
LLM
   ↓
Streaming Answer + Sources
```

Each vector is stored alongside repository and source-file metadata, enabling repository-scoped retrieval and source attribution.

This architecture allows RepoVision to answer questions about source code without placing an entire repository inside the model's context window.

---

## Engineering Highlights

RepoVision demonstrates several production-oriented software engineering concepts:

- Full-stack application architecture with a statically typed Java backend and TypeScript frontend
- REST API integration between Next.js, Spring Boot, GitHub, and OpenAI
- OAuth2 authentication and encrypted credential persistence
- Relational data modeling with Spring Data JPA and PostgreSQL
- Vector similarity search with PostgreSQL and pgvector
- Asynchronous source-code ingestion and embedding pipelines
- Batched external API processing
- Repository-scoped semantic retrieval
- Retrieval-Augmented Generation with Spring AI
- Persistent AI chat sessions
- Real-time HTTP streaming through Server-Sent Events
- Dockerized local infrastructure
- Environment-based configuration and secret management

---

## Future Improvements

Potential extensions include:

- Hybrid lexical + vector retrieval
- Cross-encoder reranking
- AST-aware source-code chunking
- Symbol-level code navigation
- Repository dependency graphs
- Multi-turn conversational context optimization
- Incremental indexing based on Git commits
- Background indexing queues
- Automated evaluation suites for retrieval and generation quality
- Repository-level access controls
- Production observability and tracing

---

## Motivation

Understanding an unfamiliar codebase often requires jumping between files, tracing dependencies, searching for symbols, and reconstructing architectural decisions manually.

RepoVision explores a different workflow: connect a repository once, transform its source code into searchable semantic context, and let developers ask questions directly against that context.

The project combines traditional software engineering—authentication, APIs, persistence, asynchronous processing, security, and real-time communication—with modern AI infrastructure such as embeddings, vector search, and Retrieval-Augmented Generation.

---

## Author

**Ananga Tripathee**  
Computer Science @ Truman State University