package com.tenco.spring_blog.reply;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReplyJpaRepository extends JpaRepository<Reply,Long> {

    /**
     * 1. 등록 및 수정 : save (Reply entity)
     * -엔터티를 DB에 저장합니다 ID가 없으면 INSERT, 있으면 UPDATE를 실행함
     * 2. 단건 조회 : findById(Long id)
     * - ID로 엔티티를 조회하면 Optional<Reply>타입을 반환합니다
     * 3. 전체 조회 : findAll();
     * - 테이블의 모든데이터를 조회 하여 List<Reply>로 반환도미
     * 4. 삭제 : deleteById(Long id)
     * - 특정 아이디를 가진 엔티티를 삭제합니다
     * 5, 데이터 개수 : count();
     * - 전체 레코드의 개수를 반환합니다
     * 6. 존재 여부 확인 : existsById(Long id);
     * - 해당 ID를 가진 데이터가 있는지 확인하여 boolean을 반환합니다
     */


    //게시글의 댓글 목록조회
    @Query("select r from Reply r join fetch r.user where r.board.id = :boardId order by r.id")
    List<Reply> findAllByBoardIdJoinUser (@Param("boardId") Long boardId);


    // 주의 @Query는 기본적으로 조회로(select)로 실행이된다 Delete, Update를 직접 쓸떄 반드시
    // @Modifying 사용
    //게시글에 달린 댓글 일관삭제 (게시글 삭제 전에 사용 예정)
    @Modifying
    @Query("delete from Reply r where r.board.id = :boardId ")
    void deleteByBoardId(@Param("boardId") Long boardId);


}
