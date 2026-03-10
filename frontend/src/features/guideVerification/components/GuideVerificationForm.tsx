"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { z } from "zod";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useApplyGuideVerification } from "@/hooks/useAuth";
import type { ApplyGuideVerificationRequestDto, DocumentType } from "@/types/guideVerification";

const documentTypeValues: DocumentType[] = ["AADHAR", "PASSPORT", "DRIVING_LICENSE"];

const applySchema = z.object({
  documentType: z.enum(["AADHAR", "PASSPORT", "DRIVING_LICENSE"]),
  documentNumber: z
    .string()
    .trim()
    .min(1, "Document number is required")
    .max(100, "Document number must be at most 100 characters"),
  documentUrl: z.string().trim().url("Document URL must be a valid URL"),
});

type ApplyFormValues = z.infer<typeof applySchema>;

type GuideVerificationFormProps = {
  onSuccess?: () => void;
};

export function GuideVerificationForm({ onSuccess }: GuideVerificationFormProps) {
  const applyMutation = useApplyGuideVerification();

  const form = useForm<ApplyFormValues>({
    resolver: zodResolver(applySchema),
    defaultValues: {
      documentType: "AADHAR",
      documentNumber: "",
      documentUrl: "",
    },
  });

  const onSubmit = async (values: ApplyFormValues) => {
    const payload: ApplyGuideVerificationRequestDto = {
      documentType: values.documentType,
      documentNumber: values.documentNumber.trim(),
      documentUrl: values.documentUrl.trim(),
    };

    try {
      await applyMutation.mutateAsync(payload);
      toast.success("Verification request submitted");
      form.reset({ documentType: "AADHAR", documentNumber: "", documentUrl: "" });
      onSuccess?.();
    } catch (error) {
      const message = error instanceof Error ? error.message : "Failed to submit verification";
      toast.error(message);
    }
  };

  return (
    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="documentType">Document Type</Label>
        <Controller
          control={form.control}
          name="documentType"
          render={({ field }) => (
            <Select value={field.value} onValueChange={(value) => field.onChange(value as DocumentType)}>
              <SelectTrigger id="documentType" className="w-full">
                <SelectValue placeholder="Select document type" />
              </SelectTrigger>
              <SelectContent>
                {documentTypeValues.map((type) => (
                  <SelectItem key={type} value={type}>
                    {type}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
        />
        {form.formState.errors.documentType ? (
          <p className="text-sm text-destructive">{form.formState.errors.documentType.message}</p>
        ) : null}
      </div>

      <div className="space-y-2">
        <Label htmlFor="documentNumber">Document Number</Label>
        <Input id="documentNumber" placeholder="Enter document number" {...form.register("documentNumber")} />
        {form.formState.errors.documentNumber ? (
          <p className="text-sm text-destructive">{form.formState.errors.documentNumber.message}</p>
        ) : null}
      </div>

      <div className="space-y-2">
        <Label htmlFor="documentUrl">Document URL</Label>
        <Input id="documentUrl" placeholder="https://..." {...form.register("documentUrl")} />
        {form.formState.errors.documentUrl ? (
          <p className="text-sm text-destructive">{form.formState.errors.documentUrl.message}</p>
        ) : null}
      </div>

      <Button type="submit" disabled={applyMutation.isPending}>
        {applyMutation.isPending ? "Submitting..." : "Submit Verification"}
      </Button>
    </form>
  );
}
