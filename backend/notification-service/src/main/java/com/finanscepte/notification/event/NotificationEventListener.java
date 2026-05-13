package com.finanscepte.notification.event;

import com.finanscepte.notification.model.Notification;
import com.finanscepte.notification.repository.NotificationRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;

    public NotificationEventListener(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .type(event.getType())
                .message(event.getMessage())
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }
}
