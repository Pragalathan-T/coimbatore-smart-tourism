import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

export function BookingCardSkeleton() {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <Skeleton className="h-5 w-32" />
        <Skeleton className="h-5 w-20" />
      </CardHeader>
      <CardContent className="space-y-2">
        <Skeleton className="h-4 w-48" />
        <Skeleton className="h-4 w-36" />
        <Skeleton className="h-4 w-28" />
      </CardContent>
    </Card>
  );
}

export function BookingListSkeleton({ count = 3 }: { count?: number }) {
  return (
    <div className="space-y-3">
      {Array.from({ length: count }).map((_, i) => (
        <BookingCardSkeleton key={i} />
      ))}
    </div>
  );
}
