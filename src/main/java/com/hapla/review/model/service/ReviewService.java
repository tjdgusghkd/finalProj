package com.hapla.review.model.service;

import java.util.ArrayList;

import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;

import com.hapla.common.PageInfo;
import com.hapla.review.model.mapper.ReviewMapper;
import com.hapla.review.model.vo.Review;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
	private final ReviewMapper mapper;
	public int getListCount() {
		return mapper.getListCount();
	}
	public ArrayList<Review> selectReviewList(PageInfo pi) {
		int offset = (pi.getCurrentPage() -1) * pi.getBoardLimit();
		RowBounds rowBounds = new RowBounds(offset, pi.getBoardLimit());
		return mapper.selectReviewList(rowBounds);
	}
	public Review selectReview(int reviewNo) {
		return mapper.selectReview(reviewNo);
	}
	public int insertReview(Review r) {
		return mapper.insertReview(r);
	}
	public int checkUserLike(int userNo, int reviewNo) {
		return mapper.checkUserLike(userNo, reviewNo);
	}
	public void removeLike(int userNo, int reviewNo) {
		mapper.deleteLike(userNo, reviewNo);
	}
	public void addLike(int userNo, int reviewNo) {
		mapper.insertLike(userNo, reviewNo);
	}
	public int getLikeCount(int reviewNo) {
		return mapper.countLikes(reviewNo);
	}
	public int getSearchListCount(String search) {
		return mapper.getSearchListCount(search);
	}

	// ✅ 검색된 리뷰 목록 조회 (페이징 포함) → 새롭게 추가됨
    public ArrayList<Review> searchReviewList(PageInfo pi, String search) {
        int offset = (pi.getCurrentPage() - 1) * pi.getBoardLimit();
        RowBounds rowBounds = new RowBounds(offset, pi.getBoardLimit());
        return mapper.searchReviewList(search, rowBounds);
    }
	public int deleteReview(int reviewNo) {
		return mapper.deleteReview(reviewNo);
	}
}
