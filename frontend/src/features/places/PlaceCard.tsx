import Link from "next/link";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import type { PlaceResponseDto } from "@/types/place";

type PlaceCardProps = {
  place: PlaceResponseDto;
  showDetails?: boolean;
};

export function PlaceCard({ place, showDetails = true }: PlaceCardProps) {
  return (
    <Card className="flex flex-col">
      <CardHeader>
        <CardTitle className="line-clamp-1">{place.title}</CardTitle>
        <CardDescription className="line-clamp-1">{place.address}</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-1 flex-col space-y-3 text-sm text-muted-foreground">
        <p className="line-clamp-3 flex-1">{place.description}</p>
        <div className="flex items-center gap-2">
          {showDetails && (
            <Button asChild variant="default" size="sm">
              <Link href={`/places/${place.id}`}>View Details</Link>
            </Button>
          )}
          {place.mapUrl && (
            <Button asChild variant="outline" size="sm">
              <a href={place.mapUrl} target="_blank" rel="noreferrer">
                Map
              </a>
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
