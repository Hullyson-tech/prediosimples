# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

PrédioSimples: the REST backend for managing a small condominium (ODS 11 academic project). Part of a 3-tier design: a Flutter mobile client (not in this repo), this Spring Boot API, and PostgreSQL. Code, comments, API messages and commit messages are in Portuguese (pt-BR). Commits use Conventional Commits prefixes (`feat:`, `fix:`, `docs:`).

Stack: Java 17, Spring Boot 4.1.1 (parent POM), Spring Web MVC, Spring Data JPA/Hibernate, Bean Validation, Spring Mail, Lombok, PostgreSQL. Maven artifact `com.condominio:gestao-condominio`.

## Commands

Use the Maven wrapper (`./mvnw` in Bash, `.\mvnw.cmd` in PowerShell):

```bash
./mvnw compile                      # build
./mvnw spring-boot:run              # run API on :8080
./mvnw test                         # all tests
./mvnw test -Dtest=GestaoCondominioApplicationTests            # one test class
./mvnw test -Dtest=GestaoCondominioApplicationTests#contextLoads  # one method
./mvnw package                      # build jar into target/
```

There is no linter/formatter configured. The only test is a `@SpringBootTest` context-load test, so it needs a reachable PostgreSQL at `localhost:5432/gestao_condominio` (see `src/main/resources/application.properties`). Schema is managed by `spring.jpa.hibernate.ddl-auto=update`; there are no migrations.

## Architecture

**Package layout.** All code lives under `src/main/java/com/condominio/gestao_condominio/` in `controller/`, `model/`, `repository/`, `service/` and `exception/`. Keep the directory and the `package` declaration in sync. Component scanning starts from the application class's package.

**Layers.** Controllers talk straight to Spring Data repositories and hold the business rules. `service/` only has notification senders (`EmailService` via `JavaMailSender`, `SmsService`, which is a stub). There is no DTO layer: JPA entities are used as request/response bodies.

**Authorization is not Spring Security.** RBAC is done by hand in each controller method, using a `roleQuemExecuta` request parameter that the caller supplies (`MORADOR`, `SINDICO`, `SUPORTE`, compared case-insensitively). `SINDICO` and `SUPORTE` are treated as equal admins. Login (`POST /users/login`) checks the BCrypt hash and returns user info. It issues no token or session.

**Domain rules spread across controllers:**
- Users sign up as `statusAprovacao = "PENDENTE"` and `ativo = 1`. An admin sets `APROVADO`/`REJEITADO` through `GET /users/{id}/aprovar`. Approval triggers the email and SMS notifications. Login and opening an occurrence both require `APROVADO`.
- Deletion is soft: `ativo` is an `Integer` flag (1/0), not a boolean.
- Every user mutation (create, approve, update, deactivate) must also save a `UserLog` row (`tb_user_logs`, an append-only audit table). Keep this when adding new user operations.
- `MORADOR` can update only email and phone. Admins can update name, apartment and role too.
- Occurrence (`Ocorrencia`) status values: `ABERTO`, `EM_ANDAMENTO`, `CONCLUIDO` (validated in `OcorrenciaController`; the comment in `Ocorrencia.java` says `RESOLVIDO`, which is stale). Only a `MORADOR` can create one.
- Statuses and roles are plain `String`s compared with string literals. There are no enums.

**Errors.** Business-rule failures throw a plain `RuntimeException` with a Portuguese message. `GlobalExceptionHandler` (`@RestControllerAdvice`) maps it to HTTP 400 `{timestamp, status, error}`. Validation errors become 400 `{timestamp, status, errors: {field: msg}}`. Follow this pattern unless you add proper exception types.

**Config flags.** `app.sms.enabled` (defaults to `false`) gates SMS sending. SMTP settings in `application.properties` are placeholders.

## Known state of the code (verify before relying on it)

- `./mvnw compile` succeeds. BCrypt comes from `spring-security-crypto` only. Do not add `spring-boot-starter-security`: its auto-config would lock every endpoint behind HTTP Basic and break the hand-rolled `roleQuemExecuta` RBAC.
- `Ocorrencia.morador` is `@ManyToOne(fetch = LAZY)` and entities are serialized directly. Expect Jackson problems with Hibernate proxies, and `User.password` (the hash) leaking into JSON responses, until `@JsonIgnore`/DTOs are added.

## Repo quirks

- `README.md` is UTF-16 encoded. Tools that read it as UTF-8 will show garbled text.
- A stray `${project.build.directory}/` folder at the root is an IDE build artifact, not source.
