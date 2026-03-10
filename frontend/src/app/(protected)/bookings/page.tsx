"use client";

import { BookingList } from "@/features/bookings";

export default function BookingsPage() {
  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-semibold">My Bookings</h1>
      <BookingList />
    </section>
  );
}
