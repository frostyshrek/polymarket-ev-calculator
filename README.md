# UFC Polymarket +EV Research Platform

A Java 21, Spring Boot, PostgreSQL research application for identifying and evaluating potentially positive expected value opportunities between UFC sportsbook moneyline odds and Polymarket prices.

The application ingests market data, matches sportsbook and prediction-market outcomes, calculates expected value using a versioned strategy, places simulated paper bets, records official results, settles completed events, and exposes the workflow through an interactive console.

> This project is for research, simulation, and software experimentation. It does not place real wagers and does not provide financial or betting advice.

---

## What the application does

The operational workflow is:

1. Ingest sportsbook markets and odds.
2. Ingest Polymarket markets and order-book snapshots.
3. Match equivalent UFC markets and fighter outcomes.
4. Review uncertain mappings manually.
5. Calculate and persist qualified or rejected opportunities.
6. Place one-unit simulated paper bets for qualified opportunities.
7. Record final event resolutions.
8. Settle open paper bets as wins, losses, or voids.

The primary expected-value calculation is:

```text
estimated EV = reference probability × sportsbook decimal odds − 1
```

The initial strategy uses the Polymarket best bid as the reference probability.

---

## Technology stack

- Java 21
- Maven multi-module build
- Spring Boot 3.5.x
- PostgreSQL
- Flyway database migrations
- Spring JDBC with `NamedParameterJdbcTemplate`
- Java `HttpClient`
- Jackson
- JUnit 5
- Testcontainers
- Docker Compose

---

## Repository modules

| Module | Responsibility |
|---|---|
| `domain` | Shared domain types and enums |
| `calculation-engine` | Probability, implied probability, and EV calculations |
| `odds-client` | Sportsbook or odds-provider API integration |
| `polymarket-client` | Polymarket Gamma and CLOB API integration |
| `persistence` | JDBC repositories and persistence records |
| `event-matching` | Participant, event, market, and outcome matching |
| `strategy-engine` | Strategy rules, qualification, and validation |
| `paper-betting` | Simulated paper-bet placement and duplicate protection |
| `settlement` | Result resolution and paper-bet settlement calculations |
| `reporting` | Research and performance reporting |
| `kelly-simulation` | Alternative bankroll and Kelly-staking simulations |
| `console-ui` | Spring Boot application and interactive console |

---

## Prerequisites

Install the following before running the project:

- JDK 21
- Maven 3.9 or later
- Docker Desktop or another working Docker engine
- Git

Verify the installations:

```powershell
java -version
mvn -version
docker version
docker info
```

The Java and Maven output should show Java 21.

---

## Clone the repository

```powershell
git clone <your-repository-url>
cd polymarket-ev-calculator
```

---

## Start PostgreSQL

Start the database from the repository root:

```powershell
docker compose up -d postgres
```

Check the container:

```powershell
docker compose ps
```

Open a PostgreSQL shell:

```powershell
docker compose exec postgres psql -U ufc_study -d ufc_study
```

List the application tables:

```sql
\dt ufc_study.*
```

Exit `psql`:

```sql
\q
```

Flyway applies the schema automatically when the Spring Boot application starts.

---

## Build the project

Compile all modules:

```powershell
mvn clean compile
```

Compile the console application and all required modules:

```powershell
mvn clean compile -pl console-ui -am
```

Run the full test suite:

```powershell
mvn clean verify
```

The integration tests use Testcontainers and therefore require a working Docker engine.

For a compile-only check when Docker is unavailable:

```powershell
mvn clean compile -pl console-ui -am
```

Do not treat skipped integration tests as a permanent replacement for a working Docker setup.

---

## Run the console application

From the repository root:

```powershell
mvn -pl console-ui spring-boot:run
```

The interactive menu exposes the operational pipeline:

```text
1. Run sportsbook ingestion
2. Run Polymarket ingestion
3. Run automated matching
4. Review mappings
5. Calculate opportunities
6. Place qualified paper bets
7. Record resolutions
8. Settle completed events
```

Follow the confirmation prompts shown by each operation.

---

## Configuration

