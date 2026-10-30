# Frontend-Backend Connection Specification

## Context & Objectives
Connect the completed Spring Boot backend (`./Veterinaria` running on port `8081`) to the React frontend (`./frontend`).
All decisions were settled through collaborative grilling rounds:
1. Target port alignment: `:8000` -> `:8081` across `axiosConfig.js`, `vite.config.js`, and `nginx.conf`.
2. Flexible Base URL: Default to relative path `''` with Vite proxy to avoid CORS issues on LAN, while supporting `VITE_API_BASE_URL`.
3. Auto-logout on 401: Add response interceptor to `axiosConfig.js` to clear expired sessions and invoke `useAuthStore.getState().logout()`.
4. Consistent detail errors: Ensure error handling captures backend `detail` messages.
5. Verification: Install dependencies with `npm.cmd`, build, lint, and run end-to-end smoke tests for both staff (`admin`) and client (`jperez`).

## Architecture & Integration Details
- **Backend API**: Port `8081`, Spring Boot 4.1.1, Spring Security, JWT HS256 (`sub`, `rol`), Jackson `SNAKE_CASE`.
- **Frontend App**: React 19, Vite 8.1, TailwindCSS 4, Zustand 5, Axios.
- **Proxy Configuration**:
  - Dev: Vite proxy for `/api` and `/token` forwarding to `http://localhost:8081`.
  - Prod: Nginx reverse proxy for `/api/` and `/token` forwarding to `http://backend:8081`.
