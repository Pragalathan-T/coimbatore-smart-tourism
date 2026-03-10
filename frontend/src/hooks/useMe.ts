import { useQuery } from "@tanstack/react-query";
import { getAuth } from "@/auth/authStore";
import { api, unwrapApiResponse } from "@/lib/api";
import type { ApiResponse } from "@/types/api";
import type { UserResponseDto } from "@/types/user";

export function useMe() {
  const { userId } = getAuth();

  return useQuery({
    queryKey: ["me", userId],
    enabled: Boolean(userId),
    queryFn: async () => {
      const response = await api.get<ApiResponse<UserResponseDto>>(
        `/api/v1/users/${userId}`,
      );
      return unwrapApiResponse(response.data);
    },
  });
}
