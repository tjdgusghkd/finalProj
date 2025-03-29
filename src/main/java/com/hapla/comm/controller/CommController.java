package com.hapla.comm.controller;

import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.hapla.admin.model.service.AdminService;
import com.hapla.admin.model.vo.Notice;
import com.hapla.comm.model.service.CommService;
import com.hapla.comm.model.vo.Comm;
import com.hapla.comm.model.vo.Reply;
import com.hapla.common.PageInfo;
import com.hapla.common.Pagination;
import com.hapla.exception.Exception;
import com.hapla.users.model.vo.Users;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comm")
public class CommController {
    private final CommService commService;
    private final AdminService adminService;
    
//    @GetMapping("list")
//	public String selectList(@RequestParam(value="page", defaultValue="1") int currentPage, Model model, HttpServletRequest request) {
//
//    	int listCount = commService.getListCount(1);
//		
//		PageInfo pi = Pagination.getPageInfo(currentPage, listCount, 5);
//		ArrayList<Comm> list = commService.selectCommList(pi, 1);
//		
//		model.addAttribute("list", list).addAttribute("pi", pi);
//		model.addAttribute("loc", request.getRequestURI());
//		// getRequestURI() : /board/list
//		// getRequestURL() : http://localhost:8080/board/list 
//		
//		return "comm/list";
//	}
    
//    @GetMapping("list")
//    public String selectList(
//            @RequestParam(value = "page", defaultValue = "1") int currentPage,
//            @RequestParam(value = "search", required = false) String search,
//            @RequestParam(value = "category", required = false, defaultValue = "-") String category,
//            Model model,
//            HttpServletRequest request) {
//
//        int listCount;
//        PageInfo pi;
//        ArrayList<Comm> list;
//
//        if ((search != null && !search.trim().isEmpty()) || !"-".equals(category)) {
//            listCount = commService.getSearchListCount(search, category);
//        } else {
//            listCount = commService.getListCount(1, category);
//        }
//        
//
//        pi = Pagination.getPageInfo(currentPage, listCount, 5);
//        list = commService.selectCommList(pi, 1, search, category);
//
//        model.addAttribute("list", list).addAttribute("pi", pi);
//        model.addAttribute("search", search); // 검색어 유지
//        model.addAttribute("category", category); // 카테고리 유지
//        model.addAttribute("loc", request.getRequestURI());
//
//        return "comm/list";
//    }
    
//    @GetMapping("list")
//    public String selectList(
//            @RequestParam(value = "page", defaultValue = "1") int currentPage,
//            @RequestParam(value = "search", required = false) String search,
//            @RequestParam(value = "category", required = false, defaultValue = "0") int category,
//            Model model,
//            HttpServletRequest request) {
//
//        int listCount;
//        PageInfo pi;
//        ArrayList<Comm> list;
//
////        // ✅ category가 "-"이면 전체 게시글을 조회하도록 NULL 처리
////        if ("-".equals(category)) {
////            category = 0;
////        }
//
//        // ✅ 검색어가 있을 경우 검색된 게시글 개수 조회
//        if (search != null && !search.trim().isEmpty()) {
//            listCount = commService.getSearchListCount(search, category);
//        } else {
//            listCount = commService.getListCount(1, category);
//        }
//
//        pi = Pagination.getPageInfo(currentPage, listCount, 5);
//        list = commService.selectCommList(pi, 1, search, category);
//        
//        // 공지사항 목록 가져오기 (최신 3개)
//        ArrayList<Notice> noticeList = adminService.selectRecent();
//
//        model.addAttribute("list", list)
//             .addAttribute("pi", pi)
//             .addAttribute("search", search) // 검색어 유지
//             .addAttribute("category", category) // 카테고리 유지
//             .addAttribute("loc", request.getRequestURI())
//        	 .addAttribute("noticeList", noticeList);
//
//        return "comm/list";
//    }
    
