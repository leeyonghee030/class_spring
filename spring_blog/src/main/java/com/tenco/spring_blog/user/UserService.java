package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception401;
import com.tenco.spring_blog._core.error.Exception404;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.plaf.PanelUI;


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
    public User join(UserRequest.JoinDto joinDto) {
        log.info("회원가입 서비스 시작 -사용자 명: {}", joinDto.getUsername());

        // 값이 있다면 에러가 실행된다 (중복이면 에러를 던지기위해 .ifPresent)
        userJpaRepository.findByUsername(joinDto.getUsername())
                .ifPresent(user -> {
                    log.warn("회원가입 실패- 중복된 사용자명: {}", joinDto.getUsername());
                    throw new Exception400("이미 존재하는 사용자명 입니다");
                });

        //저장
        User savedUser = userJpaRepository.save(joinDto.toEntity());

        log.info("회원가입 서비스 완료 -사용자 명 : {}, ID : {}", joinDto.getUsername(), savedUser.getId());
        return savedUser;
    }

    /**
     * 로그인 처리
     *
     * @param loginDto 로그인 정보
     * @return 로그인 된 사용자 정보
     */
    // 로그인
    public User login(UserRequest.LoginDto loginDto) {
        log.info("로그인 서비스 시작 -사용자 명: {}", loginDto.getUsername());

        User user = userJpaRepository.findByUsernameAndPassword(
                        loginDto.getUsername(), loginDto.getPassword())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 사용자몇 : {}, 원인 : 인증정보 불일치", loginDto.getUsername());
                    return new Exception400("사용자명 또는 비밀번호가 올바르지 않습니다");
                });

        log.info("로그인 성공 -사용자 명: {}", loginDto.getUsername());
        return user;
    }

    /**
     *  사용자 정보조회
     * @param id 사용자 PK
     * @return 사용자 정보
     */
    public User findById(Long id) {
        log.info("사용자 정보 조회 -사용자 ID: {}", id);
        return userJpaRepository.findById(id).orElseThrow(()->{
            throw new Exception404("사용자를 찾을 수 없습니다");
        });
    }

    // 사용자 정보 수정

    /**
     * 회원정보 수정 처리
     * @param id 사용자 Pk
     * @param updateDto 수정할 정보
     * @return 수정된 사용자 정보
     */
    @Transactional
    public User updateById(Long id, UserRequest.UpdateDto updateDto) {
        log.info("사용자 정보 수정 시작 ");

        User user = findById(id);
        user.update(updateDto.getPassword());
        return user;


    }



}
