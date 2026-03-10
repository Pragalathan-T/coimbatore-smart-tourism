"use client";

import { useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";

const rejectionSchema = z.object({
  rejectionReason: z
    .string()
    .trim()
    .min(3, "Rejection reason is required")
    .max(500, "Rejection reason must be at most 500 characters"),
});

type RejectionFormValues = z.infer<typeof rejectionSchema>;

type VerificationDecisionDialogProps = {
  trigger: React.ReactNode;
  title: string;
  description: string;
  confirmText: string;
  pending: boolean;
  requireReason?: boolean;
  onConfirm: (reason?: string) => Promise<void>;
};

export function VerificationDecisionDialog({
  trigger,
  title,
  description,
  confirmText,
  pending,
  requireReason = false,
  onConfirm,
}: VerificationDecisionDialogProps) {
  const [open, setOpen] = useState(false);

  const form = useForm<RejectionFormValues>({
    resolver: zodResolver(rejectionSchema),
    defaultValues: {
      rejectionReason: "",
    },
  });

  const submit = async (values?: RejectionFormValues) => {
    await onConfirm(values?.rejectionReason);
    form.reset();
    setOpen(false);
  };

  return (
    <Dialog
      open={open}
      onOpenChange={(nextOpen) => {
        setOpen(nextOpen);
        if (!nextOpen) {
          form.reset();
        }
      }}
    >
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>

        {requireReason ? (
          <form className="space-y-3" onSubmit={form.handleSubmit((values) => submit(values))}>
            <div className="space-y-2">
              <Label htmlFor="rejectionReason">Rejection Reason</Label>
              <Textarea
                id="rejectionReason"
                placeholder="Provide reason for rejection"
                {...form.register("rejectionReason")}
              />
              {form.formState.errors.rejectionReason ? (
                <p className="text-sm text-destructive">
                  {form.formState.errors.rejectionReason.message}
                </p>
              ) : null}
            </div>
            <DialogFooter>
              <Button type="submit" variant="destructive" disabled={pending}>
                {pending ? "Submitting..." : confirmText}
              </Button>
            </DialogFooter>
          </form>
        ) : (
          <DialogFooter>
            <Button type="button" onClick={() => submit()} disabled={pending}>
              {pending ? "Submitting..." : confirmText}
            </Button>
          </DialogFooter>
        )}
      </DialogContent>
    </Dialog>
  );
}
