package com.finanscepte.notification.util;

import com.finanscepte.notification.dto.NotificationRequest;
import com.finanscepte.notification.dto.NotificationResponse;
import com.finanscepte.notification.model.Notification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationMapper {

    public Notification toEntity(NotificationRequest request) {
        return Notification.builder()
                .userId(request.userId())
                .type(request.type())
                .message(request.message())
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getMessage(),
                notification.getRead(),
                notification.getCreatedAt()
        );
    }
}
