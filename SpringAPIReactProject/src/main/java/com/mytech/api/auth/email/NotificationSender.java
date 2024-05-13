package com.mytech.api.auth.email;

public interface NotificationSender {
    void send(String to, String email);
}
