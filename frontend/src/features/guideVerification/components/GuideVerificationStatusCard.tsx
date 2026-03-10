"use client";

import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import type { GuideVerificationResponseDto } from "@/types/guideVerification";
import { GuideVerificationDetails } from "./GuideVerificationDetails";
import { GuideVerificationForm } from "./GuideVerificationForm";

type GuideVerificationStatusCardProps = {
  verification: GuideVerificationResponseDto | null;
};

function statusVariant(status: GuideVerificationResponseDto["status"] | "NONE") {
  if (status === "APPROVED") return "default" as const;
  if (status === "REJECTED") return "destructive" as const;
  if (status === "PENDING") return "secondary" as const;
  return "outline" as const;
}

export function GuideVerificationStatusCard({ verification }: GuideVerificationStatusCardProps) {
  const [applyOpen, setApplyOpen] = useState(false);

  const currentStatus = verification?.status ?? "NONE";
  const canApply = !verification || verification.status !== "PENDING";

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0">
        <CardTitle className="text-lg">Current Verification Status</CardTitle>
        <Badge variant={statusVariant(currentStatus)}>
          {verification ? verification.status : "NOT_SUBMITTED"}
        </Badge>
      </CardHeader>
      <CardContent className="space-y-4">
        {verification ? (
          <GuideVerificationDetails verification={verification} />
        ) : (
          <p className="text-sm text-muted-foreground">
            You have not submitted a verification request yet.
          </p>
        )}

        {verification?.status === "PENDING" ? (
          <p className="text-sm text-muted-foreground">
            Your verification is under review. You cannot submit another request while one is pending.
          </p>
        ) : null}

        {verification?.status === "REJECTED" ? (
          <p className="text-sm text-muted-foreground">
            You may reapply. If a cooldown is active, the backend will enforce the reapply date.
          </p>
        ) : null}

        {canApply ? (
          <Dialog open={applyOpen} onOpenChange={setApplyOpen}>
            <DialogTrigger asChild>
              <Button>{verification ? "Submit New Verification" : "Apply for Verification"}</Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Guide Verification Application</DialogTitle>
                <DialogDescription>
                  Submit your document details for admin review.
                </DialogDescription>
              </DialogHeader>
              <GuideVerificationForm onSuccess={() => setApplyOpen(false)} />
            </DialogContent>
          </Dialog>
        ) : null}
      </CardContent>
    </Card>
  );
}
