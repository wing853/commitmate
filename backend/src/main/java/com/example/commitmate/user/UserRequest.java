package com.example.commitmate.user;

import com.example.commitmate.core.errors.ExceptionInput;
import lombok.Data;

public class UserRequest {
    @Data
    public static class LoginDTO {
        private String email;
        private String password;

        public void validate() {
            if(email == null || email.trim().isEmpty()) {
                throw new ExceptionInput("이메일을 입력하세요");
            }

            if(password == null || password.trim().isEmpty()) {
                throw new ExceptionInput("비밀번호를 입력하세요");
            }
        }

    } // LoginDTO

    @Data
    public static class SignupDTO {
        private String email;
        private String password;
        private String nickname;
        private String phoneNumber;
        private String phoneVerificationToken;

        public void validate() {
            if(email == null || email.trim().isEmpty()) {
                throw new ExceptionInput("이메일을 입력하세요");
            }

            if(password == null || password.trim().isEmpty()) {
                throw new ExceptionInput("비밀번호를 입력하세요");
            }

            if(nickname == null || nickname.trim().isEmpty()) {
                throw new ExceptionInput("닉네임을 입력하세요");
            }

            if(phoneNumber == null || phoneNumber.trim().isEmpty()
                    || phoneVerificationToken == null || phoneVerificationToken.trim().isEmpty()) {
                throw new ExceptionInput("휴대폰 번호 인증을 완료하세요");
            }
        }

        public User toEntity() {
            return User.builder()
                    .email(this.email)
                    .password(this.password)
                    .nickname(this.nickname)
                    .phoneNumber(this.phoneNumber.replaceAll("[^0-9]", ""))
                    .build();
        }
    } // SignupDTO
}