Application configuration is stored in the `console-ui` Spring Boot configuration files and may be overridden with environment variables.

### Polymarket configuration

```yaml
ufc-study:
  polymarket:
    enabled: ${POLYMARKET_INGESTION_ENABLED:false}
    gamma-base-url: ${POLYMARKET_GAMMA_BASE_URL:https://gamma-api.polymarket.com}
    clob-base-url: ${POLYMARKET_CLOB_BASE_URL:https://clob.polymarket.com}
    event-slug: ${POLYMARKET_EVENT_SLUG:}
```

Example PowerShell environment variables:

```powershell
$env:POLYMARKET_INGESTION_ENABLED = "true"
$env:POLYMARKET_EVENT_SLUG = "your-event-slug"
```

### Sportsbook configuration

Set the sportsbook or odds-provider properties required by the implementation in `console-ui` before enabling ingestion. The console validates required settings immediately before running the sportsbook ingestion command.

Typical values include:

```powershell
$env:SPORTSBOOK_INGESTION_ENABLED = "true"
$env:SPORTSBOOK_API_KEY = "your-api-key"
```

Use the exact property names defined by `SportsbookOperationProperties` in the project.

### Database configuration

Use the JDBC settings defined by the repository's Spring configuration or Docker Compose file. A typical local configuration is:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ufc_study
    username: ufc_study
    password: ufc_study
```

Do not commit production credentials or API keys.

---

## Default strategy

The initial database migration creates an active strategy similar to:

| Setting | Value |
|---|---:|
| Strategy code | `UFC_EV` |
| Version | `1.0` |
| Probability method | `BEST_BID` |
| Minimum EV | `0.05` |
| Maximum Polymarket spread | `0.04` |
| Maximum snapshot age | `60` seconds |
| Maximum source timestamp gap | `60` seconds |
| Minimum time before event | `1800` seconds |
| Primary staking method | `FLAT_ONE_UNIT` |
| Required settlement compatibility | `EXACT` |

The strategy is stored in `ufc_study.strategy_version`. Opportunity calculation reads the active database row instead of hard-coding the thresholds.

Inspect the current strategy:

```sql
SELECT *
FROM ufc_study.strategy_version
ORDER BY created_at DESC;
```

---

## Detailed workflow

### 1. Sportsbook ingestion

The sportsbook ingestion operation should:

- fetch UFC events and moneyline odds;
- record the ingestion run and raw payload;
- upsert participants and sporting events;
- create source event references;
- create source markets and outcomes;
- append immutable sportsbook odds snapshots.

Primary tables:

```text
ingestion_run
raw_source_payload
sporting_event
participant
event_participant
source_event_reference
source_market
source_market_outcome
sportsbook_odds_snapshot
```

If the runner is still configured as a temporary adapter, the menu reports that live sportsbook ingestion is not connected. Replace the temporary runner bean with the real odds-client integration.

### 2. Polymarket ingestion

The Polymarket operation should:

- fetch the configured UFC event from Gamma;
- fetch order-book data from CLOB;
- create or update source references, markets, and outcomes;
- append snapshots containing best bid, best ask, midpoint, and spread;
- record raw payloads and ingestion metadata.

Primary table:

```text
prediction_market_snapshot
```

### 3. Automated matching

Automated matching considers eligible pre-fight moneyline markets and pairs sportsbook and Polymarket markets associated with the same canonical sporting event.

It scores:

- participant-name compatibility;
- scheduled-time compatibility;
- overall confidence.

Results are stored in:

```text
automated_match_run
automated_match_candidate
```

Possible candidate statuses include:

```text
AUTO_APPROVED
REVIEW_REQUIRED
REJECTED
SUPERSEDED
```

### 4. Mapping review

Manual review displays pending candidates and the two participant outcomes on each market.

Approving a two-fighter market creates two `market_mapping` rows, one per outcome pair. Manual approvals use:

```text
mapping_status = APPROVED_MANUAL
settlement_compatibility = EXACT
manually_approved = true
```

The candidate's `created_mapping_id` points to the first created mapping as an audit anchor.

### 5. Opportunity calculation

The calculator loads:

- approved exact mappings;
- the latest sportsbook snapshot for the mapped sportsbook outcome;
- the latest Polymarket snapshot for the mapped prediction outcome;
- the active strategy version.

It records both qualified and rejected calculations in:

```text
opportunity
```

A calculation may be rejected for reasons such as:

- stale sportsbook snapshot;
- stale Polymarket snapshot;
- excessive source timestamp gap;
- event too close to starting;
- live or suspended sportsbook market;
- Polymarket spread too wide;
- EV below the strategy threshold.

Duplicate calculations for the same strategy, mapping, and snapshot pair are prevented by a database constraint.

### 6. Paper-bet placement

Only `QUALIFIED` opportunities can create paper bets.

The primary paper-betting service enforces:

- approved mappings only;
- exact settlement compatibility;
- valid odds and probability values;
- flat one-unit stakes;
- one paper bet per opportunity;
- the first official entry per strategy, event, fighter outcome, bookmaker, and stake method.

Primary table:

```text
paper_bet
```

New primary bets use:

```text
stake_method = FLAT_ONE_UNIT
stake_units = 1.00000000
bet_status = OPEN
```

`simulation_account_id` is nullable for the primary flat-stake research flow. Simulation accounts are used by separate bankroll and Kelly analyses.

### 7. Record resolutions

Resolution recording stores the official result in:

```text
event_resolution
```

Supported result types are:

```text
PARTICIPANT_WIN
DRAW
NO_CONTEST
CANCELLED
POSTPONED
UNKNOWN
```

For `PARTICIPANT_WIN`, the winning participant must belong to the event. Other result types must not specify a winner.

A final resolution is recorded with `is_final = true`. The event status is updated appropriately.

### 8. Settle completed events

Settlement loads the final resolution recorded in Step 7 and settles all `OPEN` paper bets for the event.

Settlement math:

```text
WIN
  gross return = stake × decimal odds
  net profit   = gross return − stake

