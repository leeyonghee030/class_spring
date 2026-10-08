package com.tenco.spring_blog.board;


import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/*
Board 관련 비즈니스 로직을 처리하는 Service 계층
 */
@Slf4j
@RequiredArgsConstructor
@Service  // IoC 싱글톤 패턴으로 처리됨
@Transactional(readOnly = true)
// 모든 메서드를 읽기 전용 트랜잭션으로 실행 (findAll, findById등 조회에 적합)
// 성능 최적화 (변경 감지 비활성화), 데이터 수정 방지
// 데이터베이스 락 (lock)를 최소화해 동시성 성능 개선
public class BoardService {

    private final BoardJpaRepository boardJpaRepository;

    /**
     * 게시글 저장
     *
     * @param saveDto     게시글 저장 정보
     * @param sessionUser 작성자 정보
     * @return 저장된 게시글 반환
     */
    @Transactional
    // 데이터 수정이 필요하므로 트랜젝션 읽기 전용 설정을 해제하고 쓰기 가능한 트랜젝션으로 실행돰
    public Board save(BoardRequest.SaveDto saveDto, User sessionUser) {
        log.info("게시글 저장 시작 -제목 : {}, 작성자 : {}", saveDto.getTitle(), sessionUser.getUsername());
        Board board = saveDto.toEntity(sessionUser);
        Board savedBoard = boardJpaRepository.save(board);

        log.info("게시글 저장 완료 -ID : {}, 제목 : {}", savedBoard.getId(), savedBoard.getTitle());

        return savedBoard;
    }


    /**
     *  게시글 상세 조회
     * @param id  게시글 PK
     * @return  게시글 정보 (작성자 정보 포함)
     */
    public Board findById(Long id) {
        log.info("상세 게시글 조회 시작 -ID : {}", id);
        //boardJpaRepository.findById 이 메서드 대신 Join fetch를 사용함
        Board board = boardJpaRepository.findByIdJoinUser(id) //직접 정의한 쿼리
                .orElseThrow(() -> {
                    log.warn("게시글 조회 실패  - ID : {}",id);
                    return new Exception404("게시글을 찾을수 없습니다");
                });


        log.info("상세 게시글 조회 완료 -제목 : {}, 작성자 : {}",board.getTitle(),board.getUser().getUsername());

        return board;
    }

    /**
     *  게시글 목록 조회 (메인페이지용)
     * @return 게시글 목록(작성자 정보 포함)
     */
    public List<Board> findAll() {
        log.info("게시글 목록 조회 시작 ");
        // 대신 Join fetch를 사용함
        List<Board> boardList = boardJpaRepository.findAllJoinUser();
        log.info("게시글 목록 조회 완료 ");
        return boardList;
    }

    /**
     *  게시글 수정 (권한 체크)
     * @param id  게시글 pk
     * @param updateDto 게시글 수정 정보
     * @param sessionUser 게시글 작성자 정버(요청자)
     * @return  수정된 게시글 리턴
     */
    @Transactional
    public Board update(Long id,BoardRequest.updateDto updateDto, User sessionUser) {

        log.info("게시글 수정 시작");
        //게시글 조회
        Board board = findById(id);

        //권한
        if (!board.isOwner(sessionUser.getId())) {
            throw  new Exception403("본인이 작성한 게시글만 수정 할 수 있습니다");
        }


        //더치 체킹을 활용한 수정
        board.update(updateDto);
        log.info("게시글 수정 완료");

        return board;
    }


    // 게시글 삭제
    @Transactional
    public void delete(Long id,User sessionUser) {

        log.info("게시글 삭제 시작");
        Board board = findById(id);

        if (!board.isOwner(sessionUser.getId())){
            log.warn("게시글 삭제 권한 없음 - 게시글 ID : {}, 작성자 : {}, 요청자 : {}"
            ,id,board.getUser().getUsername(),sessionUser.getUsername());
            throw new Exception403("본인이 작성한 게시글만 삭제 가능합니다");
        }

        boardJpaRepository.deleteById(id);
        log.info("게시글 삭제 완료");
    }






}
