"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { clearToken } from "@/auth/authStore";
import { requireAuth, requireRole } from "@/auth/guards";
import { Button } from "@/components/ui/button";
import type { UserRole } from "@/types/auth";

const ROUTE_ACCESS: Array<{ prefix: string; roles: UserRole[] }> = [
  { prefix: "/admin", roles: ["ADMIN"] },
  { prefix: "/guide/verification", roles: ["GUIDE"] },
  { prefix: "/guide/bookings", roles: ["GUIDE"] },
  { prefix: "/places", roles: ["ADMIN", "GUIDE", "TOURIST"] },
  { prefix: "/bookings", roles: ["GUIDE", "TOURIST"] },
];

export default function ProtectedLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  const pathname = usePathname();
  const router = useRouter();

  const rule = ROUTE_ACCESS.find((entry) => pathname.startsWith(entry.prefix));
  const auth = rule ? requireRole(rule.roles) : requireAuth();

  if (!auth) {
    return <main className="p-6 text-sm text-muted-foreground">Checking access...</main>;
  }

  const onLogout = () => {
    clearToken();
    router.replace("/login");
  };

  const currentRole = auth.role;

  return (
    <div className="min-h-screen">
      <header className="border-b">
        <div className="mx-auto flex w-full max-w-6xl items-center justify-between p-4">
          <nav className="flex items-center gap-4 text-sm">
            <Link href="/places" className="underline-offset-4 hover:underline">
              Places
            </Link>
            {(currentRole === "GUIDE" || currentRole === "TOURIST") && (
              <Link href="/bookings" className="underline-offset-4 hover:underline">
                Bookings
              </Link>
            )}
            {currentRole === "ADMIN" && (
              <Link
                href="/admin/verifications"
                className="underline-offset-4 hover:underline"
              >
                Admin Verifications
              </Link>
            )}
            {currentRole === "ADMIN" && (
              <Link
                href="/admin/places"
                className="underline-offset-4 hover:underline"
              >
                Admin Places
              </Link>
            )}
            {currentRole === "GUIDE" && (
              <Link
                href="/guide/verification"
                className="underline-offset-4 hover:underline"
              >
                My Verification
              </Link>
            )}
            {currentRole === "GUIDE" && (
              <Link
                href="/guide/bookings"
                className="underline-offset-4 hover:underline"
              >
                Guide Bookings
              </Link>
            )}
          </nav>

          <div className="flex items-center gap-3">
            <span className="text-sm text-muted-foreground">
              Role: {currentRole ?? "UNKNOWN"}
            </span>
            <Button variant="outline" onClick={onLogout}>
              Logout
            </Button>
          </div>
        </div>
      </header>

      <main className="mx-auto w-full max-w-6xl p-6">{children}</main>
    </div>
  );
}
