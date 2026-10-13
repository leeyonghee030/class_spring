package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Data;

public class BoardResponse {

    @Data
    public static class ListDto {
        private Long id;
        private String title;
        private String username;
        private String createdAt;

        public ListDto(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            // join fetch로 이미 가져온 user 사용예정 추가 쿼리 발생없음
            if (board.getUser() != null) {
                this.username = board.getUser().getUsername();
            }
            if(board.getCreatedAt() != null){
                this.createdAt = MyDateUtil.timestampFormat(board.getCreatedAt());
            }
        }
    }



    //게시글 상세 응답 DTO
    @Data
    public  static  class DetailDto{
        private Long id;
        private String title;
        private String content;
        private Long userId;
        private String username;
        private String createdAt;

        public DetailDto(Board board){
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
            //지연 로딩 오류방지를 위한 방어적 코드 작성
            if (board.getUser() != null) {
                this.userId = board.getUser().getId();
                this.username = board.getUser().getUsername();
            }
            if(board.getCreatedAt() != null){
                this.createdAt = MyDateUtil.timestampFormat(board.getCreatedAt());
            }
        }


    }
    //수정화면
    @Data
    public static class UpdateDto {
        private Long id;
        private String title;
        private String content;

        public UpdateDto(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
        }
    }



}
