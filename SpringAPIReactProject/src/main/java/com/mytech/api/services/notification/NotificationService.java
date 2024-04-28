package com.mytech.api.services.notification;

import java.util.List;

import com.mytech.api.models.notifications.NotificationDTO;

public interface NotificationService {
    List<NotificationDTO> getAllNotifications();

    List<NotificationDTO> getNotificationsByUserId(long userId);

    NotificationDTO createNotification(NotificationDTO notificationDTO);

    NotificationDTO updateNotification(long id, NotificationDTO notificationDTO);

    void deleteNotification(long id);

    void sendNotification(NotificationDTO notificationDTO);
}
