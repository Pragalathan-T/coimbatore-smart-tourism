"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useRegister } from "@/hooks/useAuth";
import { ApiClientError } from "@/lib/api";
import type { UserRole } from "@/types/auth";

const registerSchema = z.object({
  firstName: z.string().min(1, "First name is required").max(100),
  lastName: z.string().max(100).optional(),
  email: z.string().email("Enter a valid email").max(150),
  password: z
    .string()
    .min(8, "Password must be at least 8 characters")
    .max(100),
  idProofUrl: z.string().url("Must be a valid URL").optional().or(z.literal("")),
  selfieUrl: z.string().url("Must be a valid URL").optional().or(z.literal("")),
});

type RegisterFormValues = z.infer<typeof registerSchema>;

function getRedirectPathForRole(role: UserRole | null | undefined): string {
  switch (role) {
    case "ADMIN":
      return "/admin/verifications";
    case "GUIDE":
      return "/guide/verification";
    case "TOURIST":
    default:
      return "/places";
  }
}

export default function RegisterPage() {
  const router = useRouter();
  const registerMutation = useRegister();

  const form = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      firstName: "",
      lastName: "",
      email: "",
      password: "",
      idProofUrl: "",
      selfieUrl: "",
    },
  });

  const onSubmit = async (values: RegisterFormValues) => {
    try {
      const data = await registerMutation.mutateAsync({
        firstName: values.firstName,
        lastName: values.lastName || undefined,
        email: values.email,
        password: values.password,
        role: "TOURIST",
        idProofUrl: values.idProofUrl || undefined,
        selfieUrl: values.selfieUrl || undefined,
      });
      toast.success("Registration successful");
      // Redirect based on role from response
      const redirectPath = getRedirectPathForRole(data.role);
      router.replace(redirectPath);
    } catch (error) {
      const message =
        error instanceof ApiClientError
          ? `${error.message}${error.errorCode ? ` (${error.errorCode})` : ""}`
          : "Registration failed";
      toast.error(message);
    }
  };

  return (
    <main className="flex min-h-screen items-center justify-center p-6">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Register</CardTitle>
          <CardDescription>Create a Smart Tourism account (TOURIST).</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={form.handleSubmit(onSubmit)}>
            <div className="space-y-2">
              <Label htmlFor="firstName">First Name</Label>
              <Input id="firstName" {...form.register("firstName")} />
              {form.formState.errors.firstName ? (
                <p className="text-sm text-destructive">
                  {form.formState.errors.firstName.message}
                </p>
              ) : null}
            </div>

            <div className="space-y-2">
              <Label htmlFor="lastName">Last Name</Label>
              <Input id="lastName" {...form.register("lastName")} />
              {form.formState.errors.lastName ? (
                <p className="text-sm text-destructive">
                  {form.formState.errors.lastName.message}
                </p>
              ) : null}
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" {...form.register("email")} />
              {form.formState.errors.email ? (
                <p className="text-sm text-destructive">
                  {form.formState.errors.email.message}
                </p>
              ) : null}
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                {...form.register("password")}
              />
              {form.formState.errors.password ? (
                <p className="text-sm text-destructive">
                  {form.formState.errors.password.message}
                </p>
              ) : null}
            </div>

            <div className="space-y-2">
              <Label htmlFor="idProofUrl">ID Proof URL (optional)</Label>
              <Input id="idProofUrl" {...form.register("idProofUrl")} />
              {form.formState.errors.idProofUrl ? (
                <p className="text-sm text-destructive">
                  {form.formState.errors.idProofUrl.message}
                </p>
              ) : null}
            </div>

            <div className="space-y-2">
              <Label htmlFor="selfieUrl">Selfie URL (optional)</Label>
              <Input id="selfieUrl" {...form.register("selfieUrl")} />
              {form.formState.errors.selfieUrl ? (
                <p className="text-sm text-destructive">
                  {form.formState.errors.selfieUrl.message}
                </p>
              ) : null}
            </div>

            <Button
              type="submit"
              className="w-full"
              disabled={registerMutation.isPending}
            >
              {registerMutation.isPending ? "Creating account..." : "Register"}
            </Button>
          </form>

          <p className="mt-4 text-sm text-muted-foreground">
            Already have an account? <Link href="/login" className="underline">Login</Link>
          </p>
        </CardContent>
      </Card>
    </main>
  );
}
