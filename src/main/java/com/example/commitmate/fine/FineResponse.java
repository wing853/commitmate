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
        private int unpaidCount;
        private int pendingCount;
        private int paidCount;
        private int unpaidAmount;
        private int pendingAmount;
        private int paidAmount;

        @Builder
        public GroupFineInfo(List<Fine> expiredFines, int totalFinesAmount,
                             int unpaidCount, int pendingCount, int paidCount,
                             int unpaidAmount, int pendingAmount, int paidAmount) {
            this.expiredFines = expiredFines;
            this.totalFinesAmount = totalFinesAmount;
            this.unpaidCount = unpaidCount;
            this.pendingCount = pendingCount;
            this.paidCount = paidCount;
            this.unpaidAmount = unpaidAmount;
            this.pendingAmount = pendingAmount;
            this.paidAmount = paidAmount;
        }
    }
}
