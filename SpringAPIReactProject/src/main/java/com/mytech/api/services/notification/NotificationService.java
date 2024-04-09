package com.mytech.api.services.notification;

import com.mytech.api.models.notifications.NotificationDTO;

import java.util.List;

public interface NotificationService {
    List<NotificationDTO> getAllNotifications();
    List<NotificationDTO> getNotificationsByUserId(Long userId);
    NotificationDTO createNotification(NotificationDTO notificationDTO);
    NotificationDTO updateNotification(Long id, NotificationDTO notificationDTO);
    void deleteNotification(Long id);
}
