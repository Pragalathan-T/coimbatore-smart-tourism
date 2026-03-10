"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { AdminPlaceForm, AdminPlaceList } from "@/features/places";

export default function AdminPlacesPage() {
  const [createOpen, setCreateOpen] = useState(false);

  return (
    <section className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="space-y-1">
          <h1 className="text-2xl font-semibold">Admin Place Management</h1>
          <p className="text-sm text-muted-foreground">
            Create, edit, and remove places available in Smart Tourism.
          </p>
        </div>

        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <DialogTrigger asChild>
            <Button>Create Place</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Create Place</DialogTitle>
            </DialogHeader>
            <AdminPlaceForm
              mode="create"
              onSuccess={() => setCreateOpen(false)}
              onCancel={() => setCreateOpen(false)}
            />
          </DialogContent>
        </Dialog>
      </div>

      <AdminPlaceList />
    </section>
  );
}
