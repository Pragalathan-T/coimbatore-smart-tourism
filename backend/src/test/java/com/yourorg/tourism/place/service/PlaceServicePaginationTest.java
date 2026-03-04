package com.yourorg.tourism.place.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import com.yourorg.tourism.common.config.PaginationGuard;
import com.yourorg.tourism.common.exception.AppException;
import com.yourorg.tourism.place.mapper.PlaceMapper;
import com.yourorg.tourism.place.repository.PlaceRepository;

class PlaceServicePaginationTest {

    @Test
    void shouldRejectTooLargePageSize() {
        PlaceService service = new PlaceService(
                mock(PlaceRepository.class),
                mock(PlaceMapper.class),
            new PaginationGuard(100),
            mock(PlaceAuditService.class)
        );

        AppException ignored = assertThrows(AppException.class, () -> service.list(0, 101));
    }
}
