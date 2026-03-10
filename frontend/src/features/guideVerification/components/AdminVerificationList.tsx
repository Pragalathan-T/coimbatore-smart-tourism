"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useAdminVerifications } from "@/hooks/useAuth";
import type { VerificationStatus } from "@/types/guideVerification";
import { AdminVerificationCard } from "./AdminVerificationCard";

const STATUS_FILTERS: VerificationStatus[] = ["PENDING", "APPROVED", "REJECTED"];

type AdminVerificationListProps = {
  initialPage?: number;
  pageSize?: number;
};

export function AdminVerificationList({ initialPage = 0, pageSize = 10 }: AdminVerificationListProps) {
  const [page, setPage] = useState(initialPage);
  const [status, setStatus] = useState<VerificationStatus>("PENDING");

  const { data, isLoading, error, refetch } = useAdminVerifications(page, pageSize, status);

  if (isLoading) {
    return <p className="text-sm text-muted-foreground">Loading verification requests...</p>;
  }

  if (error) {
    return (
      <div className="space-y-3 text-center">
        <p className="text-sm text-destructive">Failed to load verification requests.</p>
        <Button variant="outline" onClick={() => refetch()}>
          Retry
        </Button>
      </div>
    );
  }

  const items = data?.content ?? [];

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <Select
          value={status}
          onValueChange={(value) => {
            setStatus(value as VerificationStatus);
            setPage(0);
          }}
        >
          <SelectTrigger className="w-[220px]">
            <SelectValue placeholder="Filter status" />
          </SelectTrigger>
          <SelectContent>
            {STATUS_FILTERS.map((item) => (
              <SelectItem key={item} value={item}>
                {item}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {items.length === 0 ? (
        <div className="py-10 text-center text-muted-foreground">
          <p className="text-lg font-medium">No verification requests found</p>
          <p className="text-sm">Try changing the status filter.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {items.map((verification) => (
            <AdminVerificationCard key={verification.id} verification={verification} />
          ))}
        </div>
      )}

      {data && data.totalPages > 1 ? (
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
