package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception404;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // 스프링을 띄우지 않는 단위 테스트 (Mockito 전용 러너)
public class UserServiceTest {

    @Mock // 가짜(Mock) 객체로 대체
    private UserJpaRepository userJpaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks // 위의 @Mock 들을 UserService 생성자에 주입
    private UserService userService;


    @Test
    public void join_회원가입_성공_테스트() {
        // given : 중복되지 않은 회원가입 정보
        UserRequest.JoinDto joinDto = new UserRequest.JoinDto();
        joinDto.setUsername("tens");
        joinDto.setPassword("1234");
        joinDto.setEmail("tens@email.com");

        given(userJpaRepository.findByUsername("tens")).willReturn(Optional.empty());
        given(userJpaRepository.findByEmail("tens@email.com")).willReturn(Optional.empty());
        given(passwordEncoder.encode("1234")).willReturn("encodedPw");
        // save 호출시 넘겨받은 엔티티를 그대로 반환하도록 설정 (실제 DB 저장을 흉내)
        given(userJpaRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when : 회원가입 실행
        UserResponse.JoinDto result = userService.join(joinDto);

        // then : 응답 DTO 검증
        Assertions.assertThat(result.getUsername()).isEqualTo("tens");
        Assertions.assertThat(result.getEmail()).isEqualTo("tens@email.com");

        // 저장된 엔티티에 인코딩된 비밀번호와 USER 역할이 들어갔는지 확인
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userJpaRepository).save(captor.capture());
        Assertions.assertThat(captor.getValue().getPassword()).isEqualTo("encodedPw");
        Assertions.assertThat(captor.getValue().hasRole(Role.USER)).isTrue();
    }


    @Test
    public void join_중복된_사용자명_예외_테스트() {
        // given : 이미 가입된 사용자명과 같은 이름으로 가입 시도
        UserRequest.JoinDto joinDto = new UserRequest.JoinDto();
        joinDto.setUsername("tens");
        joinDto.setPassword("1234");
        joinDto.setEmail("tens@email.com");

        User existingUser = User.builder()
                .username("tens")
                .password("pw")
                .email("other@email.com")
                .build();
        given(userJpaRepository.findByUsername("tens")).willReturn(Optional.of(existingUser));

        // when & then : 400 예외, 저장은 실행되지 않음
        Assertions.assertThatThrownBy(() -> userService.join(joinDto))
                .isInstanceOf(Exception400.class)
                .hasMessage("이미 존재하는 사용자명 입니다");

        verify(userJpaRepository, never()).save(any(User.class));
    }


    @Test
    public void join_중복된_이메일_예외_테스트() {
        // given : 사용자명은 중복되지 않지만 이메일이 이미 존재함
        UserRequest.JoinDto joinDto = new UserRequest.JoinDto();
        joinDto.setUsername("tens");
        joinDto.setPassword("1234");
        joinDto.setEmail("tens@email.com");

        User existingUser = User.builder()
                .username("other")
                .password("pw")
                .email("tens@email.com")
                .build();
        given(userJpaRepository.findByUsername("tens")).willReturn(Optional.empty());
        given(userJpaRepository.findByEmail("tens@email.com")).willReturn(Optional.of(existingUser));

        // when & then : 400 예외, 저장은 실행되지 않음
        Assertions.assertThatThrownBy(() -> userService.join(joinDto))
                .isInstanceOf(Exception400.class)
                .hasMessage("이미 존재하는 이메일명 입니다");

        verify(userJpaRepository, never()).save(any(User.class));
    }


    @Test
    public void login_로그인_성공_테스트() {
        // given : 저장된 사용자와 일치하는 비밀번호
        User user = User.builder()
                .username("tens")
                .password("encodedPw")
                .email("tens@email.com")
                .build();

        UserRequest.LoginDto loginDto = new UserRequest.LoginDto();
        loginDto.setUsername("tens");
        loginDto.setPassword("1234");

        given(userJpaRepository.findByUsernameWithRoles("tens")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("1234", "encodedPw")).willReturn(true);

        // when : 로그인 실행
        SessionUserDto result = userService.login(loginDto);

        // then : 세션용 사용자 정보 검증
        Assertions.assertThat(result.getUsername()).isEqualTo("tens");
        Assertions.assertThat(result.isAdmin()).isFalse();
    }


