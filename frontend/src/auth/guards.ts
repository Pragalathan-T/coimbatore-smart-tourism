import { getAuth } from "@/auth/authStore";
import type { UserRole } from "@/types/auth";

export function requireAuth() {
  const auth = getAuth();
  if (!auth.token) {
    if (typeof window !== "undefined") {
      window.location.replace("/login");
    }
    return null;
  }
  return auth;
}

export function requireRole(allowedRoles: UserRole[]) {
  const auth = requireAuth();
  if (!auth) {
    return null;
  }

  if (!auth.role || !allowedRoles.includes(auth.role)) {
    if (typeof window !== "undefined") {
      window.location.replace("/places");
    }
    return null;
  }

  return auth;
}
