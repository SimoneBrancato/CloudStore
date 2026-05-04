# CloudStore

CloudStore is a full-stack demo e-commerce system composed of a Java backend, a Python Streamlit frontend, and MySQL + Redis infrastructure.

## Quick Start With Docker

### Create your `.env`

From repository root:

```bash
cp .env.example .env
```

Then edit `.env` and set values, especially:

- `JWT_SECRET` must be Base64 and at least 32 bytes after decoding.
- `LLM_SERVICE_URL` must point to the assistant endpoint used by backend (default in compose: `http://llm-assistant:8000/chat`).

Generate a secure secret:

```bash
openssl rand -base64 32
```

Paste it into `JWT_SECRET` in `.env`.

### Build and start all services

```bash
docker compose up -d --build
```

## Customer Shopping Assistant (LLM)

- New container: `llm-assistant` (FastAPI) is started by Docker Compose.
- New free LLM runtime container: `ollama`.
- Backend calls it through `LLM_SERVICE_URL` and exposes customer-only facade method `getCustomerShoppingAdvice`.
- Frontend customer view includes a `Shopping Assistant` tab.

Required Ollama setup in `.env`:

```bash
OLLAMA_BASE_URL=http://ollama:11434
OLLAMA_MODEL=llama3.2:3b
```

Then pull the model once:

```bash
docker compose exec ollama ollama pull llama3.2:3b
```

If model is not available yet, assistant still works with rule-based suggestions.

## Redis Inspection (Keys, Types, TTL)

List keys with type and TTL:

```bash
docker compose exec redis sh -lc 'redis-cli --scan | while read k; do t=$(redis-cli TYPE "$k"); ttl=$(redis-cli TTL "$k"); printf "%-60s | %-8s | TTL=%s\n" "$k" "$t" "$ttl"; done | sort'
```