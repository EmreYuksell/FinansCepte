package com.finanscepte.notification.event;

import org.springframework.context.ApplicationEvent;

public class NotificationEvent extends ApplicationEvent {

    private final String userId;
    private final String type;
    private final String message;

    public NotificationEvent(Object source, String userId, String type, String message) {
        super(source);
        this.userId = userId;
        this.type = type;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
