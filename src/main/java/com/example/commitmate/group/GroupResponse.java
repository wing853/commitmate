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
        // 추후 추가 예정인 필드들 (현재는 기본값)
        private Double achievementRate = 0.0;
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
    public static class UpdateDTO {
        private Integer id;
        private String roomName;
        private String description;

        public UpdateDTO(Group group) {
            this.id = group.getId();
            this.roomName = group.getRoomName();
            this.description = group.getDescription();
        }


    }
}