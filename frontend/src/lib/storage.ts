export const isBrowser = typeof window !== "undefined";

export function getStorageItem(key: string): string | null {
  if (!isBrowser) {
    return null;
  }
  return window.localStorage.getItem(key);
}

export function setStorageItem(key: string, value: string): void {
  if (!isBrowser) {
    return;
  }
  window.localStorage.setItem(key, value);
}

export function removeStorageItem(key: string): void {
  if (!isBrowser) {
    return;
  }
  window.localStorage.removeItem(key);
}
