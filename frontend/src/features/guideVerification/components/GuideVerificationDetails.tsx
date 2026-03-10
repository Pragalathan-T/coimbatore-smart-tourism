import type { GuideVerificationResponseDto } from "@/types/guideVerification";

type GuideVerificationDetailsProps = {
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

export function GuideVerificationDetails({ verification }: GuideVerificationDetailsProps) {
  return (
    <div className="space-y-1 text-sm text-muted-foreground">
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
        <a
          href={verification.documentUrl}
          target="_blank"
          rel="noreferrer"
          className="underline underline-offset-4"
        >
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
        <span className="font-medium text-foreground">Submitted At:</span>{" "}
        {formatDate(verification.createdAt)}
      </p>
      <p>
        <span className="font-medium text-foreground">Updated At:</span>{" "}
        {formatDate(verification.updatedAt)}
      </p>
    </div>
  );
}
