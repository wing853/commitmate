package com.example.commitmate.user;

import com.example.commitmate.core.errors.ExceptionInput;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class ProductionPushSender implements PushSender {

    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    @Override
    public void sendNotification(String token, String title, String body) {
        if (token == null || token.isBlank()) {
            return;
        }

        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null) {
            throw new ExceptionInput("푸시 알림 서비스 설정이 필요합니다.");
        }

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .build();

        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.error("[FCM] 푸시 발송 실패. error={}", e.getMessage());
            throw new ExceptionInput("푸시 발송에 실패했습니다.");
        }
    }
}
