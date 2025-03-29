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
public class Report {
    private int reportNo;          
    private String reportTitle;    
    private String reportContent;  
    private int reporterNo;        
    private int violatorNo;        
    private String reportStatus;   
    private String reportCategory; 
    private int contentNo;         
    private Date createDate;       
    private String reporterNickname; 
    private String violatorNickname; 
    private int reportCount;       
}

