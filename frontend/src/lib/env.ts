const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;

if (!baseUrl) {
  throw new Error(
    "Missing NEXT_PUBLIC_API_BASE_URL. Set it in frontend/.env.local",
  );
}

export const API_BASE_URL = baseUrl;
