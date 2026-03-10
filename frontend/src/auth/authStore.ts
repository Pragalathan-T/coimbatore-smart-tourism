import { getStorageItem, removeStorageItem, setStorageItem } from "@/lib/storage";
import { getRoleFromToken, getUserIdFromToken, getTokenVersionFromToken } from "@/auth/token";
import type { UserRole } from "@/types/auth";

const AUTH_TOKEN_KEY = "smart-tourism:auth-token";
const AUTH_ROLE_KEY = "smart-tourism:auth-role";
const AUTH_USER_ID_KEY = "smart-tourism:auth-userId";
const AUTH_TV_KEY = "smart-tourism:auth-tv";

export function getToken(): string | null {
  return getStorageItem(AUTH_TOKEN_KEY);
}

/**
 * Store authentication data.
 * Prefers explicit response fields, falls back to decoding JWT.
 */
export function setAuthData(
  token: string,
  options?: {
    role?: UserRole | null;
    userId?: string | null;
    tokenVersion?: number | null;
  }
): void {
  setStorageItem(AUTH_TOKEN_KEY, token);

  // Prefer response fields if present, else decode from JWT
  const role = options?.role ?? getRoleFromToken(token);
  const userId = options?.userId ?? getUserIdFromToken(token);
  const tv = options?.tokenVersion ?? getTokenVersionFromToken(token);

  if (role) setStorageItem(AUTH_ROLE_KEY, role);
  if (userId) setStorageItem(AUTH_USER_ID_KEY, userId);
  if (tv !== null && tv !== undefined) setStorageItem(AUTH_TV_KEY, String(tv));
}

/**
 * @deprecated Use setAuthData instead for new code
 */
export function setToken(token: string): void {
  setAuthData(token);
}

export function clearToken(): void {
  removeStorageItem(AUTH_TOKEN_KEY);
  removeStorageItem(AUTH_ROLE_KEY);
  removeStorageItem(AUTH_USER_ID_KEY);
  removeStorageItem(AUTH_TV_KEY);
}

export function getAuth(): {
  token: string | null;
  role: UserRole | null;
  userId: string | null;
  tv: number | null;
} {
  const token = getToken();
  if (!token) {
    return { token: null, role: null, userId: null, tv: null };
  }

  // Try stored values first, fall back to JWT decoding
  const storedRole = getStorageItem(AUTH_ROLE_KEY) as UserRole | null;
  const storedUserId = getStorageItem(AUTH_USER_ID_KEY);
  const storedTv = getStorageItem(AUTH_TV_KEY);

  return {
    token,
    role: storedRole ?? getRoleFromToken(token),
    userId: storedUserId ?? getUserIdFromToken(token),
    tv: storedTv !== null ? parseInt(storedTv, 10) : getTokenVersionFromToken(token),
  };
}
