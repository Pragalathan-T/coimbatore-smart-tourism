"use client";

import { GuideBookingList } from "@/features/bookings/components/GuideBookingList";

export default function GuideBookingsPage() {
  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-semibold">Guide Booking Requests</h1>
      <p className="text-sm text-muted-foreground">
        Review incoming booking requests from tourists and accept or reject them.
      </p>
      <GuideBookingList />
    </section>
  );
}
