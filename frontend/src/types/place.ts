import type { PageResponse } from "@/types/api";

export type PlaceResponseDto = {
  id: string;
  title: string;
  description: string;
  address: string;
  mapUrl: string;
  createdAt: string;
};

export type PlaceListPageResponseDto = PageResponse<PlaceResponseDto>;
