package com.example.commitmate.group;

import com.example.commitmate.core.errors.ExceptionInput;
import com.example.commitmate.core.errors.ExceptionNoInfo;
import com.example.commitmate.groupmember.GroupMember;
import com.example.commitmate.groupmember.GroupRole;
import com.example.commitmate.user.User;
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

        public GroupMember toMemberEntity(User user, Group group) {
            return GroupMember.builder()
                    .user(user)
                    .group(group)
                    .role(GroupRole.ADMIN)
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
                throw new ExceptionNoInfo("그룹 이름을 입력하세요");
            }
        }
    }

    @Data // Getter, Setter 포함
    public static class UpdateDTO {
        private String roomName;
        private String description;

        public void validate() {
            if (roomName == null || roomName.trim().isEmpty()) {
                throw new ExceptionInput("그룹 이름을 입력하세요");
            }
        }
    }

    @Data
    public static class JoinDTO {
        private String joinCode;

        public GroupMember toEntity(User user, Group group) {
            return GroupMember.builder()
                    .user(user)
                    .group(group)
                    .role(GroupRole.MEMBER)
                    .build();
        }
    }



}
