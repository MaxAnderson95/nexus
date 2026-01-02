# AGENTS.md - Coding Agent Guidelines for NEXUS Station

## Project Overview

Microservices-based space station management system demonstrating OpenTelemetry distributed tracing.

- **Backend**: Java 17, Spring Boot 3.4.1
- **Frontend**: React 18, TypeScript, Vite, Tailwind CSS
- **Database**: PostgreSQL 16 with schema-per-service
- **Cache**: Redis 7

## Build & Test Commands

### Java Services (Maven)

```bash
# Build a single service (from service directory)
./mvnw clean package

# Build without tests
./mvnw package -DskipTests

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=PowerServiceTest

# Run a single test method
./mvnw test -Dtest=PowerServiceTest#testAllocatePower

# Run tests matching pattern
./mvnw test -Dtest=*Controller*
```

### Frontend (npm)

Use NVM to get access to the correct Node version (v20.x):

```bash
cd cortex/frontend

npm install          # Install dependencies
npm run dev          # Development server (port 3000)
npm run build        # Production build (tsc && vite build)
npm run lint         # ESLint check
npm run preview      # Preview production build
```

### Docker Compose

```bash
docker compose up -d --build              # Start all services
docker compose --profile load up -d       # Include load generator
docker compose logs -f <service>          # View logs
docker compose down                        # Stop all services
```

## Code Style Guidelines

### Java

**Package Structure:**

```
com.nexus.<service>/
├── client/          # REST clients for other services
├── config/          # Spring configuration classes
├── controller/      # REST controllers
├── dto/             # Data Transfer Objects (use records)
├── entity/          # JPA entities
├── repository/      # Spring Data repositories
├── service/         # Business logic
└── <Service>Application.java
```

**Naming Conventions:**

- Controllers: `*Controller` (e.g., `CrewController`)
- Services: `*Service` (e.g., `CrewService`)
- Repositories: `*Repository` (e.g., `CrewMemberRepository`)
- Entities: Singular nouns (e.g., `CrewMember`)
- DTOs: `*Dto`, `*Request`, `*Response`, `*Summary`
- Clients: `*Client` (e.g., `PowerClient`)

**DTOs - Use Java Records:**

```java
public record CrewMemberDto(
    Long id,
    String name,
    String rank,
    String status
) {
    public static CrewMemberDto fromEntity(CrewMember entity) {
        return new CrewMemberDto(entity.getId(), ...);
    }
}
```

**Exception Handling:**

```java
// Define as static inner classes in Service
public static class CrewNotFoundException extends RuntimeException {
    public CrewNotFoundException(String message) { super(message); }
}

// Handle in Controller with @ExceptionHandler
@ExceptionHandler(CrewService.CrewNotFoundException.class)
public ResponseEntity<Map<String, String>> handleNotFound(...) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
}
```

**Dependency Injection:**

- Use constructor injection (no `@Autowired` on constructors)
- Use `@Value` for configuration properties

**Logging:**

```java
private static final Logger log = LoggerFactory.getLogger(ClassName.class);
```

### TypeScript/React

**Import Order:**

1. React hooks (`useState`, `useEffect`)
2. Local imports (api, types)
3. Type imports with `type` keyword

**Component Pattern:**

```typescript
import { useState, useEffect } from 'react';
import { api } from '../api/client';
import type { CrewMember } from '../types';

function Crew() {
  const [data, setData] = useState<CrewMember[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => { loadData(); }, []);

  // Use 'init' parameter for silent background updates (preventing full reload/skeleton)
  async function loadData(init = true) {
    try {
      if (init) setLoading(true);
      setError(null);
      const result = await api.crew.getRoster();
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load');
    } finally {
      if (init) setLoading(false);
    }
  }

  // Example: Silent refresh after action
  async function handleAction() {
    await api.crew.updateMember();
    await loadData(false); // No loading spinner
  }

  // ... render
}
```

**Styling:**

- **Theme:** Cinematic Sci-Fi (Dark mode default)
- **Framework:** Tailwind CSS + Framer Motion (animations) + Lucide React (icons)
- **Colors:** Custom 'space' palette (deep blues/blacks), Cyan/Emerald/Red accents for status
- **Typography:** 'Rajdhani' (headings), 'Share Tech Mono' (data/code)
- **Components:** Use `Card` from `components/ui/Card.tsx` for consistency

## Architecture Patterns

### Service Communication

- Services communicate via REST through CORTEX (BFF)
- Use `RestClient` for service-to-service calls
- Graceful degradation: warn and continue on non-critical failures

### Error Handling Strategy

```java
// Critical operations - fail fast
try {
    powerClient.allocate(...);
} catch (Exception e) {
    return Result.failure("Power allocation failed");
}

// Non-critical - warn and continue
try {
    lifeSupportClient.notify(...);
} catch (Exception e) {
    log.warn("Notification failed: {}", e.getMessage());
}
```

### Chaos Engineering

Chaos settings affect all endpoints via `ChaosInterceptor`:

- `none`: 0% errors, 0ms latency
- `low`: 5% errors, 200-500ms latency
- `medium`: 15% errors, 1-3s latency
- `high`: 30% errors, 3-8s latency

Set via environment: `CHAOS_DEFAULT=low` or per-service: `POWER_CHAOS=high`

## Service Ports

| Service | Port |
|---------|------|
| CORTEX (BFF + Frontend) | 8080 |
| Docking | 8080 |
| Crew | 8080 |
| Life Support | 8080 |
| Power | 8080 |
| Inventory | 8080 |

## Service Dependencies

| Service | Depends On | Purpose |
|---------|------------|---------|
| Power | - | Base service (no dependencies) |
| Life Support | Power | Power allocation for environmental systems |
| Crew | Life Support | Section capacity adjustments when crew relocates |
| Docking | Power, Crew | Bay power allocation, crew notifications |
| Inventory | Docking, Crew | Cargo manifest linking, crew for cargo handling |

## Important Files

- `docker-compose.yml` - Local development environment
- `cortex/frontend/src/types/index.ts` - Frontend type definitions
- `cortex/frontend/src/api/client.ts` - API client
- `helm/nexus-station/values.yaml` - Kubernetes configuration
- `load-generator/locustfile.py` - Load testing scenarios

## Common Tasks

### Adding a New Endpoint

1. Add method to service class
2. Add endpoint to controller
3. Add client method in CORTEX
4. Add proxy endpoint in CORTEX controller
5. Add API method in frontend client
6. Add type definitions if needed
7. Update load generator if interactive

### Adding Interactive UI Feature

1. Add state for modal/loading
2. Add handler function with try/catch
3. Add UI components (button, modal)
4. Update load generator to test the feature
