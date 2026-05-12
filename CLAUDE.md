# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Start local DB (one-time)
docker run -d --name kb-ielts-pg \
  -e POSTGRES_DB=enterprise_kb -e POSTGRES_USER=kb_user \
  -e POSTGRES_PASSWORD=changeme_dev_password \
  -p 5432:5432 postgres:16-alpine

# Run the application (requires SPRING_DATASOURCE_PASSWORD env var or .env file)
mvn spring-boot:run

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=IeltsStudyServiceImplTest

# Run a single test method
mvn test -Dtest=IeltsStudyServiceImplTest#todayPlan_reviewItemsLeadNewItems

# Build fat jar
mvn package

# Full Docker Compose deployment
PG_PASSWORD=changeme docker compose up -d --build
```

App runs on port **8083**. Compose exposes Postgres on **5433** (to avoid conflicts with a local instance).

## Architecture

### Package layout

```
com.enterprise.kb.common          — ApiResponse<T>, PageResponse, shared exceptions
com.enterprise.kb.ielts
  controller/                     — REST endpoints (@RequestMapping("/api/ielts/..."))
  service/                        — interfaces
  service/impl/                   — implementations
  mapper/                         — MyBatis mapper interfaces
  model/                          — entity POJOs (Lombok @Data)
  dto/                            — request/response records
  study/                          — SpacedRepetitionCalculator
  config/                         — IeltsStudyConfig, ClockConfig
  typehandler/                    — UUIDTypeHandler (PostgreSQL UUID ↔ Java UUID)
  exception/                      — IeltsExceptionHandler (@RestControllerAdvice)
src/main/resources/mapper/        — MyBatis XML (one per mapper interface)
src/main/resources/db/changelog/  — Liquibase migrations
src/main/resources/static/        — HTML pages + js/ + css/
```

### Request flow

`Controller` → `Service (interface)` → `ServiceImpl` → `Mapper (interface)` → `Mapper.xml` → PostgreSQL

Every controller method returns `ApiResponse<T>`. Validation errors, `KbException`, and uncaught exceptions are all handled in `IeltsExceptionHandler`.

### Content types

Ten content types share a common pattern: `WORD | PHRASE | PARAPHRASE | PRONUNCIATION | GRAMMAR_POINT | GRAMMAR_EXERCISE | SPEAKING | LISTENING | READING | WRITING`. The constant lives in `ContentTypeConstants.CONTENT_TYPE_PATTERN` and is referenced by `@Pattern` on all request DTOs.

### Spaced repetition

`SpacedRepetitionCalculator` in `study/` has two modes (see `SM2_TYPES`):
- **WORD / PHRASE** — full SM-2 (ease factor, repetition count, interval)
- **everything else** — simplified staged intervals (3/7/14/30+ days), MASTERED when `rep ≥ 4 && interval ≥ 30`

SM-2 state lives in `ielts_study_records`. `ielts_review_logs` stores only rating history (before/after snapshot). The calculator is a Spring bean so tests can inject a fixed `Clock` via `ClockConfig`.

### Today's plan

`IeltsStudyServiceImpl.getTodayPlan()` generates a plan once per day and persists it to `ielts_daily_plan_items` to prevent plan drift on page reload. On subsequent loads it reads from persisted items directly. **The plan is limited to `VOCAB_TYPES = {WORD, PHRASE, PARAPHRASE}`** — all skill types are excluded at the SQL level.

Words with no `ielts_examples` rows are filtered out in both `IeltsWordMapper.findNewContent` and `IeltsStudyRecordMapper.findDueItemsWithSummary` via `EXISTS` subqueries.

### Frontend

No build tool. Static HTML files in `resources/static/` are served by Spring Boot. Bootstrap via CDN. Shared JS lives in `js/api.js` (fetch wrapper), `js/nav.js`, `js/links.js`, `js/backlinks.js`.

`study.html` switches between two rendering modes based on `contentType`:
- `EXERCISE_TYPES = Set(['WORD','PHRASE'])` → `#wordExerciseArea` (active typing exercise)
- all others → `#flipScene` (flip card)

### Database

PostgreSQL 16. All PKs are UUID (`uuid` Postgres type). MyBatis needs `UUIDTypeHandler` and `jdbcType=OTHER` on UUID params in XML. Liquibase runs migrations on startup from `db/changelog/db.changelog-master.xml`.

### Testing patterns

**Controller validation tests** — `MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new IeltsExceptionHandler())`, no Spring context. See `IeltsStudyControllerValidationTest`.

**Service unit tests** — `@ExtendWith(MockitoExtension.class)` + `@InjectMocks` with all mapper dependencies `@Mock`ed. See `IeltsStudyServiceImplTest`.
