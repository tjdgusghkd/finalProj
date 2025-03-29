package com.hapla.admin.model.vo;

import java.util.Date;



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

public class Notice {
	
	private int noticeNo;
	private String title;
	private String content;
	private int views;
	private Date createDate;
	private Date updateDate;
	private String status;
	private String isImportant;
}
