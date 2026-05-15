package com.finanscepte.goals.service;

import com.finanscepte.common.exception.ResourceNotFoundException;
import com.finanscepte.goals.dto.GoalRequest;
import com.finanscepte.goals.dto.GoalResponse;
import com.finanscepte.goals.model.Goal;
import com.finanscepte.goals.repository.GoalRepository;
import com.finanscepte.goals.service.impl.GoalServiceImpl;
import com.finanscepte.goals.util.GoalMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock private GoalRepository goalRepository;
    @Mock private GoalMapper goalMapper;
    @InjectMocks private GoalServiceImpl service;

    @Test
    void create_shouldSaveAndReturn() {
        GoalRequest req = new GoalRequest("u1", "Araba", 100000.0, 10000.0, LocalDate.now().plusMonths(6), "#4f46e5", "tasarruf");
        Goal entity = Goal.builder().userId("u1").name("Araba").build();
        GoalResponse resp = new GoalResponse("1", "u1", "Araba", 100000, 10000, 10, LocalDate.now().plusMonths(6), "#4f46e5", "tasarruf", LocalDateTime.now());
        when(goalMapper.toEntity(req)).thenReturn(entity);
        when(goalRepository.save(entity)).thenReturn(entity);
        when(goalMapper.toResponse(entity)).thenReturn(resp);

        GoalResponse result = service.create(req);
        verify(goalRepository).save(entity);
        org.assertj.core.api.Assertions.assertThat(result.name()).isEqualTo("Araba");
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(goalRepository.findById("99")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById("99")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deposit_shouldIncreaseAmount() {
        Goal goal = Goal.builder().id("1").currentAmount(100).targetAmount(1000).build();
        Goal saved = Goal.builder().id("1").currentAmount(200).targetAmount(1000).build();
        GoalResponse resp = new GoalResponse("1", "u1", "Araba", 1000, 200, 20, LocalDate.now(), null, null, LocalDateTime.now());
        when(goalRepository.findById("1")).thenReturn(Optional.of(goal));
        when(goalRepository.save(any())).thenReturn(saved);
        when(goalMapper.toResponse(saved)).thenReturn(resp);

        service.deposit("1", 100.0);
        verify(goalRepository).save(any());
    }
}
