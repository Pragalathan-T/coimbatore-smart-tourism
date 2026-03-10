import { Badge } from "@/components/ui/badge";
import type { BookingStatus } from "./types";

const statusConfig: Record<
  BookingStatus,
  { label: string; variant: "default" | "secondary" | "destructive" | "outline" }
> = {
  REQUESTED: { label: "Requested", variant: "secondary" },
  ACCEPTED: { label: "Accepted", variant: "default" },
  CONFIRMED: { label: "Confirmed", variant: "default" },
  IN_PROGRESS: { label: "In Progress", variant: "default" },
  COMPLETED: { label: "Completed", variant: "outline" },
  RATED: { label: "Rated", variant: "outline" },
  CANCELLED: { label: "Cancelled", variant: "destructive" },
  REJECTED: { label: "Rejected", variant: "destructive" },
};

type BookingStatusBadgeProps = {
  status: BookingStatus;
  className?: string;
};

export function BookingStatusBadge({ status, className }: BookingStatusBadgeProps) {
  const config = statusConfig[status] ?? { label: status, variant: "secondary" as const };

  return (
    <Badge variant={config.variant} className={className}>
      {config.label}
    </Badge>
  );
}
