package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor // DI 처리
public class BoardController {

    private final BoardPersistRepository boardPersistRepository;

    // GET http://localhost:8080/board/list
    // http://localhost:8080/
    @GetMapping({"/", "/board/list"})
    public String list(Model model) {
        List<Board> boardList = boardPersistRepository.findAll();
        model.addAttribute("boardList", boardList);
        return "board/list";
    }

    // GET http://localhost:8080/board/3
    //excludePathPatterns 제외되어 로그인 접근가능
    @GetMapping("/board/{id}")
    public String detail(@PathVariable(name = "id") Long id, Model model) {

        Board boardEntity = boardPersistRepository.findByIdWithJPQL(id);
        if (boardEntity == null) {
            throw new Exception404("게시글을 찾을 수 없습니다");
        }

        model.addAttribute("board", boardEntity);
        return "board/detail";
    }


    // GET http://localhost:8080/board/save
    @GetMapping("/board/save")
    public String saveForm(HttpSession session) {
        //인터셉터에서 인증검사 진행됨
        return "board/save-form";
    }

    //코드 추가
    // post http://localhost:8080/board/save
    @PostMapping("/board/save")
    //Spring 폼 데이터를 객체로 변환하는 과정 (데이터 바인딩 메커니즘)
    // 폼 데이터 바인딘: Spring이 HTTP 요펑 파라미터를 객체로 자동 변환
    public String save(BoardRequest.SaveDto saveDto, HttpSession session) {
        //1. 인증검사
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        //2. 유효성검사
        //입력데이터검증
        saveDto.validate();
        // Dto에서 Board 객체생성
        //Board저장
        Board savedBoard = boardPersistRepository.save(saveDto.toEntity(sessionUser));
        //저장 성공시 페이지 이동
        return "redirect:/";


    }

    //<a href="/board/{{board.id}}/update" class="btn btn-warning me-1">수정</a>
    // GET http://localhost:8080/board/1/update
    @GetMapping("/board/{id}/update")
    public String updateForm(@PathVariable long id, Model model, HttpSession session) {
        //인증검사
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);

        //2. 권한체크


        Board board = boardPersistRepository.findById(id);
        //3. 권한 체크 : 본인이 작성한 게시글만 수정가능
        if (!board.isOwner(sessionUser.getId())) {
            throw new Exception403("수정 권한이 없습니다");
        }

        model.addAttribute("board", board);
        //PRG 패턴
        // /board/{id}
        return "board/update-form";


    }

    // post http://localhost:8080/board/1/update
    // 게시글 수정 요청 기능
    @PostMapping("/board/{id}/update")
    public String update(@PathVariable long id, BoardRequest.updateDto updateDto, HttpSession session) {

        //인증검사
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        //2. 권한체크
        Board boardEntity = boardPersistRepository.findById(id);

        if (!boardEntity.isOwner(sessionUser.getId())) {
            throw new Exception403("수정 권한이 없습니다");
        }
        //3. 입력 데이터 검사
        updateDto.validate(); // 유효성 실패 (throw 던져짐)

        //4. 더치 체킹으로 업데이트함
        boardPersistRepository.updateById(id, updateDto);
        //PRG 패턴
        // /board/{id}
        //5. 수정완료후 해당 게시글 상세보기로 리다이렉트처리
        return "redirect:/board/" + id; //리다이렉트 수정된 게시글 상세보기 화면이동

    }

    // 게시글 삭제
    //  /board/{{board.id}}/delete method="post
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) throws Exception403 {
        //1. 인증 검사 (로그인 여뷰 확인)
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);


        //2. 권한 확인 (자기 작성한 글인지 여부확인)

        //2. 삭제할 게시글 조회 (권한체크를 위해)
        Board boardEntity = boardPersistRepository.findById(id);
        if (!boardEntity.isOwner(sessionUser.getId())) {
            throw new Exception403("삭제 권한이 없습니다");
        }
        //권한 확인후 삭제 실행
        boardPersistRepository.deleteById(id);
        //삭제 성공ㄹ후 메인 페이지로 리다이렉트
        return "redirect:/";
    }

}




