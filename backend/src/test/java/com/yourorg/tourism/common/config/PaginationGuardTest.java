package com.yourorg.tourism.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.yourorg.tourism.common.exception.AppException;
import com.yourorg.tourism.common.exception.ErrorCode;

class PaginationGuardTest {

    @Test
    void shouldThrowBadRequestWhenPageSizeExceedsMax() {
        PaginationGuard guard = new PaginationGuard(100);

        AppException ex = assertThrows(AppException.class, () -> guard.validateSize(101));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
        assertEquals("Page size too large", ex.getMessage());
    }

    @Test
    void shouldAllowPageSizeWithinMax() {
        PaginationGuard guard = new PaginationGuard(100);
        guard.validateSize(100);
    }
}
