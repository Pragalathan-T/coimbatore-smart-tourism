"use client";

import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { useUpdateBookingStatus } from "../hooks";
import type { BookingStatus } from "../types";

export type BookingActionRole = "GUIDE" | "TOURIST" | "ADMIN";

type BookingActionBarProps = {
  booking: {
    id: string;
    status: BookingStatus;
  };
  role: BookingActionRole;
};

export function BookingActionBar({ booking, role }: BookingActionBarProps) {
  const updateStatusMutation = useUpdateBookingStatus();

  const canShowGuideActions = role === "GUIDE" && booking.status === "REQUESTED";

  const handleAction = async (action: "accept" | "reject") => {
    try {
      await updateStatusMutation.mutateAsync({ bookingId: booking.id, action });
      toast.success(
        action === "accept"
          ? "Booking accepted successfully"
          : "Booking rejected successfully"
      );
    } catch (error) {
      const message = error instanceof Error ? error.message : "Failed to update booking status";
      toast.error(message);
    }
  };

  if (!canShowGuideActions) {
    return null;
  }

  return (
    <div className="flex flex-wrap gap-2 pt-1">
      <Button
        size="sm"
        onClick={() => handleAction("accept")}
        disabled={updateStatusMutation.isPending}
      >
        Accept
      </Button>
      <Button
        size="sm"
        variant="outline"
        onClick={() => handleAction("reject")}
        disabled={updateStatusMutation.isPending}
      >
        Reject
      </Button>
    </div>
  );
}