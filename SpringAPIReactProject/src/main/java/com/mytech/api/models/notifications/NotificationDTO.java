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
    private String message;
    private LocalDateTime createdAt;
    private boolean isread;
    
    
	public NotificationDTO(Long id, String message, LocalDateTime createdAt, boolean isread) {
		super();
		this.id = id;
		this.message = message;
		this.createdAt = createdAt;
		this.isread = isread;
	}
    
    
	

    
}
