"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useCreateBooking } from "@/hooks/useAuth";
import { ApiClientError } from "@/lib/api";

const bookingSchema = z
  .object({
    startDateTime: z.string().min(1, "Start date and time is required"),
    endDateTime: z.string().min(1, "End date and time is required"),
    notes: z.string().optional(),
  })
  .refine(
    (data) => {
      const start = new Date(data.startDateTime);
      const end = new Date(data.endDateTime);
      return end > start;
    },
    { message: "End time must be after start time", path: ["endDateTime"] }
  )
  .refine(
    (data) => {
      const start = new Date(data.startDateTime);
      return start > new Date();
    },
    { message: "Start time must be in the future", path: ["startDateTime"] }
  );

type BookingFormValues = z.infer<typeof bookingSchema>;

type CreateBookingDialogProps = {
  placeId: string;
  placeName: string;
  trigger?: React.ReactNode;
};

export function CreateBookingDialog({
  placeId,
  placeName,
  trigger,
}: CreateBookingDialogProps) {
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const createMutation = useCreateBooking();

  const form = useForm<BookingFormValues>({
    resolver: zodResolver(bookingSchema),
    defaultValues: {
      startDateTime: "",
      endDateTime: "",
      notes: "",
    },
  });

  const onSubmit = async (values: BookingFormValues) => {
    try {
      await createMutation.mutateAsync({
        placeId,
        startDateTime: new Date(values.startDateTime).toISOString(),
        endDateTime: new Date(values.endDateTime).toISOString(),
        notes: values.notes || undefined,
      });
      toast.success("Booking request submitted!");
      setOpen(false);
      form.reset();
      router.push("/bookings");
    } catch (error) {
      const message =
        error instanceof ApiClientError
          ? error.message
          : "Failed to create booking request";
      toast.error(message);
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        {trigger ?? <Button>Request Booking</Button>}
      </DialogTrigger>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Request a Booking</DialogTitle>
          <DialogDescription>
            Request a visit to <strong>{placeName}</strong>. A guide will review
            and respond to your request.
          </DialogDescription>
        </DialogHeader>

        <form className="space-y-4" onSubmit={form.handleSubmit(onSubmit)}>
          <div className="space-y-2">
            <Label htmlFor="startDateTime">Start Date & Time</Label>
            <Input
              id="startDateTime"
              type="datetime-local"
              {...form.register("startDateTime")}
            />
            {form.formState.errors.startDateTime && (
              <p className="text-sm text-destructive">
                {form.formState.errors.startDateTime.message}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="endDateTime">End Date & Time</Label>
            <Input
              id="endDateTime"
              type="datetime-local"
              {...form.register("endDateTime")}
            />
            {form.formState.errors.endDateTime && (
              <p className="text-sm text-destructive">
                {form.formState.errors.endDateTime.message}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="notes">Notes (optional)</Label>
            <Textarea
              id="notes"
              placeholder="Any special requests or requirements..."
              {...form.register("notes")}
            />
            <p className="text-xs text-muted-foreground">
              Add any additional information for your visit.
            </p>
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => setOpen(false)}
              disabled={createMutation.isPending}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={createMutation.isPending}>
              {createMutation.isPending ? "Submitting..." : "Submit Request"}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
