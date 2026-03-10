"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { BookingStatusBadge } from "../BookingStatusBadge";
import { BookingActionBar, type BookingActionRole } from "./BookingActionBar";
import type { BookingResponseDto as FeatureBookingResponseDto } from "../types";
import type { BookingResponseDto as AppBookingResponseDto } from "@/types/booking";

type BookingDetails = FeatureBookingResponseDto | AppBookingResponseDto;

type BookingDetailsCardProps = {
  booking: BookingDetails;
  role?: BookingActionRole;
};

function formatDateTime(value: string | null | undefined): string {
  if (!value) return "Not available";
  try {
    return new Date(value).toLocaleString(undefined, {
      dateStyle: "medium",
      timeStyle: "short",
    });
  } catch {
    return value;
  }
}

function getStartDateTime(booking: BookingDetails): string | null | undefined {
  if (booking.startDateTime) return booking.startDateTime;
  return "scheduledAt" in booking ? booking.scheduledAt : null;
}

function getTouristDisplayName(booking: BookingDetails): string {
  if ("touristName" in booking && booking.touristName) {
    return booking.touristName;
  }
  return booking.touristId;
}

export function BookingDetailsCard({ booking, role }: BookingDetailsCardProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0">
        <CardTitle className="text-base font-medium">
          Booking #{booking.id.slice(0, 8)}...
        </CardTitle>
        <BookingStatusBadge status={booking.status} />
      </CardHeader>
      <CardContent className="space-y-3 text-sm text-muted-foreground">
        <div className="grid gap-1">
          <p>
            <span className="font-medium text-foreground">Tourist:</span>{" "}
            {getTouristDisplayName(booking)}
          </p>
          <p>
            <span className="font-medium text-foreground">Place:</span>{" "}
            {booking.placeName ?? booking.placeId ?? "Not available"}
          </p>
          <p>
            <span className="font-medium text-foreground">Start:</span>{" "}
            {formatDateTime(getStartDateTime(booking))}
          </p>
          <p>
            <span className="font-medium text-foreground">End:</span>{" "}
            {formatDateTime(booking.endDateTime)}
          </p>
          {booking.notes ? (
            <p>
              <span className="font-medium text-foreground">Notes:</span>{" "}
              {booking.notes}
            </p>
          ) : null}
          <p className="text-xs">
            Created: {formatDateTime(booking.createdAt)}
          </p>
        </div>
        {role ? <BookingActionBar booking={booking} role={role} /> : null}
      </CardContent>
    </Card>
  );
}
