package com.hapla.users.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    private int userNo;
    private String name;
    private String nickname;
    private String email;
    private Date createDate;
    private String profile;
    private String isAdmin;
    private String type;
    private String tokenId;
    private String status;
    private String accessToken;
}
