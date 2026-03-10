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

// Backend-aligned booking shape for guide view
export type BookingResponseDto = {
  id: string;
  touristId: string;
  touristName?: string | null;
  guideId: string;
  placeId: string | null;
  placeName?: string | null;
  startDateTime?: string | null;
  endDateTime?: string | null;
  notes?: string | null;
  scheduledAt: string | null;
  status: BookingStatus;
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type BookingPageResponseDto = PageResponse<BookingResponseDto>;

// Guide-specific action type
export type GuideBookingAction = "accept" | "reject";

// Query params for guide bookings list
export type GuideBookingsParams = {
  page?: number;
  size?: number;
  all?: boolean;
};

// Response type alias for guide bookings
export type GuideBookingsResponse = PageResponse<BookingResponseDto>;
