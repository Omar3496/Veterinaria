import React from 'react';
import { useAuthStore } from '../store/authStore';

export const RoleGate = ({ allowedRoles, children }) => {
  const role = useAuthStore((state) => state.role);

  if (!role) {
    return null;
  }

  if (!allowedRoles.includes(role)) {
    return null;
  }

  return <>{children}</>;
};
