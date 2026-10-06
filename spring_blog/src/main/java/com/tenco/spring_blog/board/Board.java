package com.tenco.spring_blog.board;


import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.util.MyDateUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
//엔터티 클래스만들기 : 데이터 베이스 테이블 한개를 자바 클래스로 그린 설계도
@Table(name = "board_tb")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Board {
    @Id // 이필그가 기본기 임을 나타냄
    // 기본키 값을 자동으로 생성( IDENTITY 전략 -> DB에 기본 설정 따른다 ) AUTO_INCREMENT 기능사용
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  long id;

    //별도 어노테이션이 없으면 필드명이 컬럼명이 됨
    private  String title;
    private String content;
//    private String username;
    // N : 1 , 1 : N , N : M
    //LAZY 전략(빈 클래스 가지고오고 .참조시 값을 가져옴), EAGER 전략(한번에 조인해서 가져옴)
    // LAZY 전략 : 게시글 조회 할떄 사용자는 바로 조회하지않고, 실제로 사용할떄 그떄 한번더 조회

    @ManyToOne(fetch =  FetchType.EAGER)
    @JoinColumn(name = "user_id") // board_tb 에 만들어질 외래키 컬럼이름 설정
    private User user;

    // now() <-- 사용하지 않아도 자동으로 PC --> DB  날짜주입
    @CreationTimestamp
    private Timestamp createdAt; // createdAt 컬럼 (스프링이 기본값인 스네이크 케이스로 자동 변환해줌 db에서 )


    //비즈니스 로직을 위한 생성자 설계
    // id 와 createAt은 JPA가 자동으로 설정 하므로 매개변수에서 제외
    @Builder
    public Board(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
    }

    // 자신의 상태값을 변경하는 메서스 추가 (영속성 엔티티를 수정하는 메서드)
    public void update(BoardRequest.updateDto updateDto) {
        //비즈니스 규칙검증
        updateDto.validate();
        // 영속 상태에 있는 엔티티의 필드 값을 여기서 변경
        this.title = updateDto.getTitle();
        this.content = updateDto.getContent();
        //변경 감지 (Dirty Checking) 동작 과정
        //1. 영속성 컨텍스트가 엔티티 최초 상태를 스냅샷으로 따로 보관
        //2. 필드 값 변경시 현재 시점 상태와 스냅샷 비교
        //3. 트랜잭션 커밋 시점에 변경된 필드만 update 쿼리를 자동 생성
        //4. UPDATE board_tb SET title = ?, content =? where id= ?
    }



    // 시간 포맷을 메서드를 추가
    public String getTime() {
        return MyDateUtil.timestampFormat(createdAt);
    }


}
