package com.example.commitmate.user;

import com.example.commitmate.core.errors.ExceptionInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository ur;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PhoneVerificationService phoneVerificationService;

    @InjectMocks
    private UserService userService;

    private UserRequest.LoginDTO loginDto(String email, String password) {
        UserRequest.LoginDTO dto = new UserRequest.LoginDTO();
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    @Test
    void login_성공() {
        User user = User.builder().email("a@b.com").password("encoded").build();
        when(ur.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("1234", "encoded")).thenReturn(true);

        User result = userService.login(loginDto("a@b.com", "1234"));

        assertThat(result).isEqualTo(user);
        assertThat(result.getProvider()).isEqualTo(AuthProvider.LOCAL);
    }

    @Test
    void login_존재하지_않는_이메일이면_예외() {
        when(ur.findByEmail("nope@b.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(loginDto("nope@b.com", "1234")))
                .isInstanceOf(ExceptionInput.class);
    }

    @Test
    void login_비밀번호가_틀리면_예외() {
        User user = User.builder().email("a@b.com").password("encoded").build();
        when(ur.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(loginDto("a@b.com", "wrong")))
                .isInstanceOf(ExceptionInput.class);
    }

    private UserRequest.SignupDTO signupDto() {
        UserRequest.SignupDTO dto = new UserRequest.SignupDTO();
        dto.setEmail("new@b.com");
        dto.setPassword("1234");
        dto.setNickname("새유저");
        dto.setPhoneNumber("01012345678");
        dto.setPhoneVerificationToken("token");
        return dto;
    }

    @Test
    void signup_이미_사용중인_이메일이면_예외() {
        when(ur.findByEmail("new@b.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.signup(signupDto()))
                .isInstanceOf(ExceptionInput.class);
        verify(ur, never()).save(any());
    }

    @Test
    void signup_이미_사용중인_닉네임이면_예외() {
        when(ur.findByEmail("new@b.com")).thenReturn(Optional.empty());
        when(ur.findByNickname("새유저")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.signup(signupDto()))
                .isInstanceOf(ExceptionInput.class);
        verify(ur, never()).save(any());
    }

    @Test
    void signup_이미_가입된_휴대폰번호면_예외() {
        when(ur.findByEmail("new@b.com")).thenReturn(Optional.empty());
        when(ur.findByNickname("새유저")).thenReturn(Optional.empty());
        when(ur.findByPhoneNumber("01012345678")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.signup(signupDto()))
                .isInstanceOf(ExceptionInput.class);
        verify(ur, never()).save(any());
    }

    @Test
    void signup_정상가입() {
        when(ur.findByEmail("new@b.com")).thenReturn(Optional.empty());
        when(ur.findByNickname("새유저")).thenReturn(Optional.empty());
        when(ur.findByPhoneNumber("01012345678")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("1234")).thenReturn("encoded-1234");
        when(ur.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.signup(signupDto());

        verify(phoneVerificationService).consumeVerification("01012345678", "token");
        assertThat(saved.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(saved.getPassword()).isEqualTo("encoded-1234");
    }
}