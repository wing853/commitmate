package com.example.commitmate.user;

import com.example.commitmate.core.errors.ExceptionInput;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {
    private final PhoneVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsSender smsSender;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void sendCode(String rawPhoneNumber) {
        String phoneNumber = normalize(rawPhoneNumber);
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new ExceptionInput("이미 가입된 휴대폰 번호입니다.");
        }

        verificationRepository.findTopByPhoneNumberOrderByIdDesc(phoneNumber)
                .filter(previous -> previous.getLastSentAt().plusSeconds(60).isAfter(LocalDateTime.now()))
                .ifPresent(previous -> { throw new ExceptionInput("인증번호는 60초 후 다시 요청할 수 있습니다."); });

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        verificationRepository.save(new PhoneVerification(phoneNumber, passwordEncoder.encode(code)));
        smsSender.sendVerificationCode(phoneNumber, code);
    }

    @Transactional
    public String verifyCode(String rawPhoneNumber, String code) {
        String phoneNumber = normalize(rawPhoneNumber);
        if (code == null || !code.matches("\\d{6}")) {
            throw new ExceptionInput("6자리 인증번호를 입력해 주세요.");
        }

        PhoneVerification verification = verificationRepository.findTopByPhoneNumberOrderByIdDesc(phoneNumber)
                .filter(PhoneVerification::isCodeAvailable)
                .orElseThrow(() -> new ExceptionInput("인증번호가 만료되었거나 입력 횟수를 초과했습니다."));

        if (!passwordEncoder.matches(code, verification.getCodeHash())) {
            verification.failAttempt();
            throw new ExceptionInput("인증번호가 일치하지 않습니다.");
        }

        String token = UUID.randomUUID().toString();
        verification.verify(passwordEncoder.encode(token));
        return token;
    }

    @Transactional
    public void consumeVerification(String rawPhoneNumber, String token) {
        String phoneNumber = normalize(rawPhoneNumber);
        PhoneVerification verification = verificationRepository.findTopByPhoneNumberOrderByIdDesc(phoneNumber)
                .filter(PhoneVerification::canConsume)
                .filter(value -> token != null && passwordEncoder.matches(token, value.getVerificationTokenHash()))
                .orElseThrow(() -> new ExceptionInput("휴대폰 번호 인증을 다시 진행해 주세요."));
        verification.consume();
    }

    private String normalize(String value) {
        String phoneNumber = value == null ? "" : value.replaceAll("[^0-9]", "");
        if (!phoneNumber.matches("010\\d{8}")) {
            throw new ExceptionInput("올바른 휴대폰 번호를 입력해 주세요.");
        }
        return phoneNumber;
    }
}
