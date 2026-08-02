package com.example.commitmate.user;

public interface SmsSender {
    void sendVerificationCode(String phoneNumber, String code);
}
