package com.finanscepte.notification.service;

import com.finanscepte.notification.dto.NotificationRequest;
import com.finanscepte.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse create(NotificationRequest request);

    NotificationResponse findById(String id);

    List<NotificationResponse> findAll();

    List<NotificationResponse> findByUserId(String userId);

    List<NotificationResponse> getUnreadNotifications(String userId);

    void markAsRead(String id);

    void deleteById(String id);
}
