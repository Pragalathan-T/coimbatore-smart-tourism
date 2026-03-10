"use client";

import { use, useMemo } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { usePlace } from "@/hooks/useAuth";
import { getAuth } from "@/auth/authStore";
import { PlaceDetailSkeleton } from "@/features/places";
import { CreateBookingDialog } from "@/features/bookings";

type PlaceDetailPageProps = {
  params: Promise<{ id: string }>;
};

export default function PlaceDetailPage({ params }: PlaceDetailPageProps) {
  const { id } = use(params);
  const auth = useMemo(() => getAuth(), []);
  const { data: place, isLoading, error, refetch } = usePlace(id);

  const isTourist = auth.role === "TOURIST";

  if (isLoading) {
    return (
      <section className="space-y-4">
        <Link href="/places" className="text-sm text-muted-foreground hover:underline">
          ← Back to Places
        </Link>
        <PlaceDetailSkeleton />
      </section>
    );
  }

  if (error || !place) {
    return (
      <section className="space-y-4">
        <Link href="/places" className="text-sm text-muted-foreground hover:underline">
          ← Back to Places
        </Link>
        <div className="space-y-4 text-center">
          <p className="text-sm text-destructive">
            {error ? "Failed to load place details." : "Place not found."}
          </p>
          <Button variant="outline" onClick={() => refetch()}>
            Retry
          </Button>
        </div>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <Link href="/places" className="text-sm text-muted-foreground hover:underline">
        ← Back to Places
      </Link>

      <Card>
        <CardHeader>
          <CardTitle className="text-2xl">{place.title}</CardTitle>
          <CardDescription className="text-base">{place.address}</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-muted-foreground">{place.description}</p>

          <div className="flex flex-wrap items-center gap-3">
            {place.mapUrl && (
              <Button asChild variant="outline">
                <a href={place.mapUrl} target="_blank" rel="noreferrer">
                  View on Map
                </a>
              </Button>
            )}

            {isTourist && (
              <CreateBookingDialog
                placeId={place.id}
                placeName={place.title}
                trigger={<Button>Request a Booking</Button>}
              />
            )}
          </div>

          {!isTourist && (
            <p className="text-sm text-muted-foreground">
              Only tourists can request bookings. You are logged in as{" "}
              <span className="font-medium">{auth.role}</span>.
            </p>
          )}

          <p className="text-xs text-muted-foreground">
            Added: {new Date(place.createdAt).toLocaleDateString()}
          </p>
        </CardContent>
      </Card>
    </section>
  );
}
