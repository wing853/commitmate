package com.example.commitmate.user;


import com.example.commitmate.core.errors.Exception400;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

        return userEntity;
    }

    public User signup(UserRequest.SignupDTO signupDTO) {
        User userEntity = signupDTO.toEntity();

        if(ur.findByEmail(signupDTO.getEmail()).isPresent()) {
            throw new Exception400("이미 사용 중인 이메일입니다.");
        }

        if(ur.findByNickname(signupDTO.getNickname()).isPresent()) {
            throw new Exception400("이미 사용 중인 닉네임입니다.");
        }

        userEntity.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
        return ur.save(userEntity);
    }

}
