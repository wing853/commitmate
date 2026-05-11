package com.example.commitmate.user;

import lombok.Data;

public class UserRequest {
    @Data
    public static class LoginDTO {
        private String email;
        private String password;

        public void validate() {
            if(email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("이메일을 입력하세요");
            }

            if(password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("비밀번호를 입력하세요");
            }
        }

    } // LoginDTO

    @Data
    public static class SignupDTO {
        private String email;
        private String password;
        private String nickname;

        public void validate() {
            if(email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("이메일을 입력하세요");
            }

            if(password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("비밀번호를 입력하세요");
            }

            if(nickname == null || nickname.trim().isEmpty()) {
                throw new IllegalArgumentException("닉네임을 입력하세요");
            }
        }

        public User toEntity() {
            return User.builder()
                    .email(this.email)
                    .password(this.password)
                    .nickname(this.nickname)
                    .build();
        }
    } // SignupDTO
}
