# Ticket 04: Retrospective Hardening & Smells Remediation

Status: resolved
Blocked by: 03

## Objectives
1. Eliminate proxy configuration duplication in `frontend/vite.config.js` by extracting `const proxyConfig` shared between `server.proxy` and `preview.proxy`.
2. Refactor 401 response interceptor in `frontend/src/api/axiosConfig.js`:
   - Remove `window.location.href = '/login'`, relying on reactive `useAuthStore.getState().logout()` so React Router's `<ProtectedRoute>` handles SPA navigation.
   - Tighten login endpoint check using `/(^|\/)token\/?(\?.*)?$/` to prevent false matches on other token routes.
3. Update `frontend/src/pages/Login.jsx` to prioritize `err.response?.data?.detail` before falling back to generic default strings.

## Acceptance Criteria
- Proxy configuration in Vite is DRY. (Done)
- Hard window reload on 401 is removed, preserving SPA state and routing. (Done)
- Login endpoint matching is strict. (Done)
- Backend detail error message is displayed on failed login. (Done)
- Lint and build pass cleanly. (Verified)

## Answer
Extracted `proxyConfig` in `vite.config.js`.
Removed `window.location.href = '/login'` and tightened login check regex in `axiosConfig.js`.
Updated `Login.jsx` to surface `err.response?.data?.detail`.
Verified with `npm.cmd run lint` (0 errors) and `npm.cmd run build` (success).
