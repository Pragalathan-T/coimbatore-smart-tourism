import type { PageResponse } from "@/types/api";

export type VerificationStatus = "PENDING" | "APPROVED" | "REJECTED";
export type VerificationLevel =
  | "BASIC"
  | "ID_VERIFIED"
  | "ADDRESS_VERIFIED"
  | "FULLY_VERIFIED";
export type DocumentType = "AADHAR" | "PASSPORT" | "DRIVING_LICENSE";

export type GuideVerificationResponseDto = {
  id: string;
  guideId: string;
  verificationLevel: VerificationLevel;
  status: VerificationStatus;
  documentType: DocumentType;
  documentNumber: string;
  documentUrl: string;
  rejectionReason: string | null;
  createdAt: string;
  updatedAt: string;
};

export type GuideVerificationPageResponseDto =
  PageResponse<GuideVerificationResponseDto>;

export type RejectGuideVerificationRequestDto = {
  rejectionReason: string;
};

export type ApplyGuideVerificationRequestDto = {
  documentType: DocumentType;
  documentNumber: string;
  documentUrl: string;
};
