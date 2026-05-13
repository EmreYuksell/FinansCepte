package com.finanscepte.notification.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public NotificationEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publish(String userId, String type, String message) {
        NotificationEvent event = new NotificationEvent(this, userId, type, message);
        eventPublisher.publishEvent(event);
    }
}
