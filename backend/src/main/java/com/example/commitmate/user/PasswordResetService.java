package com.example.commitmate.user;

import com.example.commitmate.core.errors.ExceptionInput;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private static final int TOKEN_VALID_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Transactional
    public void requestReset(String email, String baseUrl) {
        if (email == null || email.isBlank()) {
            throw new ExceptionInput("이메일을 입력해 주세요.");
        }

        userRepository.findByEmail(email.trim().toLowerCase())
                .filter(User::isLocalUser)
                .ifPresent(user -> issueAndSend(user, baseUrl));
    }

    public boolean isValid(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return false;
        return tokenRepository.findByTokenHash(hash(rawToken))
                .map(PasswordResetToken::isAvailable)
                .orElse(false);
    }

    @Transactional
    public void resetPassword(String rawToken, String password, String passwordConfirm) {
        if (password == null || password.length() < 8) {
            throw new ExceptionInput("비밀번호는 8자 이상 입력해 주세요.");
        }
        if (!password.equals(passwordConfirm)) {
            throw new ExceptionInput("비밀번호 확인이 일치하지 않습니다.");
        }

        PasswordResetToken resetToken = tokenRepository.findByTokenHash(hash(rawToken))
                .filter(PasswordResetToken::isAvailable)
                .orElseThrow(() -> new ExceptionInput("재설정 링크가 만료되었거나 이미 사용되었습니다."));

        resetToken.getUser().setPassword(passwordEncoder.encode(password));
        resetToken.use();
    }

    private void issueAndSend(User user, String baseUrl) {
        if (mailFrom == null || mailFrom.isBlank()) {
            throw new ExceptionInput("메일 발송 설정이 완료되지 않았습니다. 관리자에게 문의해 주세요.");
        }

        tokenRepository.deleteAllByUser(user);
        String rawToken = UUID.randomUUID() + UUID.randomUUID().toString();
        tokenRepository.save(new PasswordResetToken(user, hash(rawToken),
                LocalDateTime.now().plusMinutes(TOKEN_VALID_MINUTES)));

        String resetUrl = baseUrl + "/reset-password?token=" + rawToken;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getEmail());
        message.setSubject("[CommitMate] 비밀번호 재설정 안내");
        message.setText("안녕하세요, " + user.getNickname() + "님.\n\n"
                + "아래 링크에서 비밀번호를 재설정해 주세요.\n"
                + resetUrl + "\n\n"
                + "링크는 " + TOKEN_VALID_MINUTES + "분 동안 한 번만 사용할 수 있습니다.\n"
                + "본인이 요청하지 않았다면 이 메일을 무시해 주세요.");
        mailSender.send(message);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
        }
    }
}
