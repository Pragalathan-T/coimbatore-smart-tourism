export type ApiStatus = "SUCCESS" | "ERROR";

export type ApiErrorCode =
  | "UNAUTHORIZED"
  | "FORBIDDEN"
  | "CONFLICT"
  | "INVALID_STATE"
  | "TOKEN_STALE"
  | "INTERNAL_ERROR"
  | "VALIDATION_ERROR"
  | "NOT_FOUND"
  | string;

export type ApiResponse<T> = {
  status: ApiStatus;
  data: T | null;
  message: string;
  errorCode: ApiErrorCode | null;
  timestamp: string;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  isFirst: boolean;
  isLast: boolean;
};
