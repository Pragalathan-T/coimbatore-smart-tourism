import type { UserRole } from "@/types/auth";

type JwtPayload = {
  sub?: string;
  role?: string;
  tv?: number;
};

function decodeBase64Url(input: string): string {
  const normalized = input.replace(/-/g, "+").replace(/_/g, "/");
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "=");
  const binary = atob(padded);

  try {
    const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0));
    return new TextDecoder().decode(bytes);
  } catch {
    return binary;
  }
}

export function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const parts = token.split(".");
    if (parts.length < 2) {
      return null;
    }
    const payloadJson = decodeBase64Url(parts[1]);
    return JSON.parse(payloadJson) as JwtPayload;
  } catch {
    return null;
  }
}

export function getRoleFromToken(token: string): UserRole | null {
  const payload = decodeJwtPayload(token);
  const role = payload?.role;
  if (role === "ADMIN" || role === "GUIDE" || role === "TOURIST") {
    return role;
  }
  return null;
}

export function getUserIdFromToken(token: string): string | null {
  const payload = decodeJwtPayload(token);
  return payload?.sub ?? null;
}

export function getTokenVersionFromToken(token: string): number | null {
  const payload = decodeJwtPayload(token);
  if (typeof payload?.tv === "number") {
    return payload.tv;
  }
  return null;
}
