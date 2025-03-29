package com.hapla.admin.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.hapla.admin.model.service.AdminService;
import com.hapla.admin.model.vo.AdminUsers;
import com.hapla.admin.model.vo.DashBoard;
import com.hapla.admin.model.vo.Notice;
import com.hapla.admin.model.vo.Report;
import com.hapla.comm.model.vo.Comm;
import com.hapla.comm.model.vo.Reply;
import com.hapla.common.PageInfo;
import com.hapla.common.Pagination;
import com.hapla.review.model.vo.Review;
import com.hapla.users.model.vo.Users;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminController {
	
	private final AdminService aService;
	
	
	@GetMapping("/admin/index")
	public String index(Model model) {
		// 전체 데이터 조회
				int totalUsers = aService.totalUsersCount();
				int totalComm = aService.totalComm();
				int totalReply = aService.totalReplyCount();
				int totalReview = aService.totalReview();
				
				model.addAttribute("totalUsers", totalUsers);
				model.addAttribute("totalComm", totalComm);
				model.addAttribute("totalReply", totalReply);
				model.addAttribute("totalReview", totalReview);
		
		return "/admin/index";
	}
	
	@GetMapping("/admin/members")
	public String members(@RequestParam(value="userPage", defaultValue = "1") int currentPage, @RequestParam(value = "keyword", required = false) String keyword, @RequestParam(value="order",defaultValue = "new") String order, Model model,HttpServletRequest request ) {
		
		
		// 전체 회원 수 조회
		int listCount = aService.userListCount(keyword);
		
		PageInfo pi = Pagination.getPageInfo(currentPage, listCount, 7);
		
		// 회원 목록 조회
		ArrayList<AdminUsers> list = aService.selectUserList(pi, keyword, order);
		
		
		model.addAttribute("list", list);
		model.addAttribute("pi", pi);
		model.addAttribute("keyword", keyword);
		model.addAttribute("order", order);
		model.addAttribute("loc", request.getRequestURI());
		return "/admin/members";
	}
	
	
	@GetMapping("admin/userStats")
	public String userStats( Model model, @RequestParam(value="page",defaultValue="1")int currentPage, @RequestParam(value="nickname",required=false) String nickname, 
							@RequestParam(value="order", defaultValue="new")String order,@RequestParam(value="type",defaultValue="comm") String type, HttpServletRequest request) {
		
		// 전체 데이터 조회
		int totalUsers = aService.totalUsersCount();
		int totalComm = aService.totalComm();
		int totalReply = aService.totalReplyCount();
		int totalReview = aService.totalReview();
		
		model.addAttribute("totalUsers", totalUsers);
		model.addAttribute("totalComm", totalComm);
		model.addAttribute("totalReply", totalReply);
		model.addAttribute("totalReview", totalReview);
		
		//타입에 따른 목록 조회
		if("comm".equals(type)) {
			int listCount = aService.commListCount(nickname);
			PageInfo pi = Pagination.getPageInfo(currentPage, listCount, 5);
			ArrayList<Comm> list = aService.selectComm(pi,nickname,order);
			
			model.addAttribute("list", list);
			model.addAttribute("pi", pi);
			
		}else if("reply".equals(type)){
			int listCount = aService.replyListCount(nickname);
			PageInfo pi = Pagination.getPageInfo(currentPage, listCount, 5);
			ArrayList<Reply> list = aService.selectReply(pi,nickname,order);
			
			model.addAttribute("list", list);
			model.addAttribute("pi", pi);
			
		}else if("review".equals(type)) {
			int listCount = aService.reviewListCount(nickname);
			PageInfo pi = Pagination.getPageInfo(currentPage, listCount, 5);
			ArrayList<Review> list = aService.selectReview(pi,nickname,order);
			
			model.addAttribute("list", list);
			model.addAttribute("pi", pi);
		}
		
		model.addAttribute("nickname", nickname);
		model.addAttribute("order", order);
		model.addAttribute("type", type);
		model.addAttribute("loc", request.getRequestURI());
		
		List<String> headers = aService.headers(type);
		model.addAttribute("headers", headers);
		
		return "/admin/userStats";
	}	
	
	
	@PostMapping("admin/deleteComm")
	@ResponseBody
	public HashMap<String, Boolean> deleteComm(@RequestParam("commNo") int commNo) {
		boolean success = aService.deleteComm(commNo);
		HashMap<String,Boolean> map = new HashMap<String,Boolean>();
		map.put("success", success);
		return map;
	}
	
	@PostMapping("admin/deleteReview")
	@ResponseBody
	public HashMap<String, Boolean> deleteReview(@RequestParam("reviewNo") int reviewNo) {
		boolean success = aService.deleteReview(reviewNo);
		HashMap<String,Boolean> map = new HashMap<String,Boolean>();
		map.put("success", success);
		return map;
	}
	
	@PostMapping("admin/deleteReply")
	@ResponseBody
	public HashMap<String, Boolean> deleteReply(@RequestParam("replyNo") int replyNo) {
		boolean success = aService.deleteReply(replyNo);
		HashMap<String,Boolean> map = new HashMap<String,Boolean>();
		map.put("success", success);
		return map;
	}
	
	@PostMapping("admin/memberUpdate")
	@ResponseBody 
	public String updateMember(@ModelAttribute Users user) {
	    int result = aService.updateMember(user);
        return result > 0 ? "success" : "fail";
	}
	
	
	@GetMapping("admin/report") 
	public String report(Model model,@RequestParam(value="page",defaultValue="1") int currentPage,HttpServletRequest request,@RequestParam(value="keyword",required=false) String keyword, 
						@RequestParam(value="order",defaultValue="new") String order) {
		int listCount = aService.reportListCount();
		PageInfo pi = Pagination.getPageInfo(currentPage, listCount, 5);
		
		ArrayList<Report> list = aService.selectReportList(pi,keyword,order);
		int totalWait = aService.totalWait();
		int totalAccept = aService.totalAccept();
		int totalReject = aService.totalReject();
		
		model.addAttribute("list", list);
		model.addAttribute("pi", pi);
		model.addAttribute("loc", request.getRequestURI());
		model.addAttribute("totalWait", totalWait);
		model.addAttribute("totalAccept", totalAccept);
		model.addAttribute("totalReject", totalReject);
		return "/admin/report";
	}
	
	@PostMapping("admin/status")
	@ResponseBody
	public String updateStatus(@ModelAttribute Report report, @RequestParam("reportNo") int reportNo, @RequestParam("reportStatus") String reportStatus) {
	        report.setReportNo(reportNo);
	        report.setReportStatus(reportStatus);
	        
	        // 신고 상태 업데이트
	        int result = aService.updateStatus(report);
	        
	        // 승인된 경우 해당 게시글/댓글 삭제
	        if (reportStatus.equals("A")) {
	            // 신고 대상 카테고리 확인 후 삭제 처리
	            Report reportDetail = aService.getReportDetail(reportNo); // 신고 목록 조회(3회 이상 신고된 항목만 조회)
	            if (reportDetail != null) {
	                if ("C".equals(reportDetail.getReportCategory())) {
	                    // 게시글인 경우
	                    int commNo = aService.getCommNo(reportNo);
	                    if (commNo > 0) {
	                        aService.deleteCommReport(commNo);
	                    }
	                } else if ("R".equals(reportDetail.getReportCategory())) {
	                    // 댓글인 경우
	                    int replyNo = aService.getReplyNo(reportNo);
	                    if (replyNo > 0) {
	                        aService.deleteReplyReport(replyNo);
	                    }
	                }
	            }
	        }
	        return result > 0 ? "success" : "fail";
	    }
	
	// 공지사항 상세 페이지
	@GetMapping("admin/detail/{id}/{page}")
	public ModelAndView detail(@PathVariable("id") int noticeNo, @PathVariable("page") int page, ModelAndView mv) {
	    Notice notice = aService.selectNotice(noticeNo);
	    mv.addObject("notice", notice).addObject("page", page).setViewName("admin/detail");
	    return mv;
	}
	
	// 공지사항 목록 페이지
	@GetMapping("admin/notice") 
	public String notice(@RequestParam(value="page",defaultValue="1")int currentPage,Model model) {
		int listCount = aService.noticeCount();
		PageInfo pi = Pagination.getPageInfo(currentPage, listCount, 3);
		
		ArrayList<Notice> list = aService.selectNoticeList(pi);
		
		model.addAttribute("list", list);
		model.addAttribute("pi", pi);
		
		return "/admin/notice";
	}
	


	
	// 공지사항 작성 뷰페이지 
	@GetMapping("admin/noticeWrite") 
	public String noticeWriteView() {
	   return "/admin/noticeWrite";
	}

	
	@PostMapping("admin/noticeWrite")
	public String noticeWrite(@ModelAttribute Notice notice) {
	    // 서버에서 현재 날짜 설정
	    notice.setCreateDate(new Date());
	    
	    int result = aService.insertNotice(notice);
	    return "redirect:/admin/notice";
	}
	
	// 공지사항 수정 페이지
	@GetMapping("admin/noticeEdit/{id}") 
	public String noticeEditView(@PathVariable("id") int noticeNo, Model model) {
	    Notice notice = aService.selectNotice(noticeNo);
	    model.addAttribute("notice", notice);
	    return "/admin/noticeEdit";
	}
	
	// 공지사항 수정 처리
	@PostMapping("admin/noticeEdit/{id}")
	@ResponseBody
	public HashMap<String, Boolean> noticeEdit(@PathVariable("id") int noticeNo, @ModelAttribute Notice notice) {
	    notice.setNoticeNo(noticeNo);
	    boolean success = aService.updateNotice(notice);
	    HashMap<String, Boolean> map = new HashMap<>();
	    map.put("success", success);
	    return map;
	}
	
	// 공지사항 삭제
	@PostMapping("admin/deleteNotice")
	@ResponseBody
	public HashMap<String, Boolean> deleteNotice(@RequestParam("noticeNo") int noticeNo) {
	    boolean success = aService.deleteNotice(noticeNo);
	    HashMap<String, Boolean> map = new HashMap<>();
	    map.put("success", success);
	    return map;
	}	
		

	@GetMapping("/admin/getCommNoByReplyNo")
	@ResponseBody
	public Map<String, Object> getCommNoByReplyNo(@RequestParam("replyNo") int replyNo) {
	    Map<String, Object> response = new HashMap<>();
	    
	    try {
	        // 댓글이 속한 게시글 번호 조회
	        int commNo = aService.getCommNoByReplyNo(replyNo);
	        
	        if (commNo > 0) {
	            response.put("success", true);
	            response.put("commNo", commNo);
	        } else {
	            response.put("success", false);
	            response.put("message", "댓글 정보를 찾을 수 없습니다.");
	        }
	    } catch (Exception e) {
	        response.put("success", false);
	        response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
	    }
	    
	    return response;
	}
	
	@GetMapping("admin/Stats")
	public String Stats(Model model) {
		ArrayList<DashBoard> userCount = aService.userCount();
		ArrayList<DashBoard> dailyUserCount = aService.dailyUserCount();
		System.out.println("dailyUserCount=========" + dailyUserCount);
		System.out.println("userCount======" + userCount);
		model.addAttribute("userCount", userCount).addAttribute("dailyUserCount", dailyUserCount);
		return "admin/Stats";
	}

}