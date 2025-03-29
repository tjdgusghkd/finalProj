package com.hapla.review.model.mapper;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import com.hapla.review.model.vo.Review;

@Mapper
public interface ReviewMapper {

	int getListCount();

	ArrayList<Review> selectReviewList(RowBounds rowBounds);

	Review selectReview(int reviewNo);

	int insertReview(Review r);

	int checkUserLike(@Param("userNo") int userNo, @Param("reviewNo") int reviewNo);

	void insertLike(@Param("userNo") int userNo, @Param("reviewNo") int reviewNo);

	void deleteLike(@Param("userNo") int userNo, @Param("reviewNo") int reviewNo);

	int countLikes(int reviewNo);

	int getSearchListCount(String search);

	ArrayList<Review> searchReviewList(@Param("search") String search, RowBounds rowBounds);

	int deleteReview(int reviewNo);

}
