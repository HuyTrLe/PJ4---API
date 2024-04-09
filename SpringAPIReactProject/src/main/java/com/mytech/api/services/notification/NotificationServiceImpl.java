package com.mytech.api.services.notification;

import java.util.List;

import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.mytech.api.models.notifications.Notification;
import com.mytech.api.models.notifications.NotificationDTO;
import com.mytech.api.repositories.notification.NotificationsRepository;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationsRepository notificationRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<NotificationDTO> getAllNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        return notifications.stream().map(notification -> modelMapper.map(notification, NotificationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getNotificationsByUserId(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        return notifications.stream().map(notification -> modelMapper.map(notification, NotificationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public NotificationDTO createNotification(NotificationDTO notificationDTO) {
        Notification notification = modelMapper.map(notificationDTO, Notification.class);
        notification = notificationRepository.save(notification);
        return modelMapper.map(notification, NotificationDTO.class);
    }

    @Override
    public NotificationDTO updateNotification(Long id, NotificationDTO notificationDTO) {
        Notification existingNotification = notificationRepository.findById(id).orElse(null);
        if (existingNotification != null) {
            existingNotification.setMessage(notificationDTO.getMessage());
            existingNotification.setCreatedAt(notificationDTO.getCreatedAt());
            existingNotification.setRead(false);
            existingNotification = notificationRepository.save(existingNotification);
            return modelMapper.map(existingNotification, NotificationDTO.class);
        }
        return null;
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}