    @Test
    public void login_존재하지않는_사용자_예외_테스트() {
        // given : DB에 없는 사용자명
        UserRequest.LoginDto loginDto = new UserRequest.LoginDto();
        loginDto.setUsername("xxxxx");
        loginDto.setPassword("1234");

        given(userJpaRepository.findByUsernameWithRoles("xxxxx")).willReturn(Optional.empty());

        // when & then : 400 예외
        Assertions.assertThatThrownBy(() -> userService.login(loginDto))
                .isInstanceOf(Exception400.class)
                .hasMessage("사용자명 또는 비밀번호가 올바르지 않습니다");
    }


    @Test
    public void login_비밀번호_불일치_예외_테스트() {
        // given : 사용자는 존재하지만 비밀번호가 다름
        User user = User.builder()
                .username("tens")
                .password("encodedPw")
                .email("tens@email.com")
                .build();

        UserRequest.LoginDto loginDto = new UserRequest.LoginDto();
        loginDto.setUsername("tens");
        loginDto.setPassword("wrongPw");

        given(userJpaRepository.findByUsernameWithRoles("tens")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPw", "encodedPw")).willReturn(false);

        // when & then : 400 예외
        Assertions.assertThatThrownBy(() -> userService.login(loginDto))
                .isInstanceOf(Exception400.class)
                .hasMessage("사용자명 또는 비밀번호가 올바르지 않습니다");
    }


    @Test
    public void updateById_비밀번호_변경_성공_테스트() {
        // given : 기존 사용자와 변경할 새 비밀번호 (프로필 이미지는 변경 안 함)
        User user = User.builder()
                .username("tens")
                .password("oldEncodedPw")
                .email("tens@email.com")
                .build();

        UserRequest.UpdateDto updateDto = new UserRequest.UpdateDto();
        updateDto.setPassword("newPw");

        given(userJpaRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.encode("newPw")).willReturn("newEncodedPw");

        // when : 정보 수정 실행
        SessionUserDto result = userService.updateById(1L, updateDto);

        // then : 비밀번호가 인코딩된 새 값으로 바뀌었는지 확인 (더티 체킹 대상 엔티티 직접 확인)
        Assertions.assertThat(user.getPassword()).isEqualTo("newEncodedPw");
        Assertions.assertThat(result.getUsername()).isEqualTo("tens");
    }


    @Test
    public void updateById_존재하지않는_사용자_예외_테스트() {
        // given : 존재하지 않는 사용자 ID
        UserRequest.UpdateDto updateDto = new UserRequest.UpdateDto();
        updateDto.setPassword("newPw");

        given(userJpaRepository.findById(999L)).willReturn(Optional.empty());

        // when & then : 404 예외
        Assertions.assertThatThrownBy(() -> userService.updateById(999L, updateDto))
                .isInstanceOf(Exception404.class)
                .hasMessage("사용자를 찾을 수 없습니다");

        // 사용자를 못 찾았으므로 비밀번호 인코딩까지 가지 않아야 한다
        verify(passwordEncoder, never()).encode(any());
    }


    @Test
    public void deleteProfileImage_프로필이미지_삭제_성공_테스트() {
        // given : 프로필 이미지를 가지고 있는 사용자
        User user = User.builder()
                .username("tens")
                .password("encodedPw")
                .email("tens@email.com")
                .profileImage("old.png")
                .build();

        given(userJpaRepository.findById(1L)).willReturn(Optional.of(user));

        // when : 프로필 이미지 삭제 실행
        userService.deleteProfileImage(1L);

        // then : 엔티티의 프로필 이미지 필드가 null 로 바뀌었는지 확인
        Assertions.assertThat(user.getProfileImage()).isNull();
    }


    @Test
    public void findDetail_사용자_정보_조회_성공_테스트() {
        // given : 조회할 사용자
        // createdAt은 @CreationTimestamp라 세터가 없어서, DetailDto가 날짜 포맷에 쓸 값을
        // 리플렉션으로 직접 채워준다 (실제 DB를 안 타는 단위 테스트이므로 비어있음)
        User user = User.builder()
                .username("tens")
                .password("encodedPw")
                .email("tens@email.com")
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(user, "createdAt", new Timestamp(System.currentTimeMillis()));

        given(userJpaRepository.findById(1L)).willReturn(Optional.of(user));

        // when : 상세 정보 조회
        UserResponse.DetailDto result = userService.findDetail(1L);

        // then : 응답 DTO 필드 확인
        Assertions.assertThat(result.getUsername()).isEqualTo("tens");
        Assertions.assertThat(result.getEmail()).isEqualTo("tens@email.com");
    }


    @Test
    public void findDetail_존재하지않는_사용자_예외_테스트() {
        // given : 존재하지 않는 사용자 ID
        given(userJpaRepository.findById(999L)).willReturn(Optional.empty());

        // when & then : 404 예외
        Assertions.assertThatThrownBy(() -> userService.findDetail(999L))
                .isInstanceOf(Exception404.class)
                .hasMessage("사용자를 찾을 수 없습니다");
    }


    @Test
    public void findUpdateForm_수정화면_조회_성공_테스트() {
        // given : 조회할 사용자
        User user = User.builder()
                .username("tens")
                .password("encodedPw")
                .email("tens@email.com")
                .build();

        given(userJpaRepository.findById(1L)).willReturn(Optional.of(user));

        // when : 수정화면용 조회
        UserResponse.updateFormDto result = userService.findUpdateForm(1L);

        // then : 응답 DTO 필드 확인
        Assertions.assertThat(result.getUsername()).isEqualTo("tens");
    }


    @Test
    public void kakaoLogin_신규_카카오_사용자_자동가입_테스트() {
        // given : 처음 로그인하는 카카오 사용자 정보
        UserResponse.KakaoUserInfo kakaoUserInfo = new UserResponse.KakaoUserInfo();
        kakaoUserInfo.setId(12345L);
        UserResponse.KakaoUserInfo.Properties properties = new UserResponse.KakaoUserInfo.Properties();
        properties.setProfileImage("http://kakao/image.png");
        kakaoUserInfo.setProperties(properties);

        given(userJpaRepository.findByUsername("kakao_12345")).willReturn(Optional.empty());
        given(passwordEncoder.encode(any())).willReturn("encodedRandomPw");
        given(userJpaRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when : 카카오 로그인 실행
        SessionUserDto result = userService.kakaoLogin(kakaoUserInfo);

        // then : 자동 회원가입되어 세션 정보가 반환됨
        Assertions.assertThat(result.getUsername()).isEqualTo("kakao_12345");
        Assertions.assertThat(result.getEmail()).isEqualTo("kakao_12345@kakao.com");
        Assertions.assertThat(result.isAdmin()).isFalse();

        // 신규가입이므로 KAKAO provider, USER 역할로 저장되었는지 확인
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userJpaRepository).save(captor.capture());
        Assertions.assertThat(captor.getValue().getProvider()).isEqualTo(OauthProvider.KAKAO);
        Assertions.assertThat(captor.getValue().hasRole(Role.USER)).isTrue();
    }


    @Test
    public void kakaoLogin_기존_카카오_사용자_로그인_테스트() {
        // given : 이미 가입된 카카오 사용자
        User existingUser = User.builder()
                .username("kakao_12345")
                .password("encodedPw")
                .email("kakao_12345@kakao.com")
                .provider(OauthProvider.KAKAO)
                .build();

        UserResponse.KakaoUserInfo kakaoUserInfo = new UserResponse.KakaoUserInfo();
        kakaoUserInfo.setId(12345L);

        given(userJpaRepository.findByUsername("kakao_12345")).willReturn(Optional.of(existingUser));

        // when : 카카오 로그인 실행
        SessionUserDto result = userService.kakaoLogin(kakaoUserInfo);

        // then : 기존 사용자로 로그인되고, 신규 가입(save)은 일어나지 않음
        Assertions.assertThat(result.getUsername()).isEqualTo("kakao_12345");
        verify(userJpaRepository, never()).save(any(User.class));
    }


    @Test
    public void kakaoLogin_같은이름의_일반회원이_있으면_예외_테스트() {
        // given : 동일한 사용자명으로 가입된 일반(LOCAL) 회원
        User localUser = User.builder()
                .username("kakao_12345")
                .password("encodedPw")
                .email("local@email.com")
                .provider(OauthProvider.LOCAL)
                .build();

        UserResponse.KakaoUserInfo kakaoUserInfo = new UserResponse.KakaoUserInfo();
        kakaoUserInfo.setId(12345L);

        given(userJpaRepository.findByUsername("kakao_12345")).willReturn(Optional.of(localUser));

        // when & then : LOCAL 계정과 이름이 충돌하여 400 예외
        Assertions.assertThatThrownBy(() -> userService.kakaoLogin(kakaoUserInfo))
                .isInstanceOf(Exception400.class)
                .hasMessage("이미 사용중인 사용자 명입니다");
    }

}
