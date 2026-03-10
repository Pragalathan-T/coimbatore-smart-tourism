"use client";

import { BookingDetailsCard } from "./components/BookingDetailsCard";
import type { BookingResponseDto } from "@/types/booking";
import type { UserRole } from "@/types/auth";

type BookingCardProps = {
  booking: BookingResponseDto;
  currentRole: UserRole | null;
};

export function BookingCard({ booking, currentRole }: BookingCardProps) {
  const role = currentRole === "TOURIST" ? "TOURIST" : undefined;

  return <BookingDetailsCard booking={booking} role={role} />;
}
