package com.example.commitmate.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
public class DevelopmentPushSender implements PushSender {
    @Override
    public void sendNotification(String token, String title, String body) {
        log.warn("[개발용 푸시] 토큰: {}, 제목: {}, 내용: {}", token, title, body);
    }
}
