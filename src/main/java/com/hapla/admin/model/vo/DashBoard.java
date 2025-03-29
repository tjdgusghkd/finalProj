package com.hapla.admin.model.vo;

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
public class DashBoard {
	private Date createAt;
	private int logsNo;
	private Date visitDate;
	private int id;
	private String visitIp;
	private String visitUserAgent;
	private String weekDayName;
	private int count;
}