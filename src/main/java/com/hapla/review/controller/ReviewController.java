package com.hapla.review.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.hapla.comm.model.vo.Comm;
import com.hapla.common.PageInfo;
import com.hapla.common.Pagination;
import com.hapla.exception.Exception;
import com.hapla.review.model.service.ReviewService;
import com.hapla.review.model.vo.Review;
import com.hapla.users.model.vo.Users;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/review")
public class ReviewController {
	private final ReviewService reviewService;
	
	@GetMapping("main")
	public String toMain() {
		return "review/main";
	}
		
	@GetMapping("list")
	public String selectList(
	        @RequestParam(value = "page", defaultValue = "1") int currentPage,
	        @RequestParam(value = "search", required = false) String search, // ✅ 검색어 추가
	        Model model, HttpServletRequest request) {

	    int boardLimit = 4;
	    int listCount;

	    // ✅ 검색어가 있을 경우 필터링된 게시글 개수 조회
	    if (search != null && !search.trim().isEmpty()) {
	        listCount = reviewService.getSearchListCount(search); // ✅ 검색 결과 개수
	    } else {
	        listCount = reviewService.getListCount(); // ✅ 전체 게시글 개수
	    }

	    PageInfo pi = Pagination.getPageInfo(currentPage, listCount, boardLimit);

	    ArrayList<Review> list;

	    // ✅ 검색어 여부에 따라 쿼리 다르게 실행
	    if (search != null && !search.trim().isEmpty()) {
	        list = reviewService.searchReviewList(pi, search); // ✅ 검색 리스트 불러오기
	    } else {
	        list = reviewService.selectReviewList(pi); // ✅ 전체 리스트 불러오기
	    }

	    // ✅ 검색어 유지하여 View로 전달
	    model.addAttribute("list", list)
	         .addAttribute("pi", pi)
	         .addAttribute("search", search)
	         .addAttribute("listCount", listCount)
	         .addAttribute("loc", request.getRequestURI() + "?search=" + (search != null ? search : ""));

	    return "review/list";
	}


	
	@GetMapping("write")
	public String writeReview() {
		return "review/write";
	}

	@GetMapping("/{id}/{page}")
    public ModelAndView selectReview(@PathVariable("id") int reviewNo, 
                                     @PathVariable("page") int page,
                                     HttpSession session, ModelAndView mv) throws Exception {

        Users loginUser = (Users) session.getAttribute("loginUser");
        Integer userNo = (loginUser != null) ? loginUser.getUserNo() : null;

        Review r = reviewService.selectReview(reviewNo);
        
        int updatedLikeCount = reviewService.getLikeCount(reviewNo);
        r.setLikes(updatedLikeCount);
        boolean isLiked = (userNo != null) && reviewService.checkUserLike(userNo, reviewNo) > 0;

        if(r != null) {
	    	List<String> imageUrls = null;
	        String thumbnail = null;
	
	        if (r.getImageUrls() != null && !r.getImageUrls().isEmpty()) {
	            String[] imageUrlsArray = r.getImageUrls().split(",");
//	            System.out.println("imageUrlsArray : " + Arrays.toString(imageUrlsArray));
	
	            if (imageUrlsArray.length > 0) {
	                thumbnail = imageUrlsArray[0]; // 첫 번째 이미지를 썸네일로 사용
	                imageUrls = Arrays.asList(imageUrlsArray).subList(1, imageUrlsArray.length); // 나머지 상세 이미지
	            }
	        }
	
	        // ModelAndView에 데이터 추가
	        mv.addObject("r", r)
	          .addObject("page", page)
	          .addObject("thumbnail", thumbnail)
	          .addObject("imageUrls", imageUrls)
	          .addObject("isLiked", isLiked)
	          .setViewName("review/detail");
	
	        return mv;
	    } else {
	    	throw new Exception("리뷰 조회 실패");
	    }
    }

	@PostMapping("insert")
	public String insertReview(@ModelAttribute Review r) {

//		System.out.println(r);

		if (r.getImageUrls() != null) {
			String img = r.getImageUrls();
			if (img.contains(",")){
				String tumbnail = img.split(",")[0];
				r.setThumnail(tumbnail);
				r.setImageUrls(img);
			}else{
				r.setThumnail(r.getImageUrls());
				r.setImageUrls(null);
			}
		}

		 // ✅ 줄바꿈 문자 (\n)를 <br> 태그로 변환하여 저장
	    if (r.getContent() != null) {
	        r.setContent(r.getContent().replace("\n", "<br>"));
	    }
		
		// 데이터 저장
		int result = reviewService.insertReview(r);
		if (result == 1) {
			return "redirect:/review/main";
		}
		throw new Exception("실패");
	}
	
	@PostMapping("delete")
    public String deleteReview(@RequestParam("reviewNo") int reviewNo, HttpServletRequest request) {
    	int result = reviewService.deleteReview(reviewNo);
    	if(result > 0) {
    		return "redirect:/review/main";
    	} else {
    		throw new Exception("실패");
    	}
    }
}
