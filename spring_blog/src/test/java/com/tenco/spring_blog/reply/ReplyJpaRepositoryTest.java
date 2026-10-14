package com.tenco.spring_blog.reply;

import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.board.BoardJpaRepository;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

@DataJpaTest
public class ReplyJpaRepositoryTest {

    @Autowired
    private ReplyJpaRepository replyJpaRepository;

    @Autowired
    private BoardJpaRepository boardJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    private User saveUser(String username) {
        User user = User.builder()
                .username(username)
                .password("1234")
                .email(username + "@test.com")
                .build();
        return userJpaRepository.save(user);
    }

    private Board saveBoard(User writer) {
        Board board = Board.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .user(writer)
                .build();
        return boardJpaRepository.save(board);
    }

    @Test
    public void save_댓글_저장_테스트() {
        // given
        User user = saveUser("tens");
        Board board = saveBoard(user);
        Reply reply = Reply.builder()
                .comment("댓글 내용")
                .user(user)
                .board(board)
                .build();

        // when
        Reply savedReply = replyJpaRepository.save(reply);

        // then
        Assertions.assertThat(savedReply.getId()).isNotNull();
        Assertions.assertThat(savedReply.getComment()).isEqualTo("댓글 내용");
        Assertions.assertThat(savedReply.getUser().getUsername()).isEqualTo("tens");
        Assertions.assertThat(savedReply.getBoard().getId()).isEqualTo(board.getId());
        Assertions.assertThat(reply).isSameAs(savedReply);
    }

    @Test
    public void findAllByBoardIdJoinUser_댓글목록_조회_테스트() {
        // given
        User writer = saveUser("writer");
        Board board = saveBoard(writer);

        User commenter1 = saveUser("commenter1");
        User commenter2 = saveUser("commenter2");

        Reply reply1 = replyJpaRepository.save(
                Reply.builder().comment("첫번째 댓글").user(commenter1).board(board).build()
        );
        Reply reply2 = replyJpaRepository.save(
                Reply.builder().comment("두번째 댓글").user(commenter2).board(board).build()
        );

        // when
        List<Reply> replyList = replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then
        Assertions.assertThat(replyList).hasSize(2);
        // order by r.id 이므로 등록 순서대로 조회되어야 함
        Assertions.assertThat(replyList.get(0).getId()).isEqualTo(reply1.getId());
        Assertions.assertThat(replyList.get(1).getId()).isEqualTo(reply2.getId());

        // join fetch로 user까지 같이 가져왔는지 확인 (추가 쿼리 없이 접근 가능해야 함)
        Assertions.assertThat(replyList.get(0).getUser().getUsername()).isEqualTo("commenter1");
        Assertions.assertThat(replyList.get(1).getUser().getUsername()).isEqualTo("commenter2");
    }

    @Test
    public void findAllByBoardIdJoinUser_다른게시글의_댓글은_제외_테스트() {
        // given
        User writer = saveUser("writer");
        Board boardA = saveBoard(writer);
        Board boardB = saveBoard(writer);

        User commenter = saveUser("commenter");
        replyJpaRepository.save(
                Reply.builder().comment("A 게시글 댓글").user(commenter).board(boardA).build()
        );
        replyJpaRepository.save(
                Reply.builder().comment("B 게시글 댓글").user(commenter).board(boardB).build()
        );

        // when
        List<Reply> replyList = replyJpaRepository.findAllByBoardIdJoinUser(boardA.getId());

        // then
        Assertions.assertThat(replyList).hasSize(1);
        Assertions.assertThat(replyList.get(0).getComment()).isEqualTo("A 게시글 댓글");
    }

    @Test
    public void findAllByBoardIdJoinUser_댓글이_없으면_빈리스트_테스트() {
        // given
        User writer = saveUser("writer");
        Board board = saveBoard(writer);

        // when
        List<Reply> replyList = replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then
        Assertions.assertThat(replyList).isEmpty();
    }

    @Test
    public void deleteById_댓글_삭제_테스트() {
        // given
        User user = saveUser("tens");
        Board board = saveBoard(user);
        Reply savedReply = replyJpaRepository.save(
                Reply.builder().comment("삭제할 댓글").user(user).board(board).build()
        );
        Long targetId = savedReply.getId();

        // when
        replyJpaRepository.deleteById(targetId);

        // then
        Assertions.assertThat(replyJpaRepository.findById(targetId)).isEmpty();
    }
}
