package com.finanscepte.notification.repository;

import com.finanscepte.common.GenericRepository;
import com.finanscepte.notification.model.Notification;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends GenericRepository<Notification, String> {

    List<Notification> findByUserIdAndReadFalse(String userId);

    List<Notification> findByUserId(String userId);
}
