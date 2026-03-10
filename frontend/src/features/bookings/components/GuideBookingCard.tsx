"use client";

import { BookingDetailsCard } from "./BookingDetailsCard";
import type { BookingResponseDto } from "../types";

type GuideBookingCardProps = {
  booking: BookingResponseDto;
};

export function GuideBookingCard({ booking }: GuideBookingCardProps) {
  return <BookingDetailsCard booking={booking} role="GUIDE" />;
}
