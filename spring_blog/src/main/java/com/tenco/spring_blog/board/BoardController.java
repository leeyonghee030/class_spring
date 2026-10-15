package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.reply.ReplyResponseDto;
import com.tenco.spring_blog.reply.ReplyService;
import com.tenco.spring_blog.user.SessionUserDto;
import com.tenco.spring_blog.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.naming.Name;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor // DI 처리
public class BoardController {

    //
    private final BoardService boardService;
    private  final ReplyService replyService;

    // GET http://localhost:8080/board/list
    // http://localhost:8080/
    @GetMapping({"/", "/board/list"})
    public String list(@RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "3") int size, Model model) {
        BoardResponse.PageDto pageDto = boardService.findAllPage(page,size);
        model.addAttribute("boardPage",pageDto);
        return "board/list";
    }

    // GET http://localhost:8080/board/3
    //excludePathPatterns 제외되어 로그인 접근가능
    @GetMapping("/board/{id}")
    public String detail(@PathVariable(name = "id") Long id, Model model,HttpSession session) {

        BoardResponse.DetailDto detailDto = boardService.findById(id);
        model.addAttribute("board", detailDto);
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        boolean isOwner = sessionUserDto != null && detailDto.getUserId().equals(sessionUserDto.getId());
        Long sessionUserId = (sessionUserDto == null) ? null : sessionUserDto.getId();
        List<ReplyResponseDto.ListDto> replyList = replyService.findAllByBoardId(id, sessionUserId, detailDto.getUserId());

        model.addAttribute("isOwner", isOwner);
        model.addAttribute("replyList", replyList);

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
        SessionUserDto sessionUser = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        //2. 유효성검사
        saveDto.validate();
        boardService.save(saveDto, sessionUser);
        //저장 성공시 페이지 이동
        return "redirect:/";


    }


    // GET http://localhost:8080/board/1/update
    @GetMapping("/board/{id}/update")
    public String updateForm(@PathVariable long id, Model model, HttpSession session) {

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        BoardResponse.UpdateDto updateFormDto = boardService.findByIdAndUpdateForm(id,sessionUserDto.getId());

        model.addAttribute("board", updateFormDto);
        return "board/update-form";


    }

    // post http://localhost:8080/board/1/update
    // 게시글 수정 요청 기능
    @PostMapping("/board/{id}/update")
    public String update(@PathVariable long id, BoardRequest.UpdateDto updateDto, HttpSession session) {

        updateDto.validate();
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        boardService.update(id, updateDto, sessionUserDto);
        return "redirect:/board/" + id; //리다이렉트 수정된 게시글 상세보기 화면이동

    }

    // 게시글 삭제
    //  /board/{{board.id}}/delete method="post
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) throws Exception403 {


        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        boardService.delete(id, sessionUserDto);
        return "redirect:/";
    }

}




