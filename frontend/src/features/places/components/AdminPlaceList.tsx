"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { useAdminPlaces } from "../hooks";
import { PlaceListSkeleton } from "../PlaceSkeleton";
import { AdminPlaceCard } from "./AdminPlaceCard";

type AdminPlaceListProps = {
  initialPage?: number;
  pageSize?: number;
};

export function AdminPlaceList({ initialPage = 0, pageSize = 10 }: AdminPlaceListProps) {
  const [page, setPage] = useState(initialPage);
  const { data, isLoading, isError, refetch } = useAdminPlaces(page, pageSize);

  if (isLoading) {
    return <PlaceListSkeleton count={4} />;
  }

  if (isError) {
    return (
      <div className="space-y-4 text-center">
        <p className="text-sm text-destructive">Failed to load places.</p>
        <Button variant="outline" onClick={() => refetch()}>
          Retry
        </Button>
      </div>
    );
  }

  if (!data || data.content.length === 0) {
    return (
      <div className="py-12 text-center text-muted-foreground">
        <p className="text-lg font-medium">No places yet</p>
        <p className="text-sm">Create your first place to get started.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="grid gap-4 md:grid-cols-2">
        {data.content.map((place) => (
          <AdminPlaceCard key={place.id} place={place} />
        ))}
      </div>

      {data.totalPages > 1 ? (
        <div className="flex items-center justify-center gap-2 pt-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={data.isFirst}
          >
            Previous
          </Button>
          <span className="text-sm text-muted-foreground">
            Page {data.page + 1} of {data.totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            onClick={() => setPage((p) => p + 1)}
            disabled={data.isLast}
          >
            Next
          </Button>
        </div>
      ) : null}
    </div>
  );
}
