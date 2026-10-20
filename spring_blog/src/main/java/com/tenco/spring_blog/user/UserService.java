package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog._core.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


/*
 서비스 레이어
 핵심 개념
 - 비즈니스 로직을 처리하는 계층
 - Controller 와 Repository 사이의 중간 계층 담당
 - 트랙젝션 관리
 - 계층 구조 (3 Tier 아키텍처) c -> s-> r -> DB
 */

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserJpaRepository userJpaRepository;


    //회원가입

    /**
     * User 회원가입
     *
     * @param joinDto 회원가입할 정보
     * @return 회원가입된 정보
     */
    @Transactional //쓰기 작업으로
    public UserResponse.JoinDto join(UserRequest.JoinDto joinDto) {
        log.info("회원가입 서비스 시작 -사용자 명: {}", joinDto.getUsername());

        // 값이 있다면 에러가 실행된다 (중복이면 에러를 던지기위해 .ifPresent)
        userJpaRepository.findByUsername(joinDto.getUsername())
                .ifPresent(user -> {
                    log.warn("회원가입 실패- 중복된 사용자명: {}", joinDto.getUsername());
                    throw new Exception400("이미 존재하는 사용자명 입니다");
                });

        // 두번쨰 이메일 중복 검사
        userJpaRepository.findByEmail(joinDto.getEmail()).ifPresent(user -> {
            log.warn("회원가입 실패- 중복된 이메일명: {}", joinDto.getEmail());
            throw new Exception400("이미 존재하는 이메일명 입니다");
        });
        //3. 프로필 이미지 저장  새롭게 생긴 파일 이름이 리턴됨
        String profileImage = FileUtil.saveImage(joinDto.getProfileImage());
        // 4. 프로필 파일 이름과 회원정보저장
        User user = joinDto.toEntity(profileImage);
        user.addRole(Role.USER);

        User savedUser = userJpaRepository.save(user);

        log.info("회원가입 서비스 완료 -사용자 명 : {}, ID : {}", joinDto.getUsername(), savedUser.getId());
        return new UserResponse.JoinDto(savedUser);
    }

    /**
     * 로그인 처리
     *
     * @param loginDto 로그인 정보
     * @return 로그인 된 사용자 정보
     */
    // 로그인
    public SessionUserDto login(UserRequest.LoginDto loginDto) {
        log.info("로그인 서비스 시작 -사용자 명: {}", loginDto.getUsername());

        User user = userJpaRepository.findByUsernameAndPassword(
                        loginDto.getUsername(), loginDto.getPassword())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 사용자몇 : {}, 원인 : 인증정보 불일치", loginDto.getUsername());
                    return new Exception400("사용자명 또는 비밀번호가 올바르지 않습니다");
                });

        log.info("로그인 성공 -사용자 명: {}", loginDto.getUsername());
        return new SessionUserDto(user);
    }

    /**
     * 사용자 정보조회
     *
     * @param id 사용자 PK
     * @return 사용자 정보
     */
    private User findById(Long id) {
        log.info("사용자 정보 조회 -사용자 ID: {}", id);
        return userJpaRepository.findById(id).orElseThrow(() -> {
            throw new Exception404("사용자를 찾을 수 없습니다");
        });
    }

    // 사용자 정보 수정

    /**
     * 회원정보 수정 처리
     *
     * @param id        사용자 Pk
     * @param updateDto 수정할 정보
     * @return 수정된 사용자 정보
     */
    //정보 수정후 세션 동기화용
    //세선 동기화용 DTo반환
    @Transactional
    public SessionUserDto updateById(Long id, UserRequest.UpdateDto updateDto) {
        log.info("사용자 정보 수정 시작 ");

        User user = findById(id);

        if (updateDto.hasPassword()) {
            user.update(updateDto.getPassword());
        }

        if (updateDto.hasProfileImage()) {
            String newImage = FileUtil.saveImage(updateDto.getProfileImage());
            String oldImage = user.getProfileImage();
            user.updateProfileImage(newImage);
            // 기존 파일은 서버측 폴더에서 삭제처리
            FileUtil.deleteFile(oldImage);
        }

        return new SessionUserDto(user);

    }


    @Transactional
    public void deleteProfileImage(Long userId) {
        User user = findById(userId);
        FileUtil.deleteFile(user.getProfileImage());
        user.updateProfileImage(null);
    }

    /**
     * 회원 정보 수정화면 : 화면에 필요한 값만 담는 DTO반환
     *
     * @param userId
     * @return
     */
    public UserResponse.updateFormDto findUpdateForm(Long userId) {
        User user = findById(userId);
        return new UserResponse.updateFormDto(user);

    }


    public UserResponse.DetailDto findDetail(Long userId) {
        User user = findById(userId);
        return new UserResponse.DetailDto(user);
    }

    //카카오 로그인 : 처음 ? 회원가입 : 로그인
    @Transactional
    public SessionUserDto kakaoLogin(UserResponse.KakaoUserInfo kakaoUserInfo) {
        //1. 카카오 회원 번호로 우리 서비스의 사용자명 만들기
        String username = "kakao_" + kakaoUserInfo.getId();

        //2. 이미 가입했는지 확인 없으면 자동 회원가입
        User user = userJpaRepository.findByUsername(username)
                .orElseGet(()-> joinKakaoUser(username, kakaoUserInfo));

        //3. 같은 이름의 일반 회원이 있다면 카카오 사용자로 로그인 시키지않음
        if (user.getProvider() != OauthProvider.KAKAO){
            throw new Exception400("이미 사용중인 사용자 명입니다");
        }

        //트랙젝션 안에서 sessionsUserDto 생성

        return new SessionUserDto(user);

    }

    private User joinKakaoUser(String username, UserResponse.KakaoUserInfo kakaoUserInfo) {
        User user = User.builder()
                .username(username)
                .email(username+"@kakao.com")
                .password(UUID.randomUUID().toString())
                .profileImage(kakaoUserInfo.getProperties().getProfileImage())
                .provider(OauthProvider.KAKAO)
                .build();
        user.addRole(Role.USER);

        return userJpaRepository.save(user);
    }
}
