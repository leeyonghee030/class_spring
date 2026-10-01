package com.tenco.spring_blog.board;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.stereotype.Repository;

import java.util.List;

//final 필드에 대한 생성자를 .class 생성시 자동으로 생성
@RequiredArgsConstructor //DI 처리
@Repository // IoC 자동으로  메모리 힙에 new 해줌
// @Repository : 스프링이 데이터 접근 계층으로 인식 데이터 베이스 예외를 스프링 예외로 변환해줌
public class BoardNativeRepository {

    // EntityManager는 JPA 핵심 인터페이스
    // 데이터베이스와 모든 작업을 담당
    private  final EntityManager em;

    //DI 생성자 주입
//    public BoardNativeRepository(EntityManager em) {
//        this.em = em;
//    }

    //Board save dao?
    @Transactional //AOP
    public  void save(String title, String content, String username) {
        Query query = em.createNativeQuery("insert into board_tb(title, content, username,created_at) " +
                "values(?,?,?,now())");

        query.setParameter(1,title);
        query.setParameter(2,content);
        query.setParameter(3,username);

        //select/ i,u,d = executeUpdate
        query.executeUpdate();
    }


    public List<Board> findAll() {
        String sql = """
                select * from board_tb order by id desc
""";

        //while(rs.next)... 알아서됨
        Query query = em.createNativeQuery(sql,Board.class);
        //Board.class 로 해함
        return  query.getResultList();
    }

    public Board findById(Long id) {
        String sql = """
                select * from board_tb where id = ?
                """;
       Query query = em.createNativeQuery(sql,Board.class);
       query.setParameter(1,id); // 값 바인딩
        try {
            return (Board) query.getSingleResult();
        }catch (Exception e) {
            return null;
        }
        //getSingleResult는 형변환

    }
    @Transactional // select말고는 하는게 졸음
    public void deleteById(Long id) {
        String sql = """
                delete from board_tb where id = ?
                """;

        Query query = em.createNativeQuery(sql);
        query.setParameter(1,id);
        query.executeUpdate();
    }
    @Transactional
    public boolean updateByid(String title, String content, long id) {
        String sql = """
                update board_tb set title = ?, content =? where id = ?
                """;
        Query query = em.createNativeQuery(sql);
        query.setParameter(1, title);
        query.setParameter(2, content);
        query.setParameter(3, id);
        int low = query.executeUpdate();
        if (low > 0 ) {
            return true;
        } else {
            return false;
        }
    }
}
