package com.tenco.spring_blog.purchase;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Builder;
import lombok.Data;

public class PurchaseResponse {
    // 구매항목 1개의 클래스
    @Data
    public static class ListDto {
        private Long id; // 구매내역 pk
        private Long boardId; // 게시글 pk 상세보기 링크용
        private String boardTitle;
        private String boardUsername;
        private int price;
        private String purchasedAt; //구매일

        @Builder
        public ListDto(Purchase purchase) {
            this.id = purchase.getId();
            if (purchase.getBoard() != null){
                this.boardId = purchase.getBoard().getId();
                this.boardTitle = purchase.getBoard().getTitle();
                this.boardUsername = purchase.getBoard().getUser().getUsername();
            }
            this.price = purchase.getPrice();
            this.purchasedAt = MyDateUtil.timestampFormat(purchase.getCreatedAt());
        }
    }
}
