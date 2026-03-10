import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getGuideBookings,
  getBookingById,
  updateBookingStatus,
  acceptBooking,
  rejectBooking,
} from "./api";
import type {
  BookingResponseDto,
  BookingPageResponseDto,
  GuideBookingAction,
  GuideBookingsParams,
} from "./types";

// Query key factory for bookings
export const bookingKeys = {
  all: ["bookings"] as const,
  myBookings: () => [...bookingKeys.all, "my"] as const,
  myBookingsList: (params: GuideBookingsParams) =>
    [...bookingKeys.myBookings(), params] as const,
  detail: (id: string) => [...bookingKeys.all, "detail", id] as const,
};

/**
 * Hook to fetch guide's bookings with pagination.
 */
export function useGuideBookings(params: GuideBookingsParams = {}) {
  const { page = 0, size = 10, all = true } = params;

  return useQuery<BookingPageResponseDto>({
    queryKey: bookingKeys.myBookingsList({ page, size, all }),
    queryFn: () => getGuideBookings({ page, size, all }),
  });
}

/**
 * Hook to fetch a single booking by ID.
 */
export function useBooking(id: string | undefined) {
  return useQuery<BookingResponseDto>({
    queryKey: bookingKeys.detail(id!),
    queryFn: () => getBookingById(id!),
    enabled: !!id,
  });
}

/**
 * Hook for updating booking status (accept/reject).
 * Invalidates the bookings list on success.
 */
export function useUpdateBookingStatus() {
  const queryClient = useQueryClient();

  return useMutation<
    BookingResponseDto,
    Error,
    { bookingId: string; action: GuideBookingAction }
  >({
    mutationFn: ({ bookingId, action }) => updateBookingStatus(bookingId, action),
    onSuccess: (data) => {
      // Invalidate all booking lists
      queryClient.invalidateQueries({ queryKey: bookingKeys.myBookings() });
      // Also invalidate legacy query key used in useAuth.ts
      queryClient.invalidateQueries({ queryKey: ["my-bookings"] });
      // Update the specific booking in cache
      queryClient.setQueryData(bookingKeys.detail(data.id), data);
    },
  });
}

/**
 * Hook specifically for accepting a booking.
 */
export function useAcceptBooking() {
  const queryClient = useQueryClient();

  return useMutation<BookingResponseDto, Error, string>({
    mutationFn: acceptBooking,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: bookingKeys.myBookings() });
      queryClient.invalidateQueries({ queryKey: ["my-bookings"] });
      queryClient.setQueryData(bookingKeys.detail(data.id), data);
    },
  });
}

/**
 * Hook specifically for rejecting a booking.
 */
export function useRejectBooking() {
  const queryClient = useQueryClient();

  return useMutation<BookingResponseDto, Error, string>({
    mutationFn: rejectBooking,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: bookingKeys.myBookings() });
      queryClient.invalidateQueries({ queryKey: ["my-bookings"] });
      queryClient.setQueryData(bookingKeys.detail(data.id), data);
    },
  });
}
