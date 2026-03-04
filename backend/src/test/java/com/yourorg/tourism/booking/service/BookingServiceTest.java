package com.yourorg.tourism.booking.service;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;

import com.yourorg.tourism.booking.entity.BookingEntity;
import com.yourorg.tourism.booking.entity.BookingStatus;
import com.yourorg.tourism.booking.repository.BookingRepository;
import com.yourorg.tourism.common.config.PaginationGuard;
import com.yourorg.tourism.common.exception.AppException;
import com.yourorg.tourism.common.exception.ErrorCode;
import com.yourorg.tourism.guide.service.GuideVerificationService;
import com.yourorg.tourism.user.entity.UserRole;
import com.yourorg.tourism.user.service.UserService;

class BookingServiceTest {

    @Test
    void touristCannotAcceptBooking() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        BookingService service = new BookingService(
                bookingRepository,
                mock(UserService.class),
                mock(GuideVerificationService.class),
                new PaginationGuard(100),
                mock(BookingAuditService.class)
        );

        AppException ex = assertThrows(AppException.class,
                () -> service.accept(UUID.randomUUID(), UUID.randomUUID(), UserRole.TOURIST));

        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
        verify(bookingRepository, never()).findById(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void guideCannotAcceptOthersBooking() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GuideVerificationService guideVerificationService = mock(GuideVerificationService.class);
        BookingService service = new BookingService(
                bookingRepository,
                mock(UserService.class),
                guideVerificationService,
                new PaginationGuard(100),
                mock(BookingAuditService.class)
        );

        UUID bookingId = UUID.randomUUID();
        UUID assignedGuideId = UUID.randomUUID();
        UUID requesterGuideId = UUID.randomUUID();
        BookingEntity booking = booking(bookingId, UUID.randomUUID(), assignedGuideId, BookingStatus.REQUESTED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        AppException ex = assertThrows(AppException.class,
                () -> service.accept(bookingId, requesterGuideId, UserRole.GUIDE));

        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
        verify(guideVerificationService, never()).assertGuideEligibleForBookingAcceptance(requesterGuideId);
    }

    @Test
    void guideWithoutVerificationCannotAccept() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GuideVerificationService guideVerificationService = mock(GuideVerificationService.class);
        BookingService service = new BookingService(
                bookingRepository,
                mock(UserService.class),
                guideVerificationService,
                new PaginationGuard(100),
                mock(BookingAuditService.class)
        );

        UUID bookingId = UUID.randomUUID();
        UUID guideId = UUID.randomUUID();
        BookingEntity booking = booking(bookingId, UUID.randomUUID(), guideId, BookingStatus.REQUESTED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        AppException forbidden = new AppException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN,
                "Guide verification is required to accept booking");
        org.mockito.Mockito.doThrow(forbidden)
                .when(guideVerificationService)
                .assertGuideEligibleForBookingAcceptance(guideId);

        AppException ex = assertThrows(AppException.class,
                () -> service.accept(bookingId, guideId, UserRole.GUIDE));

        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
        verify(bookingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void invalidTransitionShouldReturn409() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GuideVerificationService guideVerificationService = mock(GuideVerificationService.class);
        BookingService service = new BookingService(
                bookingRepository,
                mock(UserService.class),
                guideVerificationService,
                new PaginationGuard(100),
                mock(BookingAuditService.class)
        );

        UUID bookingId = UUID.randomUUID();
        UUID guideId = UUID.randomUUID();
        BookingEntity booking = booking(bookingId, UUID.randomUUID(), guideId, BookingStatus.ACCEPTED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        AppException ex = assertThrows(AppException.class,
                () -> service.accept(bookingId, guideId, UserRole.GUIDE));

        assertEquals(HttpStatus.CONFLICT, ex.getHttpStatus());
        assertEquals(ErrorCode.INVALID_STATE, ex.getErrorCode());
        verify(guideVerificationService, never()).assertGuideEligibleForBookingAcceptance(guideId);
    }

    @Test
    void bookingListShouldRejectTooLargePageSize() {
        BookingService service = new BookingService(
                mock(BookingRepository.class),
                mock(UserService.class),
                mock(GuideVerificationService.class),
                new PaginationGuard(100),
                mock(BookingAuditService.class)
        );

        AppException ex = assertThrows(AppException.class,
                () -> service.listMy(UUID.randomUUID(), UserRole.TOURIST, false, 0, 101));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    private BookingEntity booking(UUID id, UUID touristId, UUID guideId, BookingStatus status) {
        BookingEntity booking = new BookingEntity();
        booking.setId(id);
        booking.setTouristId(touristId);
        booking.setGuideId(guideId);
        booking.setStatus(status);
        return booking;
    }
}
