package com.mytech.api.repositories.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


import com.mytech.api.models.notifications.Notification;

public interface NotificationsRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
}