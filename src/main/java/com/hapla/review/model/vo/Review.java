package com.hapla.review.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Review {
	private int reviewNo;
	private int userNo;
	private String name;
	private String profile;
	private String title;
	private String content;
	private int rating;
	private int likes;
	private Date createDate;
	private Date updateDate;
	private String when;
	private String withWhom;
	private String thumnail;
	private String imageUrls;
	private String nickname;
	private String id;
}
