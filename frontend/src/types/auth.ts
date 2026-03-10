export type UserRole = "ADMIN" | "GUIDE" | "TOURIST";

/**
 * Authentication response from backend.
 * Fields role, userId, tokenVersion, expiresAtEpochSeconds are explicit for convenience.
 * JWT claims remain the authoritative source for security validation.
 */
export type AuthTokenResponseDto = {
  token: string;
  tokenType: "Bearer" | string;
  /** Role from server - matches JWT claim "role" */
  role?: UserRole | null;
  /** User ID (UUID string) - matches JWT claim "sub" */
  userId?: string | null;
  /** Token version - matches JWT claim "tv", used for TOKEN_STALE detection */
  tokenVersion?: number | null;
  /** Token expiration as Unix epoch seconds */
  expiresAtEpochSeconds?: number | null;
};

export type LoginRequestDto = {
  email: string;
  password: string;
};

export type RegisterRole = "TOURIST" | "GUIDE";

export type RegisterRequestDto = {
  firstName: string;
  lastName?: string;
  email: string;
  password: string;
  role: RegisterRole;
  idProofUrl?: string;
  selfieUrl?: string;
};
