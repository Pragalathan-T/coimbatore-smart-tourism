import { api, unwrapApiResponse } from "@/lib/api";
import type { ApiResponse } from "@/types/api";
import type {
  BookingResponseDto,
  BookingPageResponseDto,
  GuideBookingAction,
  GuideBookingsParams,
} from "./types";

/**
 * Fetch bookings for the current guide.
 * Uses the same endpoint as tourist bookings - backend filters by role.
 */
export async function getGuideBookings(
  params: GuideBookingsParams = {}
): Promise<BookingPageResponseDto> {
  const { page = 0, size = 10, all = true } = params;
  const response = await api.get<ApiResponse<BookingPageResponseDto>>(
    "/api/v1/bookings/my",
    { params: { page, size, all } }
  );
  return unwrapApiResponse(response.data);
}

/**
 * Get a single booking by ID.
 */
export async function getBookingById(id: string): Promise<BookingResponseDto> {
  const response = await api.get<ApiResponse<BookingResponseDto>>(
    `/api/v1/bookings/${id}`
  );
  return unwrapApiResponse(response.data);
}

/**
 * Update booking status - guide actions (accept/reject).
 */
export async function updateBookingStatus(
  bookingId: string,
  action: GuideBookingAction
): Promise<BookingResponseDto> {
  const response = await api.put<ApiResponse<BookingResponseDto>>(
    `/api/v1/bookings/${bookingId}/${action}`
  );
  return unwrapApiResponse(response.data);
}

/**
 * Accept a booking request (guide only).
 * Transitions: REQUESTED → ACCEPTED
 */
export async function acceptBooking(bookingId: string): Promise<BookingResponseDto> {
  return updateBookingStatus(bookingId, "accept");
}

/**
 * Reject a booking request (guide only).
 * Transitions: REQUESTED → REJECTED
 */
export async function rejectBooking(bookingId: string): Promise<BookingResponseDto> {
  return updateBookingStatus(bookingId, "reject");
}
