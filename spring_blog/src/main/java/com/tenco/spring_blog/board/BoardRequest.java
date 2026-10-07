package com.tenco.spring_blog.board;

import com.tenco.spring_blog.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.Data;

public class BoardRequest {
    @Data
    public static  class SaveDto {
        private String title;
        private String content;
        private String username;

        public void validate () {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("제목은 필수입니다");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("내용은 필수입니다");
            }

        }

        public Board toEntity(User user) {
            Board board = Board.builder()
                    .title(title)
                    .content(content)
                    .user(user)
                    .build();

            return board;
        }

    }

    @Data
    public static class updateDto {
        private String title;
        private String content;

        //검증 메서드 (선택사항)
        public void validate () {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("제목은 필수입니다");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("내용은 필수입니다");
            }
        }


    }


}
