package com.finanscepte.notification.service;

import com.finanscepte.common.exception.ResourceNotFoundException;
import com.finanscepte.notification.dto.NotificationRequest;
import com.finanscepte.notification.dto.NotificationResponse;
import com.finanscepte.notification.model.Notification;
import com.finanscepte.notification.repository.NotificationRepository;
import com.finanscepte.notification.service.impl.NotificationServiceImpl;
import com.finanscepte.notification.util.NotificationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository repository;
    @Mock private NotificationMapper mapper;
    @InjectMocks private NotificationServiceImpl service;

    @Test
    void create_shouldSaveAndReturn() {
        Notification n = Notification.builder().userId("u1").type("INFO").message("test").build();
        NotificationRequest req = new NotificationRequest("u1", "INFO", "test");
        NotificationResponse resp = new NotificationResponse("1", "u1", "INFO", "test", false, null);
        when(mapper.toEntity(req)).thenReturn(n);
        when(repository.save(n)).thenReturn(n);
        when(mapper.toResponse(n)).thenReturn(resp);

        NotificationResponse result = service.create(req);
        assertThat(result.message()).isEqualTo("test");
    }

    @Test
    void markAsRead_shouldSetReadTrue() {
        Notification n = Notification.builder().id("1").read(false).build();
        when(repository.findById("1")).thenReturn(Optional.of(n));
        service.markAsRead("1");
        assertThat(n.getRead()).isTrue();
        verify(repository).save(n);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(repository.findById("99")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById("99")).isInstanceOf(ResourceNotFoundException.class);
    }
}