LOSS
  gross return = 0
  net profit   = −stake

VOID
  gross return = stake
  net profit   = 0
```

The operation updates each paper bet to `SETTLED` and records audit entries in:

```text
audit_record
```

The settlement update only affects bets whose current status is `OPEN`, making repeated settlement runs safe.

---

## Current data state

A new database contains the schema and default strategy but may contain no events, markets, snapshots, mappings, opportunities, or bets.

That means operations can validly return messages such as:

```text
No approved market mappings were found.
No qualified opportunities are available for paper betting.
There are no unresolved events with open paper bets.
There are no completed events with open paper bets.
```

These messages indicate that the pipeline is working but has no source data yet.

---

## Recommended first end-to-end test

Before relying on live APIs, seed one controlled UFC fight with:

- one sporting event;
- two participants;
- two event-participant rows;
- one sportsbook moneyline market;
- two sportsbook outcomes;
- one Polymarket market;
- two Polymarket outcomes;
- recent snapshots for both sources.

Then run options 3 through 8 and verify each persisted state transition.

After the synthetic workflow succeeds, connect the real sportsbook and Polymarket ingestion runners.

---

## Useful database queries

### Recent sportsbook snapshots

```sql
SELECT *
FROM ufc_study.sportsbook_odds_snapshot
ORDER BY observed_at DESC
LIMIT 20;
```

### Recent Polymarket snapshots

```sql
SELECT *
FROM ufc_study.prediction_market_snapshot
ORDER BY observed_at DESC
LIMIT 20;
```

### Matching candidates

```sql
SELECT *
FROM ufc_study.automated_match_candidate
ORDER BY created_at DESC
LIMIT 20;
```

### Approved mappings

```sql
SELECT *
FROM ufc_study.market_mapping
WHERE mapping_status IN (
    'APPROVED_AUTOMATIC',
    'APPROVED_MANUAL'
)
ORDER BY created_at DESC;
```

### Opportunity summary

```sql
SELECT
    qualification_status,
    rejection_code,
    COUNT(*) AS total
