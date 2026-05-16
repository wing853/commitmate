package com.example.commitmate.group;

import com.example.commitmate.core.errors.Exception400;
import lombok.Builder;
import lombok.Data;

import java.util.Random;
import java.util.UUID;

public class GroupRequest {
    @Data
    public static class CreateDTO {
        private String roomName;
        private String description;
        // 추후 초대코드 추가 예정

        public Group toEntity() {
            return Group.builder()
                    .roomName(this.roomName)
                    .description(this.description)
                    .inviteCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .joinCode(generateJoinCode())
                    .build();
        }

        private String generateJoinCode() {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            Random random = new Random();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }

            return sb.toString();
        }


        public void validate() {
            if (roomName == null || roomName.trim().isEmpty()) {
                throw new Exception400("그룹 이름을 입력하세요");
            }
        }
    }

    @Data // Getter, Setter 포함
    public static class UpdateDTO {
        private String roomName;
        private String description;

        public void validate() {
            if (roomName == null || roomName.trim().isEmpty()) {
                throw new Exception400("그룹 이름을 입력하세요");
            }
        }
    }


    @Data
    public static class JoinDTO {
        private String joinCode;
    }
}
