package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Builder;
import lombok.Data;

public class RefundResponse {

    @Data
    public static class ListDto {
        private Long id;              // 환불 요청 pk
        private String amountFormat;  // 환불 요청 금액
        private String paymentId;     // 포트원 결제 건 식별자 (주문번호)
        private String reason;        // 본인이 작성한 환불 사유
        private String rejectReason;  // 거절 사유 (거절됨일 때만 값이 존재)
        private String statusDisplay; // 대기중, 승인됨, 거절됨
        private String requestedAt;   // 환불 요청일

        // 머스태치 조건 분기용 (상태별 배지 색)
        private boolean pending;
        private boolean approved;
        private boolean rejected;

        @Builder
        public ListDto(Refund refund) {
            this.id = refund.getId();
            this.amountFormat = refund.getPayment().getAmoutFormat();
            this.paymentId = refund.getPayment().getPaymentId();
            this.reason = refund.getReason();
            if (refund.getRejectReason() != null) {
                this.rejectReason = refund.getRejectReason();
            }
            this.statusDisplay = refund.getStatus().getDisplayName();
            if (refund.getCreatedAt() != null) {
                this.requestedAt = MyDateUtil.timestampFormat(refund.getCreatedAt());
            }
            this.pending = refund.getStatus() == RefundStatus.PENDING;
            this.approved = refund.getStatus() == RefundStatus.APPROVED;
            this.rejected = refund.getStatus() == RefundStatus.REJECTED;
        }
    }
}