FROM ufc_study.opportunity
GROUP BY qualification_status, rejection_code
ORDER BY qualification_status, rejection_code;
```

### Recent paper bets

```sql
SELECT
    id,
    opportunity_id,
    bookmaker_code,
    stake_method,
    stake_units,
    decimal_odds,
    estimated_ev,
    bet_status,
    bet_result,
    net_profit_units,
    placed_at,
    settled_at
FROM ufc_study.paper_bet
ORDER BY placed_at DESC
LIMIT 50;
```

### Final resolutions

```sql
SELECT *
FROM ufc_study.event_resolution
WHERE is_final = TRUE
ORDER BY observed_at DESC;
```

### Audit history

```sql
SELECT
    entity_type,
    entity_id,
    action_type,
    reason_code,
    description,
    performed_by,
    occurred_at
FROM ufc_study.audit_record
ORDER BY occurred_at DESC
LIMIT 100;
```

---

## Troubleshooting

### Testcontainers cannot find Docker

Symptoms include Maven test failures saying no valid Docker environment was found.

Check:

```powershell
docker version
docker info
docker run --rm hello-world
```

On Docker Desktop for Windows, confirm the active context:

```powershell
docker context ls
docker context use desktop-linux
```

Restart Docker Desktop or WSL if necessary:

```powershell
wsl --shutdown
```

Then restart Docker Desktop and rerun:

```powershell
mvn clean verify
```

### Java reports compact source-file or implicit-main errors

This usually means a method or import block was saved as a standalone `.java` file without a class declaration.

Every Java source file must have:

```java
package ...;

import ...;

public class Example {
    // methods belong here
}
```

Repository and service implementation files belong under `src/main/java`, not `src/test/java`.

### Package does not match the expected package

The directory and package must agree. For example:

```text
console-ui/src/main/java/com/ufcstudy/console/settlement/service/EventSettlementService.java
```

must begin with:

```java
package com.ufcstudy.console.settlement.service;
```

### A service method is undefined

Check that the caller imports the intended class. The project may contain similarly named service classes in different packages.

Search for stale imports:

```powershell
Get-ChildItem .\console-ui\src\main\java -Recurse -Filter "*.java" |
Select-String -Pattern "EventSettlementService"
```

### The console starts but every operation returns zero rows

This is expected in a newly migrated database. Verify whether these tables contain data:

```sql
SELECT COUNT(*) FROM ufc_study.source_market;
SELECT COUNT(*) FROM ufc_study.source_market_outcome;
SELECT COUNT(*) FROM ufc_study.sportsbook_odds_snapshot;
SELECT COUNT(*) FROM ufc_study.prediction_market_snapshot;
```

Connect the real ingestion runners or load a controlled seed fixture.

---

## Development notes

- Keep console orchestration separate from calculation and persistence logic.
- Reuse existing module services instead of duplicating SQL in `console-ui` when a domain service already exists.
- Use transactions for operations that update multiple related rows.
- Generate UUIDs in Java for tables whose IDs have no database default.
- Preserve immutable snapshot history; insert new snapshots rather than updating old ones.
- Let database unique constraints provide the final race-condition protection.
- Avoid declaring Spring-proxied configuration or service classes `final` when class-based proxies are required.
- Never commit API keys, database passwords, or private source payloads.

---

## Testing strategy

Recommended test coverage includes:

- unit tests for probability and EV calculations;
- strategy qualification boundary tests;
- participant and market matching tests;
- paper-bet duplicate-entry tests;
- win, loss, and void settlement tests;
- repository integration tests with Testcontainers;
- one complete synthetic event workflow from ingestion-shaped data through settlement.

Run all tests:

```powershell
mvn clean verify
```

---

## Suggested roadmap

1. Add a deterministic single-fight SQL or Java seed fixture.
2. Connect the sportsbook ingestion runner to the selected odds API.
3. Connect Gamma and CLOB Polymarket ingestion.
4. Add scheduled ingestion and retry handling.
5. Add stale-data and ingestion-failure monitoring.
6. Add settlement reconciliation against an external result provider.
7. Expand reporting for ROI, calibration, closing-line value, and strategy comparisons.
8. Add historical replay and bankroll simulations.

---

## License

MIT License