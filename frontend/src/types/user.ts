import type { UserRole } from "@/types/auth";

export type UserResponseDto = {
  id: string;
  firstName: string;
  lastName: string | null;
  email: string;
  role: UserRole;
  isActive: boolean;
  createdAt: string;
};
