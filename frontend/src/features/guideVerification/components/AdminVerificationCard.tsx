"use client";

import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useApproveVerification, useRejectVerification } from "@/hooks/useAuth";
import type { GuideVerificationResponseDto } from "@/types/guideVerification";
import { VerificationDecisionDialog } from "./VerificationDecisionDialog";

type AdminVerificationCardProps = {
  verification: GuideVerificationResponseDto;
};

function formatDate(value: string): string {
  try {
    return new Date(value).toLocaleString(undefined, {
      dateStyle: "medium",
      timeStyle: "short",
    });
  } catch {
    return value;
  }
}

function statusVariant(status: GuideVerificationResponseDto["status"]) {
  if (status === "APPROVED") return "default" as const;
  if (status === "REJECTED") return "destructive" as const;
  return "secondary" as const;
}

export function AdminVerificationCard({ verification }: AdminVerificationCardProps) {
  const approveMutation = useApproveVerification();
  const rejectMutation = useRejectVerification();

  const actionPending = approveMutation.isPending || rejectMutation.isPending;
  const canReview = verification.status === "PENDING";

  const onApprove = async () => {
    try {
      await approveMutation.mutateAsync(verification.id);
      toast.success("Verification approved");
    } catch (error) {
      const message = error instanceof Error ? error.message : "Failed to approve verification";
      toast.error(message);
    }
  };

  const onReject = async (reason?: string) => {
    try {
      await rejectMutation.mutateAsync({
        verificationId: verification.id,
        payload: { rejectionReason: reason ?? "Rejected by admin" },
      });
      toast.success("Verification rejected");
    } catch (error) {
      const message = error instanceof Error ? error.message : "Failed to reject verification";
      toast.error(message);
    }
  };

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-base">Guide Verification #{verification.id.slice(0, 8)}</CardTitle>
        <Badge variant={statusVariant(verification.status)}>{verification.status}</Badge>
      </CardHeader>
      <CardContent className="space-y-2 text-sm text-muted-foreground">
        <p>
          <span className="font-medium text-foreground">Guide ID:</span> {verification.guideId}
        </p>
        <p>
          <span className="font-medium text-foreground">Verification Level:</span>{" "}
          {verification.verificationLevel}
        </p>
        <p>
          <span className="font-medium text-foreground">Document Type:</span> {verification.documentType}
        </p>
        <p>
          <span className="font-medium text-foreground">Document Number:</span>{" "}
          {verification.documentNumber}
        </p>
        <p>
          <span className="font-medium text-foreground">Document URL:</span>{" "}
          <a className="underline underline-offset-4" href={verification.documentUrl} target="_blank" rel="noreferrer">
            View Document
          </a>
        </p>
        {verification.rejectionReason ? (
          <p>
            <span className="font-medium text-foreground">Rejection Reason:</span>{" "}
            {verification.rejectionReason}
          </p>
        ) : null}
        <p>
          <span className="font-medium text-foreground">Submitted At:</span> {formatDate(verification.createdAt)}
        </p>
        <p>
          <span className="font-medium text-foreground">Updated At:</span> {formatDate(verification.updatedAt)}
        </p>

        {canReview ? (
          <div className="flex flex-wrap gap-2 pt-2">
            <VerificationDecisionDialog
              trigger={<Button size="sm" disabled={actionPending}>Approve</Button>}
              title="Approve Verification"
              description="Approve this guide verification request?"
              confirmText="Approve"
              pending={actionPending}
              onConfirm={onApprove}
            />
            <VerificationDecisionDialog
              trigger={<Button size="sm" variant="destructive" disabled={actionPending}>Reject</Button>}
              title="Reject Verification"
              description="Reject this request and provide a clear reason for the guide."
              confirmText="Reject"
              pending={actionPending}
              requireReason
              onConfirm={onReject}
            />
          </div>
        ) : null}
      </CardContent>
    </Card>
  );
}
