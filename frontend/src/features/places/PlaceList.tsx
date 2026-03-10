"use client";

import { useState, useMemo } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { usePlaces } from "@/hooks/useAuth";
import { PlaceCard } from "./PlaceCard";
import { PlaceListSkeleton } from "./PlaceSkeleton";

type PlaceListProps = {
  initialPage?: number;
  pageSize?: number;
};

export function PlaceList({ initialPage = 0, pageSize = 20 }: PlaceListProps) {
  const [page, setPage] = useState(initialPage);
  const [search, setSearch] = useState("");

  const { data, isLoading, error, refetch } = usePlaces(page, pageSize);

  // Client-side search filter
  const filteredPlaces = useMemo(() => {
    const content = data?.content;
    if (!content) return [];
    if (!search.trim()) return content;

    const term = search.toLowerCase();
    return content.filter(
      (place) =>
        place.title.toLowerCase().includes(term) ||
        place.address.toLowerCase().includes(term) ||
        place.description.toLowerCase().includes(term)
    );
  }, [data, search]);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Input placeholder="Search places..." disabled className="max-w-sm" />
        <PlaceListSkeleton count={4} />
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-4 text-center">
        <p className="text-sm text-destructive">Failed to load places.</p>
        <Button variant="outline" onClick={() => refetch()}>
          Retry
        </Button>
      </div>
    );
  }

  const hasPlaces = data?.content && data.content.length > 0;

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-4">
        <Input
          placeholder="Search places by title, address, or description..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="max-w-sm"
        />
        {search && (
          <span className="text-sm text-muted-foreground">
            {filteredPlaces.length} result{filteredPlaces.length !== 1 ? "s" : ""}
          </span>
        )}
      </div>

      {!hasPlaces ? (
        <div className="py-12 text-center text-muted-foreground">
          <p className="text-lg font-medium">No places found</p>
          <p className="text-sm">Check back later for new destinations.</p>
        </div>
      ) : filteredPlaces.length === 0 ? (
        <div className="py-12 text-center text-muted-foreground">
          <p className="text-lg font-medium">No matches for &ldquo;{search}&rdquo;</p>
          <p className="text-sm">Try a different search term.</p>
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {filteredPlaces.map((place) => (
            <PlaceCard key={place.id} place={place} />
          ))}
        </div>
      )}

      {/* Pagination */}
      {hasPlaces && data && data.totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 pt-4">
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
      )}
    </div>
  );
}
