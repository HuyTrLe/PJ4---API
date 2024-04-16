package com.mytech.api.models.notifications;

import java.util.List;



import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;

import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

public class NotificationType {

    @Id
    private Long id;

    @Column(name = "type_name", nullable = false, unique = true)
    private String typeName;

    @OneToMany(mappedBy = "notificationType", cascade = CascadeType.ALL)
    private List<Notification> notifications;

    public NotificationType(String typeName) {
        this.typeName = typeName;
    }
}
