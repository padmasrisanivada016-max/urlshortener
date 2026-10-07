# URL Shortener with Analytics

A small service that turns long links into short ones, redirects fast, and counts clicks
(country, device, time).

**Tech:** Java 17, Spring Boot 3, Spring Data JPA, PostgreSQL, Redis, JUnit 5, Mockito, Docker

## Features
- `POST /api/shorten` creates a 6-character short code
- `GET /{code}` redirects (HTTP 302) and records the click in the background
- Redis cache (cache-aside, 1 hour TTL) so repeated redirects skip the database
- Click tracking by country, device type and day, via `GET /api/analytics/{code}`
- Rate limiting with Redis (10 creates/min and 100 redirects/min per IP, returns HTTP 429)

## Run it (only Docker needed)
```bash
docker compose up --build -d
docker compose logs -f app      # wait for "Started UrlshortenerApplication"
```

## Try it
```bash
# 1. Create a short link
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.github.com"}'

# 2. Click it, pretending to be an iPhone user from India (use your own code)
curl -s -o /dev/null \
  -H "User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) Mobile/15E148" \
  -H "CF-IPCountry: IN" \
  http://localhost:8080/aB3xYz

# 3. See the analytics
curl http://localhost:8080/api/analytics/aB3xYz
```

## Run the tests (no Docker needed)
```bash
mvn test
```

## Architecture
```
Client -> RateLimitInterceptor (Redis counters)
       -> UrlController
            -> UrlService --(cache hit)--> Redis
                          --(cache miss)-> PostgreSQL
            -> ClickTrackingService (async) -> PostgreSQL
```

## How the country is found
The app reads the `CF-IPCountry` header that Cloudflare adds to requests. Without it,
the country is saved as `UNKNOWN`.

## Production notes
- Move passwords out of `docker-compose.yml` into a `.env` file or a secrets manager.
- Behind a proxy, read the real client IP from a trusted `X-Forwarded-For` header.

## Future improvements
- Kafka/RabbitMQ for click events (no lost clicks if the app crashes)
- Sliding-window or token-bucket rate limiting
- GeoIP lookup (MaxMind GeoLite2) instead of a header
- Custom aliases and link expiry
- Testcontainers integration tests
