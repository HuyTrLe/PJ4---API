package com.mytech.api.models.notifications;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false)
    private Long userId;

    public Notification(Long id, String message, LocalDateTime createdAt, boolean isRead, Long userId) {
        super();
        this.id = id;
        this.message = message;
        this.createdAt = createdAt;
        this.isRead = isRead;
        this.userId = userId;
    }
}
