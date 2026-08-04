package com.example.commitmate.user;

import com.example.commitmate.core.errors.Exception401;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MobileOAuthService {
    private final UserService userService;
    private final Map<String, LoginCode> codes = new ConcurrentHashMap<>();

    public String issue(User user) {
        codes.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(Instant.now()));
        String code = UUID.randomUUID().toString();
        codes.put(code, new LoginCode(user.getId(), Instant.now().plusSeconds(120)));
        return code;
    }

    public User redeem(String code) {
        LoginCode loginCode = codes.remove(code);
        if (loginCode == null || loginCode.expiresAt().isBefore(Instant.now())) {
            throw new Exception401("Google 로그인 정보가 만료되었습니다. 다시 시도해 주세요.");
        }
        return userService.findById(loginCode.userId());
    }

    private record LoginCode(Integer userId, Instant expiresAt) {}
}
