"use client";

import { useEffect } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useCreatePlace, useUpdatePlace } from "../hooks";
import type { PlaceDto } from "../types";

const placeSchema = z.object({
  title: z
    .string()
    .trim()
    .min(1, "Title is required")
    .max(200, "Title must be at most 200 characters"),
  description: z
    .string()
    .trim()
    .max(1000, "Description must be at most 1000 characters")
    .optional()
    .or(z.literal("")),
  address: z
    .string()
    .trim()
    .max(255, "Address must be at most 255 characters")
    .optional()
    .or(z.literal("")),
  mapUrl: z
    .string()
    .trim()
    .max(2048, "Map URL must be at most 2048 characters")
    .optional()
    .or(z.literal(""))
    .refine((value) => !value || z.string().url().safeParse(value).success, {
      message: "Map URL must be a valid URL",
    }),
});

type PlaceFormValues = z.infer<typeof placeSchema>;

type AdminPlaceFormProps = {
  mode: "create" | "edit";
  place?: PlaceDto;
  onSuccess?: () => void;
  onCancel?: () => void;
};

export function AdminPlaceForm({ mode, place, onSuccess, onCancel }: AdminPlaceFormProps) {
  const createPlaceMutation = useCreatePlace();
  const updatePlaceMutation = useUpdatePlace();

  const isPending = createPlaceMutation.isPending || updatePlaceMutation.isPending;

  const form = useForm<PlaceFormValues>({
    resolver: zodResolver(placeSchema),
    defaultValues: {
      title: place?.title ?? "",
      description: place?.description ?? "",
      address: place?.address ?? "",
      mapUrl: place?.mapUrl ?? "",
    },
  });

  useEffect(() => {
    form.reset({
      title: place?.title ?? "",
      description: place?.description ?? "",
      address: place?.address ?? "",
      mapUrl: place?.mapUrl ?? "",
    });
  }, [form, place]);

  const onSubmit = async (values: PlaceFormValues) => {
    const payload = {
      title: values.title.trim(),
      description: values.description?.trim() || undefined,
      address: values.address?.trim() || undefined,
      mapUrl: values.mapUrl?.trim() || undefined,
    };

    try {
      if (mode === "create") {
        await createPlaceMutation.mutateAsync(payload);
        toast.success("Place created successfully");
        form.reset({ title: "", description: "", address: "", mapUrl: "" });
      } else if (place) {
        await updatePlaceMutation.mutateAsync({ id: place.id, payload });
        toast.success("Place updated successfully");
      }
      onSuccess?.();
    } catch (error) {
      const message = error instanceof Error ? error.message : "Failed to save place";
      toast.error(message);
    }
  };

  return (
    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="title">Title</Label>
        <Input id="title" placeholder="Enter place title" {...form.register("title")} />
        {form.formState.errors.title ? (
          <p className="text-sm text-destructive">{form.formState.errors.title.message}</p>
        ) : null}
      </div>

      <div className="space-y-2">
        <Label htmlFor="description">Description</Label>
        <Textarea
          id="description"
          placeholder="Describe this place"
          {...form.register("description")}
        />
        {form.formState.errors.description ? (
          <p className="text-sm text-destructive">{form.formState.errors.description.message}</p>
        ) : null}
      </div>

      <div className="space-y-2">
        <Label htmlFor="address">Address</Label>
        <Input id="address" placeholder="Address" {...form.register("address")} />
        {form.formState.errors.address ? (
          <p className="text-sm text-destructive">{form.formState.errors.address.message}</p>
        ) : null}
      </div>

      <div className="space-y-2">
        <Label htmlFor="mapUrl">Map URL</Label>
        <Input id="mapUrl" placeholder="https://maps.google.com/..." {...form.register("mapUrl")} />
        {form.formState.errors.mapUrl ? (
          <p className="text-sm text-destructive">{form.formState.errors.mapUrl.message}</p>
        ) : null}
      </div>

      <div className="flex justify-end gap-2">
        {onCancel ? (
          <Button type="button" variant="outline" onClick={onCancel} disabled={isPending}>
            Cancel
          </Button>
        ) : null}
        <Button type="submit" disabled={isPending}>
          {isPending ? "Saving..." : mode === "create" ? "Create Place" : "Update Place"}
        </Button>
      </div>
    </form>
  );
}
