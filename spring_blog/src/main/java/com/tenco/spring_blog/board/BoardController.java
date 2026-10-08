package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception403;
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

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor // DI 처리
public class BoardController {

    //
    private final BoardService boardService;

    // GET http://localhost:8080/board/list
    // http://localhost:8080/
    @GetMapping({"/", "/board/list"})
    public String list(Model model) {
        List<Board> boardList = boardService.findAll();
        model.addAttribute("boardList", boardList);
        return "board/list";
    }

    // GET http://localhost:8080/board/3
    //excludePathPatterns 제외되어 로그인 접근가능
    @GetMapping("/board/{id}")
    public String detail(@PathVariable(name = "id") Long id, Model model) {

        Board board = boardService.findById(id);
        model.addAttribute("board", board);
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
        //1. user
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        //2. 유효성검사
        saveDto.validate();
        boardService.save(saveDto, sessionUser);
        //저장 성공시 페이지 이동
        return "redirect:/";


    }

    //TODO 1. 인가처리를 서비스단으로 이동 예정 
    // GET http://localhost:8080/board/1/update
    @GetMapping("/board/{id}/update")
    public String updateForm(@PathVariable long id, Model model, HttpSession session) {
        Board board = boardService.findById(id);
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        if (!board.isOwner(sessionUser.getId())){
            throw new Exception403("수정할 권한이 없습니다");
        }
        model.addAttribute("board", board);
        return "board/update-form";


    }

    // post http://localhost:8080/board/1/update
    // 게시글 수정 요청 기능
    @PostMapping("/board/{id}/update")
    public String update(@PathVariable long id, BoardRequest.updateDto updateDto, HttpSession session) {

        updateDto.validate();
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        boardService.update(id, updateDto, sessionUser);
        return "redirect:/board/" + id; //리다이렉트 수정된 게시글 상세보기 화면이동

    }

    // 게시글 삭제
    //  /board/{{board.id}}/delete method="post
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) throws Exception403 {

        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        boardService.delete(id, sessionUser);
        return "redirect:/";
    }

}




