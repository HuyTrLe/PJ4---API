
package com.mytech.api.services.notification;

import java.time.LocalDateTime;
import java.util.List;

import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


import com.mytech.api.models.notifications.Notification;
import com.mytech.api.models.notifications.NotificationDTO;
import com.mytech.api.models.notifications.NotificationType;
import com.mytech.api.repositories.notification.NotificationsRepository;

import jakarta.transaction.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationsRepository notificationRepository;

    @Autowired
    private ModelMapper modelMapper;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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
    @Transactional
    public NotificationDTO createNotification(NotificationDTO notificationDTO) {
        Notification existingNotification = notificationRepository.checkExistNotification(
                notificationDTO.getEventId(), notificationDTO.getNotificationType());
        
        if (existingNotification != null) {
            throw new RuntimeException("Notification already exists.");
        }
        Notification notification = modelMapper.map(notificationDTO, Notification.class);
        notification.setTimestamp(LocalDateTime.now());
        notification = notificationRepository.save(notification);
        return modelMapper.map(notification, NotificationDTO.class);
    }

    @Override
    public NotificationDTO updateNotification(Long id, NotificationDTO notificationDTO) {
        Notification existingNotification = notificationRepository.findById(id).orElse(null);
        if (existingNotification != null) {
            existingNotification.setMessage(notificationDTO.getMessage());
            existingNotification.setRead(notificationDTO.isRead());
            existingNotification.setTimestamp(LocalDateTime.now());
            existingNotification = notificationRepository.save(existingNotification);
            return modelMapper.map(existingNotification, NotificationDTO.class);
        }
        return null;
    }


    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
    
    public void sendNotification(NotificationDTO notificationDTO) {
        // Convert DTO to entity and save to the database if needed
        Notification notification = modelMapper.map(notificationDTO, Notification.class);
        notification.setTimestamp(LocalDateTime.now());
        Notification savedNotification = notificationRepository.save(notification);

        messagingTemplate.convertAndSend("/topic/notifications", modelMapper.map(savedNotification, NotificationDTO.class));
    }
}
