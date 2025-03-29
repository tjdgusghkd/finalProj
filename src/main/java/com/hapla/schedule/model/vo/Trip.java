package com.hapla.schedule.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trip {
	private int tripNo;
	private int userNo;
	private String title;
	private Date startDate;
	private Date endDate;
	private Date createDate;
	private String cityName;
}
