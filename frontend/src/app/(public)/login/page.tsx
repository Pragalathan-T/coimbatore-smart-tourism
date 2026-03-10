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
import { useLogin } from "@/hooks/useAuth";
import { ApiClientError } from "@/lib/api";
import type { UserRole } from "@/types/auth";

const loginSchema = z.object({
  email: z.string().email("Enter a valid email"),
  password: z.string().min(1, "Password is required"),
});

type LoginFormValues = z.infer<typeof loginSchema>;

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

export default function LoginPage() {
  const router = useRouter();
  const loginMutation = useLogin();

  const form = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const onSubmit = async (values: LoginFormValues) => {
    try {
      const data = await loginMutation.mutateAsync(values);
      toast.success("Login successful");
      // Redirect based on role from response
      const redirectPath = getRedirectPathForRole(data.role);
      router.replace(redirectPath);
    } catch (error) {
      const message =
        error instanceof ApiClientError
          ? `${error.message}${error.errorCode ? ` (${error.errorCode})` : ""}`
          : "Login failed";
      toast.error(message);
    }
  };

  return (
    <main className="flex min-h-screen items-center justify-center p-6">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Login</CardTitle>
          <CardDescription>Access your Smart Tourism account.</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={form.handleSubmit(onSubmit)}>
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

            <Button type="submit" className="w-full" disabled={loginMutation.isPending}>
              {loginMutation.isPending ? "Signing in..." : "Login"}
            </Button>
          </form>

          <p className="mt-4 text-sm text-muted-foreground">
            New here? <Link href="/register" className="underline">Register</Link>
          </p>
        </CardContent>
      </Card>
    </main>
  );
}
