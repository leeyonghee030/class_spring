package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;

    //댓글작성
    @PostMapping("/reply/save")
    public String save(ReplyRequestDto.saveDto saveDto, HttpSession session){
        saveDto.validate();
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        replyService.save(saveDto,sessionUserDto.getId());
        return "redirect:/board/" + saveDto.getBoardId();
    }

    //수정
//    @PostMapping("reply/")

    //삭제
    @PostMapping("/reply/{id}/delete")
    public String delectById(@PathVariable(name = "id") Long id,@RequestParam(name = "boardId") Long boardId, HttpSession session){

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        Long sessionUserId = (sessionUserDto == null) ? null : sessionUserDto.getId();
//         Long boardId =
        replyService.deleteById(id,sessionUserId);
        return "redirect:/board/" + boardId;
    }




}
