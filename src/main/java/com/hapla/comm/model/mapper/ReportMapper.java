package com.hapla.comm.model.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hapla.admin.model.vo.Report;
@Mapper
public interface ReportMapper {
   
   Integer checkAlreadyReported(
           @Param("reportCategory") String reportCategory, 
           @Param("contentNo") int contentNo, 
           @Param("reporterNo") int reporterNo);
   
   Integer getViolatorNo(
           @Param("reportCategory") String reportCategory, 
           @Param("contentNo") int contentNo);
   
   int insertReport(Report report);
   
   Integer getReportCount(
           @Param("reportCategory") String reportCategory, 
           @Param("contentNo") int contentNo);
   
   
   // 신고된 콘텐츠 조회
   Map<String, Object> getReportedComm(
           @Param("contentNo") int contentNo);
   
   Map<String, Object> getReportedReply(
           @Param("contentNo") int contentNo);
   
   Map<String, Object> getCommInfo(
           @Param("commNo") int commNo);
}

