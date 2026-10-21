package com.tenco.spring_blog.user;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest // JPA 테스트에 필요한 환경을 자동으로 구성합니다.
public class UserJpaRepositoryTest {

    @Autowired // DI
    private UserJpaRepository userJpaRepository;

    @Autowired // JOIN FETCH 로 연관 엔티티가 실제로 로딩되었는지 확인하기 위해 사용
    private EntityManager entityManager;

    @Test
    public void findByUsernameAndPassword_로그인_성공_테스트() {

        // given : 회원가입된 사용자 정보
        User user = User.builder()
                .username("testUser")
                .password("3456")
                .email("test@email.com")
                .build();

        userJpaRepository.save(user);

        // when : 사용자명과 비밀번호로 조회
        User loginUser = userJpaRepository.findByUsernameAndPassword(
                "testUser",
                "3456"
        ).orElse(null);

        // then : 조회된 사용자 검증
        Assertions.assertThat(loginUser).isNotNull();
        Assertions.assertThat(loginUser.getId()).isNotNull();
        Assertions.assertThat(loginUser.getUsername()).isEqualTo("testUser");
        Assertions.assertThat(loginUser.getPassword()).isEqualTo("3456");
        Assertions.assertThat(loginUser.getEmail()).isEqualTo("test@email.com");

        // 저장한 객체와 조회한 객체가 같은 엔티티인지 확인
        Assertions.assertThat(loginUser).isSameAs(user);
    }


    @Test
    public void findByUsernameAndPassword_비밀번호_불일치_테스트() {

        // given : 회원가입된 사용자 정보
        User user = User.builder()
                .username("testUser")
                .password("3456")
                .email("test@email.com")
                .build();

        userJpaRepository.save(user);

        // when : 잘못된 비밀번호로 로그인 시도
        User loginUser = userJpaRepository.findByUsernameAndPassword(
                "testUser",
                "9999"
        ).orElse(null);

        // then : 로그인 실패 -> null
        Assertions.assertThat(loginUser).isNull();
    }


    @Test
    public void findByUsernameAndPassword_존재하지않는_사용자_테스트() {

        // given : DB에 존재하지 않는 사용자 정보
        String username = "xxxxx";
        String password = "3456";

        // when : 존재하지 않는 사용자로 로그인 시도
        User loginUser = userJpaRepository.findByUsernameAndPassword(
                username,
                password
        ).orElse(null);

        // then : 로그인 실패 -> null
        Assertions.assertThat(loginUser).isNull();
    }


    @Test
    public void findByUsernameWithRoles_역할함께조회_테스트() {

        // given : 역할(USER)을 가진 사용자를 저장하고, 영속성 컨텍스트를 비워서
        // 아래 조회가 1차 캐시가 아닌 실제 JOIN FETCH 쿼리를 타도록 만든다.
        User user = User.builder()
                .username("roleUser")
                .password("1234")
                .email("role@email.com")
                .build();
        user.addRole(Role.USER);

        userJpaRepository.saveAndFlush(user);
        entityManager.clear();

        // when : JOIN FETCH 로 역할과 함께 조회
        User foundUser = userJpaRepository.findByUsernameWithRoles("roleUser").orElse(null);

        // then : 사용자 조회 확인
        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getUsername()).isEqualTo("roleUser");

        // roles 가 지연 로딩이 아니라 실제로 함께 로딩되었는지 확인
        boolean rolesLoaded = entityManager.getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .isLoaded(foundUser, "roles");
        Assertions.assertThat(rolesLoaded).isTrue();

        // 로딩된 역할 내용 검증
        Assertions.assertThat(foundUser.getRoles()).hasSize(1);
        Assertions.assertThat(foundUser.getRoles().get(0).getRole()).isEqualTo(Role.USER);
    }


    @Test
    public void findByUsernameWithRoles_존재하지않는_사용자_테스트() {

        // given : 저장된 사용자가 없음
        String username = "xxxxx";

        // when : 존재하지 않는 사용자명으로 조회
        User foundUser = userJpaRepository.findByUsernameWithRoles(username).orElse(null);

        // then : 결과 없음 -> null
        Assertions.assertThat(foundUser).isNull();
    }


    @Test
    public void findByUsername_존재하는_사용자_테스트() {

        // given : 회원가입된 사용자 정보
        User user = User.builder()
                .username("testUser")
                .password("3456")
                .email("test@email.com")
                .build();

        userJpaRepository.save(user);

        // when : 존재하는 사용자명으로 조회
        User foundUser = userJpaRepository.findByUsername("testUser").orElse(null);

        // then : 조회된 사용자 검증
        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getUsername()).isEqualTo("testUser");
    }


    @Test
    public void findByUsername_존재하지않는_사용자_테스트() {

        // given : 존재하지 않는 사용자명
        String username = "xxxxx";
        // when
        User notFoundUser = userJpaRepository.findByUsername(username).orElse(null);
        // then : null 반환 확인
        Assertions.assertThat(notFoundUser).isNull();
    }


    @Test
    public void findByEmail_존재하는_이메일_테스트() {

        // given : 회원가입된 사용자 정보
        User user = User.builder()
                .username("testUser")
                .password("3456")
                .email("test@email.com")
                .build();

        userJpaRepository.save(user);

        // when : 존재하는 이메일로 조회
        User foundUser = userJpaRepository.findByEmail("test@email.com").orElse(null);

        // then : 조회된 사용자 검증
        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getEmail()).isEqualTo("test@email.com");
    }


    @Test
    public void findByEmail_존재하지않는_이메일_테스트() {

        // given : 존재하지 않는 이메일
        String email = "none@email.com";

        // when : 존재하지 않는 이메일로 조회
        User foundUser = userJpaRepository.findByEmail(email).orElse(null);

        // then : null 반환 확인
        Assertions.assertThat(foundUser).isNull();
    }


    @Test
    public void save_회원가입_테스트() {
        // given : 회원 가입시 사용자 정보
        User user = User.
                builder()
                .username("testUser")
                .password("3456")
                .email("test@email.com")
                .build();

        // 저장 전 상태 확인 : ID 는 저장전에 null 이어야 한다.
        Assertions.assertThat(user.getId()).isNull();
        System.out.println("저장 전 User : " + user);

        // when : 회원 가입 실행 (영속화)
        User savedUser = userJpaRepository.save(user);

        // then : 저장된 결과를 검증
        // 1. 자동 생성된 ID 값 확인
        Assertions.assertThat(savedUser.getId()).isNotNull();
        Assertions.assertThat(savedUser.getId()).isGreaterThan(0);

        // 2. 입력한 데이터가 올바르게 저장되었는지 확인
        Assertions.assertThat(savedUser.getCreatedAt()).isNotNull();
        Assertions.assertThat(savedUser.getUsername()).isEqualTo("testUser");
        Assertions.assertThat(savedUser.getPassword()).isEqualTo("3456");
        Assertions.assertThat(savedUser.getEmail()).isEqualTo("test@email.com");

        // 3. 원본 객체와 반환된 객체가 동일한 참조인지 확인
        // 영속성 컨텍스트는 같은 엔티티에 대해 같은 인스턴스를 보장한다.
        Assertions.assertThat(user).isSameAs(savedUser);
    }

}
