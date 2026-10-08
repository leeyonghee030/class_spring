package com.tenco.spring_blog.board;


import com.tenco.spring_blog.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(BoardJpaRepository.class)
@DataJpaTest
public class BoardPersistRepositoryTest {

    @Autowired
    private BoardJpaRepository boardPersistRepository;



    @Test
    public void deleteById_게시글_삭제_테스트() {
        // given
        // 1. 먼저 DB에 테스트용 데이터를 저장하여 삭제할 게시글을 생성 (영속 상태)
        User user = new User(1L, "tens", "1233", "a@na", null);
        Board board = Board.builder()
                .title("삭제할 제목")
                .content("삭제할 내용")
                .user(user)
                .build();
        Board savedBoard = boardPersistRepository.save(board);
        Long targetId = savedBoard.getId();

        // when
        // 2. 저장된 게시글의 ID로 삭제 실행
        boardPersistRepository.deleteById(targetId);

        // then
        // 3. 삭제 후 해당 ID로 조회 시 null이 반환되는지 확인
//        Board deletedBoard = boardPersistRepository.findById(targetId);
//        Assertions.assertThat(deletedBoard).isNull();
    }

    @Test
    public void deleteById_존재하지않는_게시글_삭제시_예외발생_테스트() {
        // given
        Long nonExistentId = 999L;

        // when & then
        // 존재하지 않는 ID 삭제 요청 시 IllegalArgumentException 예외 발생 검증
        Assertions.assertThatThrownBy(() -> boardPersistRepository.deleteById(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제할 게시물을 찾을수 없습니다");
    }


    @Test
    public void save_저장_포함_게시물_테스트() {

//        given
        User user = new User(1L,"tens","1233","a@na",null);
        Board board = Board.builder()
                .title("테스트글")
                .content("테스트내용")
                .user(user)
                .build();

//        when
        Board savedBoard = boardPersistRepository.save(board);

        // then
        // 1. 자동 생성된 ID값 확인
        Assertions.assertThat(savedBoard.getId()).isNotNull();
        Assertions.assertThat(savedBoard.getId()).isGreaterThan(0);

        // 2. 입력한 데이터가 올바르게 저장되었는지 확인
        Assertions.assertThat(savedBoard.getTitle()).isEqualTo("테스트글");
        Assertions.assertThat(savedBoard.getContent()).isEqualTo("테스트내용");

        // 3. 연관관계가 올바르게 저장되었는지 확인
        Assertions.assertThat(savedBoard.getUser()).isNotNull();
        Assertions.assertThat(savedBoard.getUser().getUsername()).isEqualTo("tens");

        // 4 원본 객체와 반환된 객체가 동일한 참조인지 확인
        Assertions.assertThat(board).isSameAs(savedBoard);




    }

}
