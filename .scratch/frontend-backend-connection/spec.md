# Specification: Frontend-to-Backend Integration and Hardening

Status: ready-for-agent

## Problem Statement

The Sirbull Pet Clinic system completed porting its REST API backend to Java 25 Spring Boot running on port 8081. However, the React single-page frontend remained pointed to the legacy Python/FastAPI port 8000. Additionally, the development server proxy configuration was incomplete for preview workflows and local area network testing, authentication error handling masked backend validation messages behind generic fallback strings, and session expiration triggered hard browser reloads that disrupted client-side SPA routing and interrupted public catalog browsing.

## Solution

Establish a robust, zero-friction connection between the React frontend and the Spring Boot backend. Align development, preview, and containerized reverse proxies to port 8081. Preserve single-page application continuity by delegating authentication expiration to client-side reactive state rather than hard page reloads. Ensure all authentication and mutation forms surface server-generated domain error details verbatim. Eliminate configuration duplication across server modes while guaranteeing full compatibility with the existing security, role-based access control, and domain wire contracts.

## User Stories

1. As an **Administrador**, I want to log in using my system credentials so that I can access clinic statistics and administrative dashboards.
2. As an **Administrador**, I want to see detailed backend validation messages when login fails so that I understand whether my password was incorrect or my account was deactivated.
3. As a **Veterinario**, I want to access clinical schedules and medical records seamlessly so that I can attend assigned appointments without network or port mismatches.
4. As a **Recepcionista**, I want the frontend to communicate transparently with the scheduling service so that appointment booking and availability calculations function reliably.
5. As a **Cliente**, I want to authenticate into the self-service portal so that I can inspect my registered pets and appointment histories.
6. As a **Cliente**, I want to browse public veterinary services and product listings without being prompted for credentials or redirected to the login view.
7. As a **Cliente**, I want any failed password recovery attempt to display the server's explanation so that I know whether the email was invalid or throttled.
8. As an **Authenticated User**, I want my session to expire gracefully when my JWT becomes invalid so that I am returned to the login screen without losing single-page app continuity or experiencing full-page browser flashes.
9. As an **Authenticated User**, I want endpoints containing token substrings (such as reset-password tokens) to remain accessible without erroneously triggering immediate session logout.
10. As a **Developer**, I want the frontend development server to proxy API and token requests to the Spring Boot backend on port 8081 so that I do not encounter browser cross-origin resource sharing (CORS) blocks during local programming.
11. As a **Developer**, I want the frontend preview server to share the identical proxy definitions as the development server so that production bundle verification behaves consistently with development.
12. As a **Developer**, I want the API client to support an optional external base URL environment override so that containerized and staging deployments can be configured without rebuilding source files.
13. As a **Developer**, I want the production Nginx reverse proxy configuration to target backend port 8081 so that containerized production deployments route traffic without network failures.
14. As a **Developer**, I want proxy definitions unified into a single declarative structure so that code duplication and maintenance divergence are eliminated.
15. As a **Developer**, I want frontend linting and production compilation to pass cleanly with zero errors so that regressions are caught before deployment.

## Implementation Decisions

- **Network Routing & Proxy Strategy**:
  - Unify API routing through reverse proxying for both development and preview environments.
  - Target the backend runtime on port 8081 across local proxy definitions and production container reverse proxy configurations.
  - Default the API client base URL to relative paths, delegating resolution to the surrounding proxy infrastructure while retaining environment variable overrides for custom remote deployments.
  - Consolidate proxy configuration into a single shared specification applied identically to development and preview servers.

- **Session Invalidation & Reactive Routing**:
  - Eliminate direct window location assignments and browser reloads from the network transport layer.
  - Invalidate expired or revoked sessions strictly through global authentication state store actions upon receiving HTTP 401 Unauthorized responses.
  - Allow higher-level protected route guards within the single-page application router to handle redirection to the login view reactively.
  - Restrict login-exemption matching in the network interceptor strictly to authentication token acquisition endpoints, preventing false positives on token validation or credential recovery routes.

- **Domain Error Surfacing**:
  - Standardize authentication and credential recovery form handlers to extract and display server-provided detail messages before falling back to generic default warnings.
  - Maintain compatibility with the established wire contract where all validation, authentication, and domain rule rejections return structured error payloads with a standard detail field.

## Testing Decisions

- **Test Quality Definition**:
  - Tests must verify observable system behavior from the consumer perspective (status codes, response headers, parsed payload content, and route transitions) rather than private internal implementation details.
- **Modules Tested**:
  - HTTP network transport and response interceptors (401 session clearing and endpoint matching).
  - Dev server proxy routing (forwarding `/api/**` and `/token` to backend port 8081).
  - Production bundle compilation and static code analysis.
  - End-to-end authentication and data retrieval flows across both staff and client roles.
- **Prior Art**:
  - Spring Boot integration test suites (`AuthAndSecurityIntegrationTests`, `ConfiguracionCitasAndPagosIntegrationTests`).
  - Existing frontend Axios client interceptor patterns and Zustand auth store actions.

## Out of Scope

- Modifying the underlying Spring Boot domain entities, repositories, or database schemas.
- Provisioning external PostgreSQL databases (local testing uses the seeded in-memory H2 database).
- Redesigning React UI styling, page layouts, or animation components.
- Replacing the client-side state management library or router architecture.

## Further Notes

- All changes adhere to the domain taxonomy defined in the project glossary (`Usuario`, `Cliente`, `Mascota`, `Cita`, `Consulta`, `RolEnum`).
- Complies with architectural decision record ADR-0001 regarding preservation of the wire contract and Jackson snake_case serialization.
