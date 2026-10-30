# Ticket 01: Wire, Base URL, Proxy and 401 Interceptor Configuration

Status: resolved

## Objectives
1. Update `frontend/src/api/axiosConfig.js`:
   - Use `import.meta.env.VITE_API_BASE_URL || ''` for flexible base URL.
   - Maintain Bearer token injection on requests.
   - Add response interceptor to handle 401 Unauthorized responses by clearing localStorage token and invoking `useAuthStore.getState().logout()` (except for `/token` login calls).
2. Update `frontend/vite.config.js`:
   - Point `/api` and `/token` proxy targets to `http://localhost:8081`.
3. Update `frontend/nginx.conf`:
   - Point `/api/` and `/token` proxy passes to `http://backend:8081`.

## Acceptance Criteria
- Port `8000` is removed from all frontend configs. (Done)
- Axios routes requests via Vite proxy or custom base URL cleanly. (Done)
- 401 responses trigger proper session invalidation. (Done)

## Answer
Updated `frontend/src/api/axiosConfig.js` to default to relative path with Vite proxy and handle 401 token invalidation.
Updated `frontend/vite.config.js` and `frontend/nginx.conf` proxy targets to `8081`.
Verified zero lingering occurrences of `8000`.
