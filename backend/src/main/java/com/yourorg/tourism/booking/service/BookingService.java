package com.yourorg.tourism.booking.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourorg.tourism.booking.dto.BookingResponseDto;
import com.yourorg.tourism.booking.dto.CreateBookingRequestDto;
import com.yourorg.tourism.booking.entity.BookingEntity;
import com.yourorg.tourism.booking.entity.BookingStatus;
import com.yourorg.tourism.booking.repository.BookingRepository;
import com.yourorg.tourism.common.config.PaginationGuard;
import com.yourorg.tourism.common.exception.AppException;
import com.yourorg.tourism.common.exception.ErrorCode;
import com.yourorg.tourism.common.response.PageResponse;
import com.yourorg.tourism.guide.service.GuideVerificationService;
import com.yourorg.tourism.user.dto.UserResponseDto;
import com.yourorg.tourism.user.entity.UserRole;
import com.yourorg.tourism.user.service.UserService;

@Service
public class BookingService {

    private static final Set<BookingStatus> CANCELLABLE_STATES = Set.of(
            BookingStatus.REQUESTED,
            BookingStatus.ACCEPTED,
            BookingStatus.CONFIRMED
    );

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final GuideVerificationService guideVerificationService;
    private final PaginationGuard paginationGuard;
    private final BookingAuditService bookingAuditService;

