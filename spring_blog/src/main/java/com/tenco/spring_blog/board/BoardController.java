package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.purchase.PurchaseService;
import com.tenco.spring_blog.reply.ReplyResponseDto;
import com.tenco.spring_blog.reply.ReplyService;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor // DI 처리
public class BoardController {

    //
    private final BoardService boardService;
    private final ReplyService replyService;
    private final PurchaseService purchaseService;
            //유료 게시글 구먀 요청  URL만들기

    // board/{}
    @PostMapping("/board/{id}/purchase")
    public String purchase(@PathVariable(name = "id") Long id,HttpSession session ){
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        purchaseService.purchase(id,sessionUserDto.getId());
        //구매 완료 했다면  상세보기 로 이동 처리 (이제 본문이 나와야함 )
        return "redirect:/board/"+id;
    }


    // GET http://localhost:8080/board/list
    // http://localhost:8080/
    @GetMapping({"/", "/board/list"})
    public String list(@RequestParam(name = "page", defaultValue = "1") int page,
                       @RequestParam(name = "size", defaultValue = "3") int size,
                       @RequestParam(name = "keyword", defaultValue = "") String keyword
            , Model model) {
        BoardResponse.PageDto pageDto = boardService.findAllPage(page, size, keyword);
        model.addAttribute("boardPage", pageDto);
        model.addAttribute("keyword", keyword);
        model.addAttribute("isSearch", !keyword.isBlank());

        //페이지 링크는 직접 URL 인코딩이 필요하다
        model.addAttribute("keywordParam", URLEncoder.encode(keyword, StandardCharsets.UTF_8));

        return "board/list";
    }

    // GET http://localhost:8080/board/3
    //excludePathPatterns 제외되어 로그인 접근ㅌ가능
    @GetMapping("/board/{id}")
    public String detail(@PathVariable(name = "id") Long id, Model model, HttpSession session) {

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        Long sessionUserId = (sessionUserDto == null) ? null : sessionUserDto.getId();

        BoardResponse.DetailDto detailDto = boardService.findById(id,sessionUserId);


        boolean isOwner = sessionUserDto != null && detailDto.getUserId().equals(sessionUserDto.getId());


        List<ReplyResponseDto.ListDto> replyList = replyService.findAllByBoardId(id, sessionUserId, detailDto.getUserId());

        model.addAttribute("board", detailDto);
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

        BoardResponse.UpdateDto updateFormDto = boardService.findByIdAndUpdateForm(id, sessionUserDto.getId());

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




