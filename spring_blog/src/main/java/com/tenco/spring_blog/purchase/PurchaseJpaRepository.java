package com.tenco.spring_blog.purchase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurchaseJpaRepository extends JpaRepository<Purchase,Long> {

    // 구매 내역 조회 (해당 게시글 pk 기준)
    @Query("select p from Purchase p where p.user.id = :userId and p.board.id = :boardId")
    Optional<Purchase> findByUserIdAndBoardId(@Param("userId") Long userId, @Param("boardId") Long boardId);

    //구매 여부 확인 (true ,false 응답 필요시)
    @Query("""
    select case when count(p) > 0 then true else false end 
    from Purchase p
    where p.user.id = :userId and p.board.id = :boardId
""")
    boolean existsByUserIdAndBoardId(@Param("userId") Long userId, @Param("boardId") Long boardId);

    @Query("select count(p) from Purchase p where p.board.id = :boardId")
    long countByBoardId(@Param("boardId") Long boardId);

    // 게시글에 달린 구매 내역 일괄 삭제 (게시글 삭제 전에 사용 )
    @Modifying // select 가아니라면  반드시 설정
    @Query("delete from Purchase p where p.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);


    //로그인 사용자 구매 내역조회
    //구매내역 +게시글 타이틀 +게시글 작성자이름등 한번의 쿼리로 가겨온다 최신 구매순

    @Query("""
                    select p from Purchase p 
                    join fetch p.board b
                    join fetch b.user bu
                    where p.user.id = :userId
                    order by p.id desc
            """)
    List<Purchase> findAllByUserIdJoinBoard(@Param("userId") Long userId);
}
