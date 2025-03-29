package com.hapla.comm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hapla.comm.model.service.CommService;
import com.hapla.comm.model.vo.Comm;
import com.hapla.comm.model.vo.Reply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@RequestMapping("/report")
public class ContentViewController {

    private final CommService commService;
    
    @GetMapping("/content")
    @ResponseBody
    public ResponseEntity<?> getReportedContent(
            @RequestParam(value = "category") String category,
            @RequestParam(value = "contentNo") int contentNo) {
               
        try {
            if ("C".equals(category)) {
                // 게시글 조회
                Comm comm = commService.selectComm(contentNo, null);
                return ResponseEntity.ok(comm);
            } else if ("R".equals(category)) {
                // 댓글 조회
                Reply reply = commService.selectReply(contentNo);
                if (reply != null) {
                    // 댓글이 속한 게시글 정보도 함께 가져오기
                    Comm comm = commService.selectComm(reply.getCommNo(), null);
                    reply.setCommTitle(comm.getTitle());
                }
                return ResponseEntity.ok(reply);
            } else {
                return ResponseEntity.badRequest().body("지원하지 않는 카테고리입니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("콘텐츠 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}

