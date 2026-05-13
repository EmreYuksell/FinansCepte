package com.finanscepte.report.service;

import com.finanscepte.common.exception.ResourceNotFoundException;
import com.finanscepte.report.dto.ReportRequest;
import com.finanscepte.report.model.Report;
import com.finanscepte.report.model.ReportType;
import com.finanscepte.report.repository.ReportRepository;
import com.finanscepte.report.service.impl.ReportServiceImpl;
import com.finanscepte.report.strategy.ReportGenerationStrategy;
import com.finanscepte.report.util.ReportMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ReportRepository repository;
    @Mock private ReportMapper mapper;
    private ReportServiceImpl service;

    @BeforeEach
    void setUp() {
        ReportGenerationStrategy mockStrategy = mock(ReportGenerationStrategy.class);
        service = new ReportServiceImpl(repository, mapper, List.of(mockStrategy));
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(repository.findById("99")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById("99")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(repository.existsById("99")).thenReturn(false);
        assertThatThrownBy(() -> service.deleteById("99")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_shouldReturnEmpty() {
        when(repository.findAll()).thenReturn(List.of());
        org.assertj.core.api.Assertions.assertThat(service.findAll()).isEmpty();
    }
}
