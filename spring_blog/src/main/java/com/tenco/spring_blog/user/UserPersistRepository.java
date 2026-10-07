package com.tenco.spring_blog.user;


import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception404;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository // IoC
@RequiredArgsConstructor
public class UserPersistRepository {

    private final EntityManager em;

    //회원 정보 조회 :수정폼
    public User findById(Long id) {
       User user = em.find(User.class,id);

       if (user == null) {
           throw new RuntimeException("사용자를 찾을 수 없습니다");
       }
       return user;
    }




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


    //회원 정보 수정
    @Transactional
    public User updateById(Long id, UserRequest.UpdateDto updateDto) {
        // 수정할 엔티티를 먼저 조회해서 영속 상태로 만듬
        User userEntity = em.find(User.class, id);
        if (userEntity == null) {
            throw new Exception404("수정할 회원을 찾을 수 없습니다");
        }
        // 영속 상태 엔티티의 필드 값 변경 -> 트랜잭션 커밋 시점에 더티 체킹으로 UPDATE 실행
        userEntity.update(updateDto.getPassword());
        return userEntity;
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