    public BookingService(
            BookingRepository bookingRepository,
            UserService userService,
            GuideVerificationService guideVerificationService,
            PaginationGuard paginationGuard,
            BookingAuditService bookingAuditService
    ) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.guideVerificationService = guideVerificationService;
        this.paginationGuard = paginationGuard;
        this.bookingAuditService = bookingAuditService;
    }

    @Transactional
    public BookingResponseDto create(CreateBookingRequestDto request, UUID requesterId, UserRole requesterRole) {
        ensureRole(requesterRole, UserRole.TOURIST, "Only tourists can create bookings");

        UserResponseDto guide = userService.getById(request.guideId());
        if (guide.role() != UserRole.GUIDE || !guide.isActive()) {
            throw new AppException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Guide is not available");
        }

        BookingEntity booking = new BookingEntity();
        booking.setTouristId(requesterId);
        booking.setGuideId(request.guideId());
        booking.setPlaceId(request.placeId());
        booking.setScheduledAt(request.scheduledAt());
        booking.setStatus(BookingStatus.REQUESTED);

        BookingEntity saved = bookingRepository.save(booking);
        bookingAuditService.record(requesterId, "BOOKING_CREATED", saved.getId(), "status=REQUESTED");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingResponseDto> listMy(UUID requesterId, UserRole requesterRole, boolean all, int page, int size) {
        paginationGuard.validateSize(size);

        Page<BookingEntity> bookingPage;
        if (requesterRole == UserRole.ADMIN && all) {
            bookingPage = bookingRepository.findAll(PageRequest.of(page, size));
        } else if (requesterRole == UserRole.GUIDE) {
            bookingPage = bookingRepository.findAllByGuideId(requesterId, PageRequest.of(page, size));
        } else if (requesterRole == UserRole.TOURIST) {
            bookingPage = bookingRepository.findAllByTouristId(requesterId, PageRequest.of(page, size));
        } else {
            bookingPage = bookingRepository.findAllByTouristIdOrGuideId(requesterId, requesterId, PageRequest.of(page, size));
        }

        return PageResponse.from(bookingPage.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public BookingResponseDto getById(UUID bookingId, UUID requesterId, UserRole requesterRole) {
        BookingEntity booking = getBookingOrThrow(bookingId);

        boolean isOwner = requesterId.equals(booking.getTouristId()) || requesterId.equals(booking.getGuideId());
        if (!isOwner && requesterRole != UserRole.ADMIN) {
            throw new AppException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Forbidden");
        }

        return toResponse(booking);
    }

    @Transactional
    public BookingResponseDto accept(UUID bookingId, UUID requesterId, UserRole requesterRole) {
        ensureRole(requesterRole, UserRole.GUIDE, "Only guides can accept bookings");
        BookingEntity booking = getBookingOrThrow(bookingId);
        ensureGuideOwner(booking, requesterId);
        assertState(booking, BookingStatus.REQUESTED, "Invalid booking state transition");

        guideVerificationService.assertGuideEligibleForBookingAcceptance(requesterId);

        booking.setStatus(BookingStatus.ACCEPTED);
        BookingEntity saved = bookingRepository.save(booking);
        bookingAuditService.recordStatusTransition(
            requesterId,
            requesterRole,
            saved.getId(),
            "BOOKING_ACCEPTED",
            BookingStatus.REQUESTED.name(),
            BookingStatus.ACCEPTED.name()
        );
        return toResponse(saved);
    }

    @Transactional
    public BookingResponseDto reject(UUID bookingId, UUID requesterId, UserRole requesterRole) {
        ensureRole(requesterRole, UserRole.GUIDE, "Only guides can reject bookings");
        BookingEntity booking = getBookingOrThrow(bookingId);
        ensureGuideOwner(booking, requesterId);
        assertState(booking, BookingStatus.REQUESTED, "Invalid booking state transition");

        booking.setStatus(BookingStatus.REJECTED);
        BookingEntity saved = bookingRepository.save(booking);
        bookingAuditService.record(requesterId, "BOOKING_REJECTED", saved.getId(), "status=REJECTED");
        return toResponse(saved);
    }

    @Transactional
    public BookingResponseDto confirm(UUID bookingId, UUID requesterId, UserRole requesterRole) {
        ensureRole(requesterRole, UserRole.TOURIST, "Only tourists can confirm bookings");
        BookingEntity booking = getBookingOrThrow(bookingId);
        ensureTouristOwner(booking, requesterId);
        assertState(booking, BookingStatus.ACCEPTED, "Invalid booking state transition");

        booking.setStatus(BookingStatus.CONFIRMED);
        BookingEntity saved = bookingRepository.save(booking);
        bookingAuditService.record(requesterId, "BOOKING_CONFIRMED", saved.getId(), "status=CONFIRMED");
        return toResponse(saved);
    }

    @Transactional
    public BookingResponseDto cancel(UUID bookingId, UUID requesterId, UserRole requesterRole) {
        ensureRole(requesterRole, UserRole.TOURIST, "Only tourists can cancel bookings");
        BookingEntity booking = getBookingOrThrow(bookingId);
        ensureTouristOwner(booking, requesterId);
        if (!CANCELLABLE_STATES.contains(booking.getStatus())) {
            throw new AppException(ErrorCode.INVALID_STATE, HttpStatus.CONFLICT, "Invalid booking state transition");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        BookingEntity saved = bookingRepository.save(booking);
        bookingAuditService.record(requesterId, "BOOKING_CANCELLED", saved.getId(), "status=CANCELLED");
        return toResponse(saved);
    }

    private BookingEntity getBookingOrThrow(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Booking not found"));
    }

    private void ensureRole(UserRole actualRole, UserRole requiredRole, String message) {
        if (actualRole != requiredRole) {
            throw new AppException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, message);
        }
    }

    private void ensureGuideOwner(BookingEntity booking, UUID requesterId) {
        if (!requesterId.equals(booking.getGuideId())) {
            throw new AppException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Forbidden");
        }
    }

    private void ensureTouristOwner(BookingEntity booking, UUID requesterId) {
        if (!requesterId.equals(booking.getTouristId())) {
            throw new AppException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Forbidden");
        }
    }

    private void assertState(BookingEntity booking, BookingStatus expected, String message) {
        if (booking.getStatus() != expected) {
            throw new AppException(ErrorCode.INVALID_STATE, HttpStatus.CONFLICT, message);
        }
    }

    private BookingResponseDto toResponse(BookingEntity booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getTouristId(),
                booking.getGuideId(),
                booking.getPlaceId(),
                booking.getScheduledAt(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getUpdatedAt(),
                booking.getVersion()
        );
    }
}
