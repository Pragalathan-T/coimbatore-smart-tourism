package com.yourorg.tourism.booking.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yourorg.tourism.booking.entity.BookingEntity;

public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {
}
