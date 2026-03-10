import { api, ApiClientError, unwrapApiResponse } from "@/lib/api";
import type { ApiResponse } from "@/types/api";
import type {
  CreatePlaceRequestDto,
  PlaceDto,
  PlaceListResponse,
  UpdatePlaceRequestDto,
} from "./types";

export async function getPlaces(
  page = 0,
  size = 10
): Promise<PlaceListResponse> {
  const response = await api.get<ApiResponse<PlaceListResponse>>(
    "/api/v1/places",
    { params: { page, size } }
  );
  return unwrapApiResponse(response.data);
}

export async function getPlaceById(id: string): Promise<PlaceDto> {
  const response = await api.get<ApiResponse<PlaceDto>>(
    `/api/v1/places/${id}`
  );
  return unwrapApiResponse(response.data);
}

export async function getAdminPlaces(
  page = 0,
  size = 10
): Promise<PlaceListResponse> {
  const response = await api.get<ApiResponse<PlaceListResponse>>(
    "/api/v1/places",
    { params: { page, size } }
  );
  return unwrapApiResponse(response.data);
}

export async function createPlace(
  payload: CreatePlaceRequestDto
): Promise<PlaceDto> {
  const response = await api.post<ApiResponse<PlaceDto>>(
    "/api/v1/admin/places",
    payload
  );
  return unwrapApiResponse(response.data);
}

export async function updatePlace(
  id: string,
  payload: UpdatePlaceRequestDto
): Promise<PlaceDto> {
  const response = await api.put<ApiResponse<PlaceDto>>(
    `/api/v1/admin/places/${id}`,
    payload
  );
  return unwrapApiResponse(response.data);
}

export async function deletePlace(id: string): Promise<void> {
  const response = await api.delete<ApiResponse<null>>(
    `/api/v1/admin/places/${id}`
  );
  if (response.data.status === "ERROR") {
    throw new ApiClientError(response.data.message, response.data.errorCode);
  }
}
