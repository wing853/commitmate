package com.example.commitmate.group;

import com.example.commitmate.core.errors.Exception400;
import lombok.Builder;
import lombok.Data;

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
                    .build();
        }

        public void validate() {
            if(roomName == null || roomName.trim().isEmpty()) {
                throw new Exception400("그룹 이름을 입력하세요");
            }
        }
    }
        @Data // Getter, Setter 포함
        public static class UpdateDTO {
            private String roomName;
            private String description;

            public void validate() {
                if(roomName == null || roomName.trim().isEmpty()) {
                    throw new Exception400("그룹 이름을 입력하세요");
                }
            }        }



}
