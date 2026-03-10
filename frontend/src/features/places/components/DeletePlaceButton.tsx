"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { useDeletePlace } from "../hooks";

type DeletePlaceButtonProps = {
  placeId: string;
  placeTitle: string;
};

export function DeletePlaceButton({ placeId, placeTitle }: DeletePlaceButtonProps) {
  const [open, setOpen] = useState(false);
  const deleteMutation = useDeletePlace();

  const onDelete = async () => {
    try {
      await deleteMutation.mutateAsync(placeId);
      toast.success("Place deleted successfully");
      setOpen(false);
    } catch (error) {
      const message = error instanceof Error ? error.message : "Failed to delete place";
      toast.error(message);
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm" variant="destructive">Delete</Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Delete place</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete <strong>{placeTitle}</strong>? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => setOpen(false)}
            disabled={deleteMutation.isPending}
          >
            Cancel
          </Button>
          <Button
            type="button"
            variant="destructive"
            onClick={onDelete}
            disabled={deleteMutation.isPending}
          >
            {deleteMutation.isPending ? "Deleting..." : "Confirm Delete"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
