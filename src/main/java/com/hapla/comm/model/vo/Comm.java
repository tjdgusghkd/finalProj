package com.hapla.comm.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comm {
    private int commNo;
    private int categoryNo;
    private int userNo;
    private String title;
    private String commContent;
    private int views;
    private Date createDate;
    private Date updateDate;
    private int likes;
    private String status;
    private String categoryName;
    private String name;
    private String nickname;
    private String id;
}
