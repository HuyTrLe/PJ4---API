
package com.mytech.api.models.notifications;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class NotificationDTO {
	private Long id;
    private Long userId;
    private String notificationType;
    
    private String message;
    private boolean read;
    private LocalDateTime timestamp;

    public NotificationDTO(Long id, Long userId, String notificationType, String message, boolean read, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.notificationType = notificationType;
        
        this.message = message;
        this.read = read;
        this.timestamp = timestamp;
    }

	
}