    @GetMapping("list")
    public String selectList(
            @RequestParam(value = "page", defaultValue = "1") int currentPage,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "category", required = false, defaultValue = "0") int category,
            Model model,
            HttpServletRequest request) {

        int listCount;
        PageInfo pi;
        ArrayList<Comm> list;

        // ✅ 검색어가 있을 경우 검색된 게시글 개수 조회
        if (search != null && !search.trim().isEmpty()) {
            listCount = commService.getSearchListCount(search, category);
        } else {
            listCount = commService.getListCount(1, category);
        }

        pi = Pagination.getPageInfo(currentPage, listCount, 5);
        list = commService.selectCommList(pi, 1, search, category);

        // 공지사항 목록 가져오기 (최신 3개)
        ArrayList<Notice> noticeList = adminService.selectRecent();

        // ✅ 검색 및 카테고리 정보를 URL에 포함하여 전달하기 위한 URL 구성
        String searchQuery = (search != null && !search.trim().isEmpty()) ? "&search=" + search : "";
        String categoryQuery = (category != 0) ? "&category=" + category : "";
        String loc = request.getRequestURI() + "?page=" + currentPage + searchQuery + categoryQuery;

        model.addAttribute("list", list)
             .addAttribute("pi", pi)
             .addAttribute("search", search)          // 검색어 유지
             .addAttribute("category", category)      // 카테고리 유지
             .addAttribute("loc", loc)                // 페이징 URL 유지
             .addAttribute("noticeList", noticeList);

        return "comm/list";
    }


 // 공지사항 상세 조회 (일반 사용자용)
    @GetMapping("/notice/{noticeNo}/{page}")
    public ModelAndView viewNotice(@PathVariable("noticeNo") int noticeNo, 
                                  @PathVariable("page") int page,
                                  HttpSession session, 
                                  ModelAndView mv) {
        
        Notice notice = adminService.selectNotice(noticeNo);
        
        if (notice == null) {
            throw new Exception("공지사항 조회에 실패했습니다.");
        }
        
        mv.addObject("notice", notice)
          .addObject("page", page)
          .setViewName("comm/noticeDetail"); // 공지사항 전용 상세 페이지
        
        return mv;
    }
    
