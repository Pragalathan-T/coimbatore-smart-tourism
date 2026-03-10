"use client";

import { GuideVerificationPanel } from "@/features/guideVerification";

export default function GuideVerificationPage() {
  return (
    <section className="space-y-4">
      <div className="space-y-1">
        <h1 className="text-2xl font-semibold">My Guide Verification</h1>
        <p className="text-sm text-muted-foreground">
          View your verification status and submit a verification request.
        </p>
      </div>

      <GuideVerificationPanel />
    </section>
  );
}
