package com.example.commitmate.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
public class DevelopmentSmsSender implements SmsSender {
    @Override
    public void sendVerificationCode(String phoneNumber, String code) {
        log.warn("[개발용 SMS] 수신번호: {}, 인증번호: {}", phoneNumber, code);
    }
}
