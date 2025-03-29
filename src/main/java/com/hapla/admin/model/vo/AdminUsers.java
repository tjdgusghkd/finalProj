package com.hapla.admin.model.vo;

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
public class AdminUsers {
	private int userNo;
	private String name;
	private String nickname;
	private String email;
	private String createDate;
	private String profile;
	private String type;
	private String status;
	private String isAdmin;
}
