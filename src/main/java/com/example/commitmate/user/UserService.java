package com.example.commitmate.user;


import com.example.commitmate.core.errors.ExceptionInput;
import com.example.commitmate.core.errors.ExceptionNoInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository ur;
    private final PasswordEncoder passwordEncoder;

    public User findById(Integer id) {
        return ur.findById(id).orElseThrow(
                () -> new ExceptionNoInfo("사용자를 찾을 수 없습니다.")
        );
    }

    public User login(UserRequest.LoginDTO loginDTO) {
        User userEntity = ur.findByEmail(loginDTO.getEmail()).orElseThrow(
                () -> new ExceptionInput("이메일 혹은 비밀번호를 잘못 입력했습니다.")
        );

        if(!passwordEncoder.matches(loginDTO.getPassword(),userEntity.getPassword())){
            throw new ExceptionInput("이메일 혹은 비밀번호를 잘못 입력했습니다.");
        }

        userEntity.setProvider(AuthProvider.LOCAL);
        return userEntity;
    }

    public User findOrCreateSocialUser(AuthProvider provider, String providerId,
                                       String email, String nickname) {
        String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());
        return ur.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> ur.save(User.builder()
                        .email(email)
                        .nickname(nickname)
                        .password(randomPassword)
                        .provider(provider)
                        .providerId(providerId)
                        .build()));
    }

    public User signup(UserRequest.SignupDTO signupDTO) {
        User userEntity = signupDTO.toEntity();

        if(ur.findByEmail(signupDTO.getEmail()).isPresent()) {
            throw new ExceptionInput("이미 사용 중인 이메일입니다.");
        }

        if(ur.findByNickname(signupDTO.getNickname()).isPresent()) {
            throw new ExceptionInput("이미 사용 중인 닉네임입니다.");
        }

        userEntity.setProvider(AuthProvider.LOCAL);
        userEntity.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
        return ur.save(userEntity);
    }

    // 유저 정보 수정
    @Transactional
    public void updateProfile(Integer userId, String nickname,
                              String currentPassword, String newPassword, String newPasswordConfirm) {
        User user = ur.findById(userId).orElseThrow(
                () -> new ExceptionNoInfo("사용자를 찾을 수 없습니다.")
        );

        // 닉네임 변경
        if (nickname != null && !nickname.trim().isEmpty()) {
            if (ur.findByNickname(nickname).isPresent()) {
                throw new ExceptionInput("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(nickname);
        }

        // 비밀번호 변경 (새 비밀번호 입력한 경우만)
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                throw new ExceptionInput("현재 비밀번호를 입력하세요.");
            }
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new ExceptionInput("현재 비밀번호가 일치하지 않습니다.");
            }
            if (!newPassword.equals(newPasswordConfirm)) {
                throw new ExceptionInput("새 비밀번호가 일치하지 않습니다.");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }
    }

    @Transactional
    public void chargePoint(Integer userId, Integer amount) {
        User user = ur.findById(userId).orElseThrow(
                () -> new ExceptionNoInfo("사용자를 찾을 수 없습니다.")
        );
        user.setPoint(user.getPoint() + amount);
    }

}
