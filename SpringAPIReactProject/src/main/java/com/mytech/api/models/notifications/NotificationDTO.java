
package com.mytech.api.models.notifications;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long userId;
    private NotificationType notificationType;
    private Long eventId;
    private String message;
    private boolean read;
    private LocalDateTime timestamp;
}