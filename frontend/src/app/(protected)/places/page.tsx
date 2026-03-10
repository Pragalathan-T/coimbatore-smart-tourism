"use client";

import { PlaceList } from "@/features/places";

export default function PlacesPage() {
  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-semibold">Places to Visit</h1>
      <PlaceList />
    </section>
  );
}
