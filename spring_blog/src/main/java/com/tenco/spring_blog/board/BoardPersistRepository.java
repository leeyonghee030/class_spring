package com.tenco.spring_blog.board;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import jdk.jfr.TransitionTo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
영속성 컨텍스트 활용한 Repository 클래스 만들기
저장소 보관소, 창고를 의미한다
즉 , 소프트웨어에서는 데이터를 저장하고 관리하는 곳을 추상화 한 개념입니다
 */
@Repository
@RequiredArgsConstructor
public class BoardPersistRepository {
    private final EntityManager em;



    @Transactional
    public void updateById(long id, BoardRequest.updateDto reqDto) {
        // 수정할 엔티티를 먼저 조회후 영속 상태로만듬
        Board boardEntity = em.find(Board.class,id);
        // 엔터티 존재 여부 확인
        if(boardEntity == null) {
            throw new IllegalArgumentException("수정할 게시물을 찾을수 없습니다");
        }
        //엔티티 객체 상태 변경중
        boardEntity.update(reqDto);
        // 1차 캐시에 저장된 엔티티 객체의 내부상태값이 변경이 되고 트랜잭션이 종료가되면
        // 더티 채킹(Dirty CheckKing)이 발생한다
        // 자동으로 update를 함 (다른부분)
    }



    //게시글 삭제하기 (영속성 컨텍스트를 활용한 안전한 삭제)
    @Transactional
    public void deleteById(Long id){
        //먼저 삭제할 엔티티 영속 상태로 조회부터 해야함
        Board boardEntity = em.find(Board.class,id);
        // 엔티티 존재 여부 확인
        if(boardEntity == null) {
            throw new IllegalArgumentException("삭제할 게시물을 찾을수 없습니다");
        }
        em.remove(boardEntity);
        //삭제과정
        // boardEntity가 영속 -> 삭제로 변경
        // 1차 캐시에서 해당 엔터티가 제거
        // 트핸젝션 커밋 시점에 DELETE SQL 자동 실행 됨

        // 삭제하는 JPQL 퀴리 만들어 보기
        // delete from Board b where b.id = :id
//        Query query = em.createQuery("delete from Board b where b.id = :id");
//        query.setParameter("id",id);
//        query.executeUpdate();
    }




    // 기본키로 게시글 단건 조회 (1차 캐시 활용)
    public Board findById(long id) {
        Board board = em.find(Board.class,id);

        // find() 특징 :
        // 1. 기본키로만 조회 가능
        // 2. 1차 캐쉬 먼저 찾기 시도
        // 3. 없으면 DB에서 조회후 1차 캐시에 저장
        // 4. 영속 상태 만든후 반환
        return  board;
    }

    //JPQL을 사용한 조회 방법
    public Board findByIdWithJPQL(long id) {
        String jpql = """
                select b from Board b where b.id = :id
                """;
        try {
            return em.createQuery(jpql,Board.class)
                    .setParameter("id",id)
                    .getSingleResult();
        }catch (Exception e) {
            return  null;
        }

        //jpql 단점:
        // 1차 캐시 위회하여 항상 DB에 접근
        // 코드가 복잡할수있음
        // getSingleResult() 예외처리가 필요
    }

    // JPQL을 사용한 게시글 목록 조회
    public List<Board> findAll(){
        //JPQL : 엔티티 객체를 대상으로 하는 객체 지향 쿼리
        //Board는 엔티티 클래스명, b 별칭
        //테이블명(board_tb)가 아닌 엔티티명 (Board)을 사용
        String jpql = """
                select b from Board b order by b.createdAt desc
                """;
        //createQuery() : JPQL 쿼리생성
        // 두번쟤 매개변수로 반환 타입을 지정 (타입 안정성 확보)
        //getResultList() : List<Board> 로반환
        return em.createQuery(jpql,Board.class).getResultList();
    }


    //게시글 저장
    @Transactional
    public Board save(Board board) {
        // 1, 매개 변수로 받은 board는 이시점에서 비영속 상태라고 할수있다
        // - 아직 영속성 컨텍스트에 관리되지 않은 상태
        // - 데이터베이스와 연관 없는 순수 Java 객체인 상태
        em.persist(board);
        // 2. em.persist(board) 이후에 엔터티를 영속성 컨텍스트에 저장 시킴
        // - board 객체가 영속 상태로 변경됨
        // - 영속성 컨텍스트가 엔티티를 관리하기 시작함
        // - 아직 실제 insert 쿼리는 실행 되지않음 (쓰기 지연)

        //3. 트랜잭션 커밋 시점에 실제 insert쿼리 실행됨
        // 이떄 영속성 컨텐스트의 변경 사항이 DB에 반영됨
        // board 객체의 id 필드에 자종 생성된 값이 할당 됨
        return board;
        //4. 영속 상태의 객체를 반환
        // 자동으로 생성된 id 값을 포함한 객체가 반한됨
    }



   // 엔티티의 영속 상태 4가지
    //1. 비영속상태 : 새로 생성된 객체, 영속성 컨텐스트와 무관
    //2. 영속 상태 : 영속성 컨텍스트의 관리되는 상태
    //3. 준 영속 상태: 영속성 컨텍스트에서 분리된 상태
    //4. 삭제 상태: 삭제예정 상태 (트랜젝션 커밋시 DELETE 쿼리 실행)



}
