package com.hapla.comm.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Reply {
	private int replyNo;
	private int userNo;
	private int commNo;
	private String name;
	private String replyContent;
	private Date createDate;
	private Date updateDate;
	private String nickname;
	private String title;
	private String id;
	private String commTitle;
	private String status;
	}
