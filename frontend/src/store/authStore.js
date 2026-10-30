import { create } from 'zustand'
import { jwtDecode } from 'jwt-decode'

const getRoleFromToken = (token) => {
  if (!token) return null;
  try {
    const decoded = jwtDecode(token);
    return decoded.rol || null;
  } catch (error) {
    return null;
  }
};

const getUserFromToken = (token) => {
  if (!token) return null;
  try {
    const decoded = jwtDecode(token);
    return decoded.sub || null;
  } catch (error) {
    return null;
  }
};

const initialToken = typeof window !== 'undefined' ? localStorage.getItem('token') || null : null;

export const useAuthStore = create((set) => ({
  token: initialToken,
  isAuthenticated: !!initialToken,
  role: getRoleFromToken(initialToken),
  username: getUserFromToken(initialToken),
  login: (token) => {
    if (!token) return;
    localStorage.setItem('token', token);
    set({
      token,
      isAuthenticated: true,
      role: getRoleFromToken(token),
      username: getUserFromToken(token),
    });
  },
  logout: () => {
    localStorage.removeItem('token');
    set({ token: null, isAuthenticated: false, role: null, username: null });
  },
}))
