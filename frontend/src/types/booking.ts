import type { PageResponse } from "@/types/api";

export type BookingStatus =
  | "REQUESTED"
  | "ACCEPTED"
  | "CONFIRMED"
  | "IN_PROGRESS"
  | "RATED"
  | "COMPLETED"
  | "CANCELLED"
  | "REJECTED";

export type BookingResponseDto = {
  id: string;
  touristId: string;
  guideId: string | null;
  placeId: string | null;
  placeName: string | null;
  startDateTime: string | null;
  endDateTime: string | null;
  notes: string | null;
  status: BookingStatus;
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type CreateBookingRequestDto = {
  placeId: string;
  startDateTime: string;
  endDateTime: string;
  notes?: string;
};

export type BookingPageResponseDto = PageResponse<BookingResponseDto>;
