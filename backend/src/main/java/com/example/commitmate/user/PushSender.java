package com.example.commitmate.user;

public interface PushSender {
    void sendNotification(String token, String title, String body);
}
