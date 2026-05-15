package com.example.commitmate.user;


import com.example.commitmate.core.errors.Exception400;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository ur;

    public User login(UserRequest.LoginDTO loginDTO) {
        User userEntity = ur.findByEmailAndPassword(loginDTO.getEmail(), loginDTO.getPassword()).orElseThrow(
                () -> new Exception400("사용자를 찾을 수 없습니다")
        );

        return userEntity;
    }

    public User signup(UserRequest.SignupDTO signupDTO) {
        User userEntity = signupDTO.toEntity();
        return ur.save(userEntity);
    }

}
