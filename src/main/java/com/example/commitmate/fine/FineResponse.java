package com.example.commitmate.fine;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class FineResponse {
    @Data
    @NoArgsConstructor
    public static class GroupFineInfo {
        private List<Fine> expiredFines;
        private int totalFinesAmount;

        @Builder
        public GroupFineInfo(List<Fine> expiredFines, int totalFinesAmount) {
            this.expiredFines = expiredFines;
            this.totalFinesAmount = totalFinesAmount;
        }
    }
}
