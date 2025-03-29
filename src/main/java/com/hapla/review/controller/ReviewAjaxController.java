package com.hapla.review.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.hapla.comm.model.service.CommService;
import com.hapla.comm.model.vo.Reply;
import com.hapla.review.model.service.ReviewService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/review", "/comm"})
@RequiredArgsConstructor
@SessionAttributes("loginUser")
public class ReviewAjaxController {
	
	private final ReviewService reviewService;
	private final CommService commService;
	
	@PostMapping("reply")
	public ArrayList<Map<String, Object>> insertReply(@ModelAttribute Reply r, HttpServletResponse response){
	    System.out.println(r);
	    
	    // 현재 시간 설정
	    r.setCreateDate(new java.sql.Date(System.currentTimeMillis()));
	    
	    // DB에 저장 (댓글 등록)
	    int result = commService.insertReply(r);
	    
	    // 날짜 포맷 설정
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	    // 댓글 목록 조회
	    ArrayList<Reply> list = commService.selectReplyList(r.getCommNo());
	    
	    // 변환된 리스트 담기
	    ArrayList<Map<String, Object>> formattedList = new ArrayList<>();
	    
	    for (Reply reply : list) {
	        Map<String, Object> replyMap = new HashMap<>();
	        
	        replyMap.put("replyNo", reply.getReplyNo());
	        replyMap.put("userNo", reply.getUserNo());
	        replyMap.put("commNo", reply.getCommNo());
	        replyMap.put("name", reply.getName());
	        replyMap.put("replyContent", reply.getReplyContent());
	        
	        // 포맷팅된 createDate 문자열로 변환
	        replyMap.put("createDate", sdf.format(reply.getCreateDate()));
	        
	        // updateDate가 null이 아니면 포맷팅해서 넣기
	        replyMap.put("updateDate", reply.getUpdateDate() != null ? sdf.format(reply.getUpdateDate()) : null);
	        
	        replyMap.put("nickname", reply.getNickname());
	        replyMap.put("title", reply.getTitle());
	        replyMap.put("id", reply.getId());
	        replyMap.put("commTitle", reply.getCommTitle());
	        replyMap.put("status", reply.getStatus());
	        
	        formattedList.add(replyMap);
	    }
	    
	    return formattedList;  // 포맷팅된 리스트 반환
	}
	
	@DeleteMapping("reply")
	public int deleteReply(@RequestParam("replyNo") int replyNo) {
		return commService.deleteReply(replyNo);

	}

	@PutMapping("reply")
	public int updateReply(@ModelAttribute Reply r) {
		System.out.println("수정 요청 - replyNo: " + r.getReplyNo() + ", replyContent: " + r.getReplyContent());
		int result = commService.updateReply(r);
		return result;
	}
	
	
	
	
	@PostMapping("/like")
	public ResponseEntity<Map<String, Object>> toggleLike(@RequestParam("commNo") int commNo,
	                                                      @RequestParam("userNo") int userNo) {
	    Map<String, Object> response = new HashMap<>();

	    try {
	        System.out.println("좋아요 요청 - userNo: " + userNo + ", commNo: " + commNo);

	        boolean isLiked = commService.checkUserLike(userNo, commNo) > 0;
	        System.out.println("좋아요 상태 확인 - isLiked: " + isLiked);

	        if (isLiked) {
	            commService.removeLike(userNo, commNo);
	        } else {
	            commService.addLike(userNo, commNo);
	        }

	        int updatedLikeCount = commService.getLikeCount(commNo);
	        
	        System.out.println(updatedLikeCount);
	        System.out.println("좋아요 개수 업데이트 완료 - updatedLikeCount: " + updatedLikeCount);

	        response.put("status", "success");
	        response.put("likes", updatedLikeCount);
	        response.put("isLiked", !isLiked);
	        return ResponseEntity.ok(response);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.put("status", "error");
	        response.put("message", "서버 오류 발생: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
	
	@PostMapping("/likes")
	public ResponseEntity <Map<String, Object>> toggleLikes(@RequestParam("reviewNo") int reviewNo,
															@RequestParam("userNo") int userNo) {
		Map<String, Object> response = new HashMap<>();

	    try {
	        System.out.println("좋아요 요청 - userNo: " + userNo + ", reviewNo: " + reviewNo);

	        boolean isLiked = reviewService.checkUserLike(userNo, reviewNo) > 0;
	        System.out.println("좋아요 상태 확인 - isLiked: " + isLiked);

	        if (isLiked) {
	            reviewService.removeLike(userNo, reviewNo);
	        } else {
	            reviewService.addLike(userNo, reviewNo);
	        }

	        int updatedLikeCount = reviewService.getLikeCount(reviewNo);
	        
	        System.out.println(updatedLikeCount);
	        System.out.println("좋아요 개수 업데이트 완료 - updatedLikeCount: " + updatedLikeCount);

	        
	        response.put("status", "success");
	        response.put("likes", updatedLikeCount);
	        response.put("isLiked", !isLiked);
	        return ResponseEntity.ok(response);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.put("status", "error");
	        response.put("message", "서버 오류 발생: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
	


	
	
	
	
}