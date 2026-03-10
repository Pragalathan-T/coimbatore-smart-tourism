import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  createPlace,
  deletePlace,
  getAdminPlaces,
  getPlaceById,
  getPlaces,
  updatePlace,
} from "./api";
import type {
  CreatePlaceRequestDto,
  PlaceDto,
  PlaceListResponse,
  UpdatePlaceRequestDto,
} from "./types";

export const placeKeys = {
  all: ["places"] as const,
  lists: () => [...placeKeys.all, "list"] as const,
  list: (page: number, size: number) => [...placeKeys.lists(), page, size] as const,
  details: () => [...placeKeys.all, "detail"] as const,
  detail: (id: string) => [...placeKeys.details(), id] as const,
  admin: ["admin-places"] as const,
  adminList: (page: number, size: number) => [...placeKeys.admin, page, size] as const,
};

export function usePlaces(page = 0, size = 10) {
  return useQuery<PlaceListResponse>({
    queryKey: placeKeys.list(page, size),
    queryFn: () => getPlaces(page, size),
  });
}

export function usePlace(id: string | undefined) {
  return useQuery<PlaceDto>({
    queryKey: placeKeys.detail(id!),
    queryFn: () => getPlaceById(id!),
    enabled: !!id,
  });
}

export function useAdminPlaces(page = 0, size = 10) {
  return useQuery<PlaceListResponse>({
    queryKey: placeKeys.adminList(page, size),
    queryFn: () => getAdminPlaces(page, size),
  });
}

export function useCreatePlace() {
  const queryClient = useQueryClient();

  return useMutation<PlaceDto, Error, CreatePlaceRequestDto>({
    mutationFn: (payload) => createPlace(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: placeKeys.admin });
      queryClient.invalidateQueries({ queryKey: placeKeys.all });
    },
  });
}

export function useUpdatePlace() {
  const queryClient = useQueryClient();

  return useMutation<
    PlaceDto,
    Error,
    { id: string; payload: UpdatePlaceRequestDto }
  >({
    mutationFn: ({ id, payload }) => updatePlace(id, payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: placeKeys.admin });
      queryClient.invalidateQueries({ queryKey: placeKeys.all });
      queryClient.setQueryData(placeKeys.detail(data.id), data);
    },
  });
}

export function useDeletePlace() {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (id) => deletePlace(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: placeKeys.admin });
      queryClient.invalidateQueries({ queryKey: placeKeys.all });
    },
  });
}
