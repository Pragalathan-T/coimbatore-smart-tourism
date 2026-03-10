import axios from "axios";
import { clearToken, getToken } from "@/auth/authStore";
import { API_BASE_URL } from "@/lib/env";
import type { ApiErrorCode, ApiResponse } from "@/types/api";

export class ApiClientError extends Error {
  errorCode: ApiErrorCode | null;

  constructor(message: string, errorCode: ApiErrorCode | null = null) {
    super(message);
    this.name = "ApiClientError";
    this.errorCode = errorCode;
  }
}

export const api = axios.create({
  baseURL: API_BASE_URL,
});

api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const apiResponse = error?.response?.data as
      | ApiResponse<unknown>
      | undefined;

    if (apiResponse?.errorCode === "TOKEN_STALE") {
      clearToken();
      if (typeof window !== "undefined") {
        window.location.replace("/login");
      }
    }
    return Promise.reject(error);
  },
);

export function unwrapApiResponse<T>(payload: ApiResponse<T>): T {
  if (payload.status === "ERROR") {
    throw new ApiClientError(payload.message, payload.errorCode);
  }
  if (payload.data === null) {
    throw new ApiClientError("API returned empty data", payload.errorCode);
  }
  return payload.data;
}
