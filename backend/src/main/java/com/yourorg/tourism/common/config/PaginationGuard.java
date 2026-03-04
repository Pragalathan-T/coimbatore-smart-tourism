package com.yourorg.tourism.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.yourorg.tourism.common.exception.AppException;
import com.yourorg.tourism.common.exception.ErrorCode;

@Component
public class PaginationGuard {

    private final int maxPageSize;

    public PaginationGuard(@Value("${app.pagination.max-size:100}") int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public void validateSize(int size) {
        if (size > maxPageSize) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Page size too large");
        }
    }
}
