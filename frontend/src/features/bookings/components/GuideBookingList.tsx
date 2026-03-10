"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { useGuideBookings } from "../hooks";
import { BookingListSkeleton } from "../BookingSkeleton";
import { GuideBookingCard } from "./GuideBookingCard";

type GuideBookingListProps = {
  initialPage?: number;
  pageSize?: number;
};

export function GuideBookingList({ initialPage = 0, pageSize = 10 }: GuideBookingListProps) {
  const [page, setPage] = useState(initialPage);
  const { data, isLoading, isError, refetch } = useGuideBookings({
    page,
    size: pageSize,
    all: true,
  });

  if (isLoading) {
    return <BookingListSkeleton count={3} />;
  }

  if (isError) {
    return (
      <div className="space-y-4 text-center">
        <p className="text-sm text-destructive">Failed to load guide bookings.</p>
        <Button variant="outline" onClick={() => refetch()}>
          Retry
        </Button>
      </div>
    );
  }

  if (!data || data.content.length === 0) {
    return (
      <div className="py-12 text-center text-muted-foreground">
        <p className="text-lg font-medium">No booking requests yet</p>
        <p className="text-sm">New tourist booking requests will appear here.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="space-y-3">
        {data.content.map((booking) => (
          <GuideBookingCard key={booking.id} booking={booking} />
        ))}
      </div>

      {data.totalPages > 1 ? (
        <div className="flex items-center justify-center gap-2 pt-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
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
            onClick={() => setPage((prev) => prev + 1)}
            disabled={data.isLast}
          >
            Next
          </Button>
        </div>
      ) : null}
    </div>
  );
}
