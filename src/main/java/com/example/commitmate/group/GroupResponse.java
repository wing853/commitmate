package com.example.commitmate.group;

import lombok.Data;

// GroupResponse.java
public class GroupResponse {

    @Data
    public static class MyGroupDTO {
        private Integer id;
        private String roomName;
        private String description;
        private String createdAt;
        private boolean isManager;

        public MyGroupDTO(Group group, boolean isManager) {
            this.id = group.getId();
            this.roomName = group.getRoomName();
            this.description = group.getDescription();
            // 날짜 포맷팅
            if(createdAt == null) {
                this.createdAt = new java.text.SimpleDateFormat("yyyy. MM. dd")
                        .format(group.getCreatedAt());
            }
            this.isManager = isManager;
        }
    }

    @Data
    public static class detailDTO {
        private Integer id;
        private String roomName;
        private String description;

        public detailDTO(Group group) {
            this.id = group.getId();
            this.roomName = group.getRoomName();
            this.description = group.getDescription();
        }
    }

    @Data
    public static class InviteInfoDTO {
        private Integer id;
        private String inviteCode;
        private String joinCode;

        public InviteInfoDTO(Group group) {
            this.id = group.getId();
            this.inviteCode = group.getInviteCode();
            this.joinCode = group.getJoinCode();
        }
    }
}