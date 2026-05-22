package com.example.commitmate.user;


import com.example.commitmate.core.errors.Exception400;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository ur;
    private final PasswordEncoder passwordEncoder;

    public User login(UserRequest.LoginDTO loginDTO) {
        User userEntity = ur.findByEmail(loginDTO.getEmail()).orElseThrow(
                () -> new Exception400("이메일 혹은 비밀번호를 잘못 입력했습니다.")
        );

        if(!passwordEncoder.matches(loginDTO.getPassword(),userEntity.getPassword())){
            throw new Exception400("이메일 혹은 비밀번호를 잘못 입력했습니다.");
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
            throw new Exception400("이미 사용 중인 이메일입니다.");
        }

        if(ur.findByNickname(signupDTO.getNickname()).isPresent()) {
            throw new Exception400("이미 사용 중인 닉네임입니다.");
        }

        userEntity.setProvider(AuthProvider.LOCAL);
        userEntity.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
        return ur.save(userEntity);
    }

}
