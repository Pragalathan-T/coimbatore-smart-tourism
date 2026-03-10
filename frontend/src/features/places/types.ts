import type { PageResponse } from "@/types/api";

export type PlaceDto = {
  id: string;
  title: string;
  description: string;
  address: string;
  mapUrl: string;
  createdAt: string;
};

export type PlaceListResponse = PageResponse<PlaceDto>;

export type CreatePlaceRequestDto = {
  title: string;
  description?: string;
  address?: string;
  mapUrl?: string;
};

export type UpdatePlaceRequestDto = {
  title?: string;
  description?: string;
  address?: string;
  mapUrl?: string;
};
