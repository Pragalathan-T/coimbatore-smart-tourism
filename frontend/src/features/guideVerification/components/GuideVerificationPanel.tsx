"use client";

import { Button } from "@/components/ui/button";
import { ApiClientError } from "@/lib/api";
import { useGuideMyVerification } from "@/hooks/useAuth";
import { GuideVerificationStatusCard } from "./GuideVerificationStatusCard";

export function GuideVerificationPanel() {
  const { data, isLoading, error, refetch } = useGuideMyVerification();

  if (isLoading) {
    return <p className="text-sm text-muted-foreground">Loading your verification...</p>;
  }

  if (error) {
    if (error instanceof ApiClientError && error.errorCode === "NOT_FOUND") {
      return <GuideVerificationStatusCard verification={null} />;
    }

    return (
      <div className="space-y-3">
        <p className="text-sm text-destructive">Failed to load verification details.</p>
        <Button type="button" variant="outline" size="sm" onClick={() => refetch()}>
          Retry
        </Button>
      </div>
    );
  }

  return <GuideVerificationStatusCard verification={data ?? null} />;
}
