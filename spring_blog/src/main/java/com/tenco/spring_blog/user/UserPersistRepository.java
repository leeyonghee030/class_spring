package com.tenco.spring_blog.user;


import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository // IoC
@RequiredArgsConstructor
public class UserPersistRepository {

    private final EntityManager em;

    //회원 정보 조회 - 로그인 (사용자 이름, 비밀번호 확인)
    public User findByUsernameAndPassword(String username, String password) {
        try {

            String jpql = """
                    select u from User u where u.username = :username and u.password = :password
                    """;

            Query query = em.createQuery(jpql,User.class);

            query.setParameter("username",username);
            query.setParameter("password", password);

            return (User) query.getSingleResult();

        }catch (Exception e) {
            //일치하는 사용자가 없거나 에러 발생시 null반환
            //로그인 실패를 의미함
            return null;
        }
    }


    // 회원가입 :
    @Transactional
    public User save(User user) {
        // 비영속 상태에 user 객체를 영속성 컨텍스트에 저장
        em.persist(user);
        //영속성 컨텍스트가 user 객체를 관리하기 시작
        //persist() 호출 후 영속 상태가 되고 트랜잭션 커밋 시점이  insert 쿼리가 실행됨
        //자동 생성된 id와 생성시간이 user 객체에 설정됨
        return user;
    }

    // 사용자명 중복 체크용 조회메서드
    public User findByUsername(String username) {
        String jpql = """
                select u from User u where u.username = :username 
                """;
        try {
            return em.createQuery(jpql, User.class).setParameter("username", username).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }


}
