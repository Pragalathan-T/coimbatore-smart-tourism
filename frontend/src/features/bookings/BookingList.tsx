"use client";

import { useState, useMemo } from "react";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useMyBookings } from "@/hooks/useAuth";
import { getAuth } from "@/auth/authStore";
import { BookingCard } from "./BookingCard";
import { BookingListSkeleton } from "./BookingSkeleton";
import type { BookingStatus } from "@/types/booking";

const STATUS_OPTIONS: Array<{ value: BookingStatus | "ALL"; label: string }> = [
  { value: "ALL", label: "All Statuses" },
  { value: "REQUESTED", label: "Requested" },
  { value: "ACCEPTED", label: "Accepted" },
  { value: "CONFIRMED", label: "Confirmed" },
  { value: "IN_PROGRESS", label: "In Progress" },
  { value: "COMPLETED", label: "Completed" },
  { value: "RATED", label: "Rated" },
  { value: "CANCELLED", label: "Cancelled" },
  { value: "REJECTED", label: "Rejected" },
];

type BookingListProps = {
  initialPage?: number;
  pageSize?: number;
};

export function BookingList({ initialPage = 0, pageSize = 10 }: BookingListProps) {
  const [page, setPage] = useState(initialPage);
  const [statusFilter, setStatusFilter] = useState<BookingStatus | "ALL">("ALL");

  const auth = useMemo(() => getAuth(), []);
  const { data, isLoading, error, refetch } = useMyBookings(page, pageSize, true);

  // Client-side filter for status
  const filteredBookings = useMemo(() => {
    const content = data?.content;
    if (!content) return [];
    if (statusFilter === "ALL") return content;
    return content.filter((b) => b.status === statusFilter);
  }, [data, statusFilter]);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="w-48">
          <Select disabled>
            <SelectTrigger>
              <SelectValue placeholder="Filter by status" />
            </SelectTrigger>
          </Select>
        </div>
        <BookingListSkeleton count={3} />
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-4 text-center">
        <p className="text-sm text-destructive">Failed to load bookings.</p>
        <Button variant="outline" onClick={() => refetch()}>
          Retry
        </Button>
      </div>
    );
  }

  const hasBookings = data?.content && data.content.length > 0;

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-4">
        <Select
          value={statusFilter}
          onValueChange={(val) => setStatusFilter(val as BookingStatus | "ALL")}
        >
          <SelectTrigger className="w-48">
            <SelectValue placeholder="Filter by status" />
          </SelectTrigger>
          <SelectContent>
            {STATUS_OPTIONS.map((opt) => (
              <SelectItem key={opt.value} value={opt.value}>
                {opt.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {statusFilter !== "ALL" && (
          <span className="text-sm text-muted-foreground">
            {filteredBookings.length} result{filteredBookings.length !== 1 ? "s" : ""}
          </span>
        )}
      </div>

      {!hasBookings ? (
        <div className="py-12 text-center text-muted-foreground">
          <p className="text-lg font-medium">No bookings yet</p>
          <p className="text-sm">
            {auth.role === "TOURIST"
              ? "Visit a place and request a booking to get started."
              : "Bookings from tourists will appear here."}
          </p>
        </div>
      ) : filteredBookings.length === 0 ? (
        <div className="py-12 text-center text-muted-foreground">
          <p className="text-lg font-medium">No {statusFilter.toLowerCase()} bookings</p>
          <p className="text-sm">Try selecting a different status filter.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {filteredBookings.map((booking) => (
            <BookingCard
              key={booking.id}
              booking={booking}
              currentRole={auth.role}
            />
          ))}
        </div>
      )}

      {/* Pagination */}
      {hasBookings && data && data.totalPages > 1 && (
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
