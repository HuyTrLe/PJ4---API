package com.mytech.api.repositories.notification;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mytech.api.models.notifications.Notification;
import com.mytech.api.models.notifications.NotificationType;

public interface NotificationsRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId order by n.id desc ")
    List<Notification> findByUserId(Long userId);

    @Query("SELECT n FROM Notification n WHERE n.notificationType = :notificationType")
    List<Notification> findByNotificationType(String notificationType);

    @Query("SELECT n FROM Notification n WHERE n.eventId = :eventId  and n.notificationType = :notificationType")
    Notification checkExistNotification(Long eventId, NotificationType notificationType);
    
    Notification findTopByEventIdAndNotificationTypeOrderByTimestampDesc(Long eventId, NotificationType notificationType);
}
