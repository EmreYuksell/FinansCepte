package com.finanscepte.notification.service.impl;

import com.finanscepte.common.exception.ResourceNotFoundException;
import com.finanscepte.notification.dto.NotificationRequest;
import com.finanscepte.notification.dto.NotificationResponse;
import com.finanscepte.notification.model.Notification;
import com.finanscepte.notification.repository.NotificationRepository;
import com.finanscepte.notification.service.NotificationService;
import com.finanscepte.notification.util.NotificationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationRepository notificationRepository, NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public NotificationResponse create(NotificationRequest request) {
        Notification notification = notificationMapper.toEntity(request);
        Notification saved = notificationRepository.save(notification);
        return notificationMapper.toResponse(saved);
    }

    @Override
    public NotificationResponse findById(String id) {
        return notificationRepository.findById(id)
                .map(notificationMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
    }

    @Override
    public List<NotificationResponse> findAll() {
        return notificationRepository.findAll().stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public List<NotificationResponse> findByUserId(String userId) {
        return notificationRepository.findByUserId(userId).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndReadFalse(userId).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public void markAsRead(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void deleteById(String id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification", "id", id);
        }
        notificationRepository.deleteById(id);
    }
}
