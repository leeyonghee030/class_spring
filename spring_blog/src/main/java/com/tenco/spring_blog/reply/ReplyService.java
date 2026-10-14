package com.tenco.spring_blog.reply;


import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.board.BoardJpaRepository;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyJpaRepository replyJpaRepository;
    private final BoardJpaRepository boardJpaRepository;
    private final UserJpaRepository userJpaRepository;


    //findAll
    public List<ReplyResponseDto.ListDto> findAllByBoardId(long boardId, Long sessionUserId, Long boardOwnerId) {
        List<Reply> replyList = replyJpaRepository.findAllByBoardIdJoinUser(boardId);
        return replyList.stream()
                .map(reply -> new ReplyResponseDto.ListDto(reply, sessionUserId, boardOwnerId))
                .toList();
    }

    //save
    @Transactional
    public void save(ReplyRequestDto.saveDto saveDto, Long sessionUserId) {
        saveDto.validate();
        Board board = boardJpaRepository.findById(saveDto.getBoardId()).orElseThrow(() -> {
            throw new Exception404("게시글을 찾을 수 없습니다");
        });
        User user = userJpaRepository.findById(sessionUserId).orElseThrow(() -> {
            throw new Exception404("회원 정보를 찾을 수 없습니다");
        });

        replyJpaRepository.save(saveDto.toEntity(user, board));
    }
    //delete
    @Transactional
    public Long deleteById(Long replyId, Long sessionUserId){

        Reply reply = replyJpaRepository.findById(replyId)
                .orElseThrow(() -> new Exception404("댓글을 찾을수 없습니다"));

        if (!reply.isOwner(sessionUserId) && !reply.getBoard().isOwner(sessionUserId)){
            throw new Exception403("댓글 삭제 권한이 없습니다");
        }
        Long boardId = reply.getBoard().getId();
        replyJpaRepository.delete(reply);

        return boardId;
    }



}