//    @GetMapping("list")
//    public String selectList(
//        @RequestParam(value="page", defaultValue="1") int currentPage,
//        @RequestParam(value="condition", required=false) String condition,  // 검색 조건
//        @RequestParam(value="search", required=false) String search,  // 검색어
//        Model model, HttpServletRequest request) {
//
//        int listCount;
//        ArrayList<Comm> list;
//        
//        if (search != null && !search.trim().isEmpty()) { 
//            // 🔹 검색이 있는 경우 → 검색 결과만 가져오기
//            listCount = commService.getSearchListCount(condition, search, 1);
//            PageInfo pi = Pagination.getPageInfo(currentPage, listCount, 5);
//            list = commService.searchCommList(pi, condition, search, 1);
//            model.addAttribute("condition", condition);
//            model.addAttribute("search", search);
//        } else { 
//            // 🔹 검색이 없는 경우 → 전체 리스트 가져오기
//            listCount = commService.getListCount(1);
//            PageInfo pi = Pagination.getPageInfo(currentPage, listCount, 5);
//            list = commService.selectCommList(pi, 1);
//        }
//
//        // 모델에 추가
//        model.addAttribute("list", list).addAttribute("pi", Pagination.getPageInfo(currentPage, listCount, 5));
//        model.addAttribute("loc", request.getRequestURI());
//
//        return "comm/list";  // ✅ 동일한 뷰 파일 사용
//    }
    
    @GetMapping("write")
	public String writeComm() {
		return "comm/write";
	}
    
    @PostMapping("insert")
    public String insertComm(@ModelAttribute Comm c, HttpSession session) {
        Users loginUser = (Users) session.getAttribute("loginUser");

        if (loginUser == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        // 로그인한 사용자의 userNo를 Comm 객체에 설정
        c.setUserNo(loginUser.getUserNo());
        
        if (c.getCommContent() != null) {
	        c.setCommContent(c.getCommContent().replace("\n", "<br>"));
	    }

        int result = commService.insertComm(c);
        
        if (result > 0) {
            return "redirect:/comm/list";
        } else {
            throw new RuntimeException("게시글 작성을 실패하였습니다.");
        }
    }
   
//    @GetMapping("/{id}/{page}")
//    public ModelAndView selectComm(@PathVariable("id") int commNo, @PathVariable("page") int page, HttpSession session, ModelAndView mv) {
//    	Users loginUser = (Users)session.getAttribute("loginUser");
//    	String name = null;
//    	if(loginUser != null) {
//    		name = loginUser.getName();
//    	}
//    	
//    	Comm c = commService.selectComm(commNo, name);
////    	ArrayList<Reply> list = bService.selectReplyList(bId);
//    	
//    	if(c != null) {
////    		mv.addObject("list", list);
//			mv.addObject("c", c).addObject("page", page).setViewName("comm/detail");
//			return mv;
//    	} else {
//    		throw new RuntimeException("게시글 상세조회를 실패하였습니다.");
//    	}
//    }
    
    @GetMapping("/{id}/{page}")
    public ModelAndView selectComm(@PathVariable("id") int commNo, 
                                   @PathVariable("page") int page,
                                   HttpSession session, 
                                   ModelAndView mv) {
        Users loginUser = (Users) session.getAttribute("loginUser");
        Integer userNo = (loginUser != null) ? loginUser.getUserNo() : null;
        
        
        Comm c = commService.selectComm(commNo, userNo);
        ArrayList<Reply> list = commService.selectReplyList(commNo);
        
        int updatedLikeCount = commService.getLikeCount(commNo);
        c.setLikes(updatedLikeCount);
        boolean isLiked = (userNo != null) && commService.checkUserLike(userNo, commNo) > 0;
        
        System.out.println("로그인된 사용자 번호: " + userNo);
        System.out.println("게시글 번호: " + commNo);
        System.out.println("좋아요 상태: " + isLiked);

        if (c == null) {
        	throw new Exception("실패");
        }

        mv.addObject("c", c).addObject("page", page).setViewName("comm/detail"); // ✅ 게시글 상세 페이지로 이동
        mv.addObject("list", list);
        mv.addObject("isLiked", isLiked);

        return mv;
    }
    
//    @GetMapping("rinsert")
//    @ResponseBody
//    public String insertReply(@ModelAttribute Reply r, HttpServletResponse response) {
//    	int result = commService.insertReply(r);
//    	ArrayList<Reply> list = commService.selectReplyList(r.getCommNo());
//    	
//    	response.setContentType("application/json; charset=UTF-8");
//    	
//    	ObjectMapper om = new ObjectMapper();
//    	
//    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//    	om.setDateFormat(sdf);
//    	
//    	String strJson = null;
//    	
//    	try {
//			strJson = om.writeValueAsString(list);
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}
//    	return strJson;
//    }
    
    @PostMapping("/updForm")
    public String updateForm(@RequestParam("commNo") int commNo,@RequestParam("userNo") int userNo, @RequestParam("page") int page, Model model) {
    	Comm c = commService.selectComm(commNo, userNo);
    	model.addAttribute("c", c).addAttribute("page", page);
    	return "comm/edit";
    }
    
    @PostMapping("update")
    public String updateComm(@ModelAttribute Comm c, @RequestParam("page") int page) {
    	int result = commService.updateComm(c);
    	
    	if(result > 0) {
    		return String.format("redirect:/comm/%d/%d", c.getCommNo(), page);
    	} else {
    		throw new Exception("실패");
    	}
    }
    
    @PostMapping("delete")
    public String deleteComm(@RequestParam("commNo") int commNo, HttpServletRequest request) {
    	int result = commService.deleteComm(commNo);
    	if(result > 0) {
    		return "redirect:/comm/list";
    	} else {
    		throw new Exception("실패");
    	}
    }
}
