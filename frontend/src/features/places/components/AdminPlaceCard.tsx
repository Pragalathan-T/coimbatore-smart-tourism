"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import type { PlaceDto } from "../types";
import { AdminPlaceForm } from "./AdminPlaceForm";
import { DeletePlaceButton } from "./DeletePlaceButton";

type AdminPlaceCardProps = {
  place: PlaceDto;
};

function formatDate(value: string): string {
  try {
    return new Date(value).toLocaleString(undefined, {
      dateStyle: "medium",
      timeStyle: "short",
    });
  } catch {
    return value;
  }
}

export function AdminPlaceCard({ place }: AdminPlaceCardProps) {
  const [editOpen, setEditOpen] = useState(false);

  return (
    <Card>
      <CardHeader>
        <CardTitle className="line-clamp-1">{place.title}</CardTitle>
        <CardDescription>{place.address || "Address not provided"}</CardDescription>
      </CardHeader>
      <CardContent className="space-y-3 text-sm text-muted-foreground">
        <p className="line-clamp-3">{place.description || "No description provided"}</p>

        {place.mapUrl ? (
          <p>
            <span className="font-medium text-foreground">Map:</span>{" "}
            <a
              href={place.mapUrl}
              target="_blank"
              rel="noreferrer"
              className="underline underline-offset-4"
            >
              Open link
            </a>
          </p>
        ) : (
          <p>
            <span className="font-medium text-foreground">Map:</span> Not provided
          </p>
        )}

        <p>
          <span className="font-medium text-foreground">Created:</span> {formatDate(place.createdAt)}
        </p>

        <div className="flex items-center gap-2 pt-1">
          <Dialog open={editOpen} onOpenChange={setEditOpen}>
            <DialogTrigger asChild>
              <Button size="sm" variant="outline">Edit</Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Edit Place</DialogTitle>
              </DialogHeader>
              <AdminPlaceForm mode="edit" place={place} onSuccess={() => setEditOpen(false)} onCancel={() => setEditOpen(false)} />
            </DialogContent>
          </Dialog>

          <DeletePlaceButton placeId={place.id} placeTitle={place.title} />
        </div>
      </CardContent>
    </Card>
  );
}
