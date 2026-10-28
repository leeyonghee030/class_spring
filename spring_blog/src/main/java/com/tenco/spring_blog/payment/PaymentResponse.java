package com.tenco.spring_blog.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class PaymentResponse {


    @Data
    public static class ListDto {
        private Long id; //pk
        private String paymentId;
        private String pgTxId;
        private String amount;
        private String statusDisplay;
        private String paidAt;

        @Builder
        public ListDto(Payment payment) {
            this.id = payment.getId();
            this.paymentId = payment.getPaymentId();
            this.pgTxId = payment.getPgTxId();
            this.amount = String.format("%,d", payment.getAmount());
            this.statusDisplay = payment.getStatus().getDisplayName(); //결제 완료 대기 취소등
            this.paidAt = MyDateUtil.timestampFormat(payment.getCreatedAt());
        }
    }
    @Data
    //1단계 응답 : 브라우저가 결제창 띄울떄 사용
    public static class PrepareDto {
        private String paymentId; //결제건 식별자
        private int amount;
        private String storeId; //포트원 상점 아이디
        private String channelKey; // 포트원 채널키

        @Builder
        public PrepareDto(String paymentId, int amount, String storeId, String channelKey) {
            this.paymentId = paymentId;
            this.amount = amount;
            this.storeId = storeId;
            this.channelKey = channelKey;
        }
    }

    //3단계 응답
    @Data
    @AllArgsConstructor
    public static class CompleteDto {
        private int amount; // 충전된 포인트
        private int currentPoint; // 충전후 보유 포인트
    }

    //포트원 V2 결제단건조회 응답 필요한 필드만 사용
    @Data
    public static class PortOnePayment{
        private String id;
        private String status; // READY,PAID,FAILED,CANCELLED
        private String pgTxId; //PG사 거래 번호
        private Amount amount;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Amount{
            private Integer total;
        }
    }
}
