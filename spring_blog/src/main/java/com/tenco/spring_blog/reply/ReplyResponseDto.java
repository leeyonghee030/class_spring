package com.tenco.spring_blog.reply;


import lombok.Data;

import java.sql.Timestamp;

public class ReplyResponseDto {
    @Data
    public static class ListDto{
        private Long id;
        private String comment;
        private String username;
        private Timestamp createdAt;
        private boolean isOwner;

        public ListDto(Reply reply,Long sessionUserId, Long boardOwnerId) {
            this.comment = reply.getComment();
            this.id = reply.getId();
            if (reply.getUser() != null){
                this.username =reply.getUser().getUsername();
            }
            if (reply.getCreatedAt() != null){
                this.createdAt = reply.getCreatedAt();
            }
            boolean isReplyOwner = reply.isOwner(sessionUserId);
            boolean isBoardOwner = boardOwnerId != null && boardOwnerId.equals(sessionUserId);
            this.isOwner = isReplyOwner || isBoardOwner;
        }

    }


}
