package com.tenco.spring_blog.reply;


import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

/*
댓글 엔티티
 Reply - > Board n : 1
 Reply - > user  n : 1

 */
@Table(name = "reply_tb")
@Getter
@NoArgsConstructor
@Entity
public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false)
    private String comment;

    @CreationTimestamp
    private Timestamp createdAt;

    //단방향  N : 1  외캐키 user_id를 가짐
    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "board_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Board board;

    //권한 수정
    //권한 삭제
    //권한 업데이트 메서드?

    @Builder
    public Reply(String comment,User user, Board board) {
        this.comment = comment;
        this.user = user;
        this.board = board;
    }


    // 댓글 작성자 본인인지
    public boolean isOwner(Long userid){
        if (userid == null) {
            return false;
        }
        return this.user.getId().equals(userid);
    }
}
