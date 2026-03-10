import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api, unwrapApiResponse } from "@/lib/api";
import { setAuthData } from "@/auth/authStore";
import type { ApiResponse } from "@/types/api";
import type {
  AuthTokenResponseDto,
  LoginRequestDto,
  RegisterRequestDto,
  UserRole,
} from "@/types/auth";
import type { BookingPageResponseDto, BookingResponseDto, CreateBookingRequestDto } from "@/types/booking";
import type {
  ApplyGuideVerificationRequestDto,
  GuideVerificationPageResponseDto,
  GuideVerificationResponseDto,
  RejectGuideVerificationRequestDto,
  VerificationStatus,
} from "@/types/guideVerification";
import type { PlaceListPageResponseDto, PlaceResponseDto } from "@/types/place";

export function useLogin() {
  return useMutation({
    mutationFn: async (payload: LoginRequestDto) => {
      const response = await api.post<ApiResponse<AuthTokenResponseDto>>(
        "/api/v1/auth/login",
        payload,
      );
      const data = unwrapApiResponse(response.data);
      // Store auth data, preferring response fields over JWT decoding
      setAuthData(data.token, {
        role: data.role as UserRole | undefined,
        userId: data.userId ?? undefined,
        tokenVersion: data.tokenVersion ?? undefined,
      });
      return data;
    },
  });
}

export function useRegister() {
  return useMutation({
    mutationFn: async (payload: RegisterRequestDto) => {
      const response = await api.post<ApiResponse<AuthTokenResponseDto>>(
        "/api/v1/auth/register",
        payload,
      );
      const data = unwrapApiResponse(response.data);
      // Store auth data, preferring response fields over JWT decoding
      setAuthData(data.token, {
        role: data.role as UserRole | undefined,
        userId: data.userId ?? undefined,
        tokenVersion: data.tokenVersion ?? undefined,
      });
      return data;
    },
  });
}

export function usePlaces(page = 0, size = 10) {
  return useQuery({
    queryKey: ["places", page, size],
    queryFn: async () => {
      const response = await api.get<ApiResponse<PlaceListPageResponseDto>>(
        "/api/v1/places",
        { params: { page, size } },
      );
      return unwrapApiResponse(response.data);
    },
  });
}

export function usePlace(id: string | undefined) {
  return useQuery({
    queryKey: ["place", id],
    queryFn: async () => {
      const response = await api.get<ApiResponse<PlaceResponseDto>>(
        `/api/v1/places/${id}`,
      );
      return unwrapApiResponse(response.data);
    },
    enabled: !!id,
  });
}

export function useAdminVerifications(
  page = 0,
  size = 10,
  status?: VerificationStatus,
) {
  return useQuery({
    queryKey: ["admin-verifications", page, size, status],
    queryFn: async () => {
      const response = await api.get<ApiResponse<GuideVerificationPageResponseDto>>(
        "/api/v1/admin/verifications",
        { params: { page, size, ...(status ? { status } : {}) } },
      );
      return unwrapApiResponse(response.data);
    },
  });
}

export function useGuideMyVerification() {
  return useQuery({
    queryKey: ["guide-my-verification"],
    queryFn: async () => {
      const response = await api.get<ApiResponse<GuideVerificationResponseDto>>(
        "/api/v1/guides/verification/me",
      );
      return unwrapApiResponse(response.data);
    },
  });
}

export function useApplyGuideVerification() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (payload: ApplyGuideVerificationRequestDto) => {
      const response = await api.post<ApiResponse<GuideVerificationResponseDto>>(
        "/api/v1/guides/verification/apply",
        payload,
      );
      return unwrapApiResponse(response.data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["guide-my-verification"] });
      queryClient.invalidateQueries({ queryKey: ["admin-verifications"] });
    },
  });
}

export function useApproveVerification() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (verificationId: string) => {
      const response = await api.put<ApiResponse<GuideVerificationResponseDto>>(
        `/api/v1/admin/verifications/${verificationId}/approve`,
      );
      return unwrapApiResponse(response.data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-verifications"] });
    },
  });
}

export function useRejectVerification() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      verificationId,
      payload,
    }: {
      verificationId: string;
      payload: RejectGuideVerificationRequestDto;
    }) => {
      const response = await api.put<ApiResponse<GuideVerificationResponseDto>>(
        `/api/v1/admin/verifications/${verificationId}/reject`,
        payload,
      );
      return unwrapApiResponse(response.data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-verifications"] });
    },
  });
}

export function useCreateBooking() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: CreateBookingRequestDto) => {
      const response = await api.post<ApiResponse<BookingResponseDto>>(
        "/api/v1/bookings",
        payload,
      );
      return unwrapApiResponse(response.data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["my-bookings"] });
    },
  });
}

export function useAcceptBooking(bookingId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async () => {
      const response = await api.put<ApiResponse<BookingResponseDto>>(
        `/api/v1/bookings/${bookingId}/accept`,
      );
      return unwrapApiResponse(response.data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["my-bookings"] });
    },
  });
}

export function useMyBookings(page = 0, size = 10, all = true) {
  return useQuery({
    queryKey: ["my-bookings", page, size, all],
    queryFn: async () => {
      const response = await api.get<ApiResponse<BookingPageResponseDto>>(
        "/api/v1/bookings/my",
        { params: { page, size, all } },
      );
      return unwrapApiResponse(response.data);
    },
  });
}
