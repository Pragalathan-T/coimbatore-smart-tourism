package com.yourorg.tourism.booking.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourorg.tourism.booking.entity.BookingEntity;
import com.yourorg.tourism.booking.entity.BookingStatus;
import com.yourorg.tourism.booking.repository.BookingRepository;
import com.yourorg.tourism.common.exception.AppException;
import com.yourorg.tourism.common.exception.ErrorCode;
import com.yourorg.tourism.guide.service.GuideVerificationService;

@Service
public class BookingStatusService {

    private final BookingRepository bookingRepository;
    private final GuideVerificationService guideVerificationService;

    public BookingStatusService(BookingRepository bookingRepository, GuideVerificationService guideVerificationService) {
        this.bookingRepository = bookingRepository;
        this.guideVerificationService = guideVerificationService;
    }

    @Transactional
    public void markAccepted(UUID bookingId, UUID authenticatedGuideId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getGuideId() == null || !booking.getGuideId().equals(authenticatedGuideId)) {
            throw new AppException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Not allowed to accept this booking");
        }

        guideVerificationService.assertGuideEligibleForBookingAcceptance(authenticatedGuideId);
        booking.setStatus(BookingStatus.ACCEPTED);
        bookingRepository.save(booking);
    }
}
