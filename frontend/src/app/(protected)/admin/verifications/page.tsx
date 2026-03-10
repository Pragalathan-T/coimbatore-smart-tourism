"use client";

import { AdminVerificationList } from "@/features/guideVerification";

export default function AdminVerificationsPage() {
  return (
    <section className="space-y-4">
      <div className="space-y-1">
        <h1 className="text-2xl font-semibold">Admin Verifications</h1>
        <p className="text-sm text-muted-foreground">
          Review guide verification requests and approve or reject with reason.
        </p>
      </div>

      <AdminVerificationList />
    </section>
  );
}
