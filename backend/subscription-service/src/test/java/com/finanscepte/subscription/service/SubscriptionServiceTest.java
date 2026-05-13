package com.finanscepte.subscription.service;

import com.finanscepte.common.exception.ResourceNotFoundException;
import com.finanscepte.subscription.dto.SubscriptionRequest;
import com.finanscepte.subscription.dto.SubscriptionResponse;
import com.finanscepte.subscription.model.Subscription;
import com.finanscepte.subscription.repository.SubscriptionRepository;
import com.finanscepte.subscription.service.impl.SubscriptionServiceImpl;
import com.finanscepte.subscription.util.SubscriptionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock private SubscriptionRepository repository;
    @Mock private SubscriptionMapper mapper;
    @InjectMocks private SubscriptionServiceImpl service;

    @Test
    void cancel_shouldSetStatusCancelled() {
        Subscription s = Subscription.builder().id("1").status("ACTIVE").build();
        when(repository.findById("1")).thenReturn(Optional.of(s));
        service.cancel("1");
        assertThat(s.getStatus()).isEqualTo("CANCELLED");
        verify(repository).save(s);
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
}
