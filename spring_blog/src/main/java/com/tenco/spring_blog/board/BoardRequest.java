package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.Data;

public class BoardRequest {
    @Data
    public static  class SaveDto {
        private String title;
        private String content;
        private boolean premium; //유료 게시글 여부 체크안하면 false

        public void validate () {
            if (title == null || title.trim().isEmpty()) {
                throw new Exception400("제목은 필수입니다");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new Exception400("내용은 필수입니다");
            }

        }

        public Board toEntity(User user) {
            Board board = Board.builder()
                    .title(this.title)
                    .content(this.content)
                    .premium(this.premium)
                    .user(user)
                    .build();

            return board;
        }

    }

    @Data
    public static class UpdateDto {
        private String title;
        private String content;
        private boolean premium; //유료 게시글 여부 체크안하면 false


        //검증 메서드 (선택사항)
        public void validate () {
            if (title == null || title.trim().isEmpty()) {
                throw new Exception400("제목은 필수입니다");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new Exception400("내용은 필수입니다");
            }
        }


    }


}
