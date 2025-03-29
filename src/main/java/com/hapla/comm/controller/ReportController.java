package com.hapla.comm.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hapla.comm.model.service.ReportService;
import com.hapla.admin.model.vo.Report;
import com.hapla.users.model.vo.Users;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {
   
   private final ReportService reportService;
   
   @PostMapping("/submit")
   @ResponseBody
   public ResponseEntity<Map<String, Object>> submitReport(
           @RequestParam(value = "reportTitle") String reportTitle,
           @RequestParam(value = "reportContent") String reportContent,
           @RequestParam(value = "reportCategory") String reportCategory,
           @RequestParam(value = "contentNo") int contentNo,
           HttpSession session) {
       
       
       Users loginUser = (Users) session.getAttribute("loginUser");
       Map<String, Object> response = new HashMap<>();
       
       if (loginUser == null) {
           response.put("success", false);
           response.put("message", "로그인이 필요합니다.");
           return ResponseEntity.badRequest().body(response);
       }
       
       int reporterNo = loginUser.getUserNo();
       
       try {
           // 이미 신고했는지 확인
           boolean alreadyReported = reportService.checkAlreadyReported(reportCategory, contentNo, reporterNo);
           if (alreadyReported) {
               response.put("success", false);
               response.put("message", "이미 신고한 " + (reportCategory.equals("C") ? "게시글" : "댓글") + "입니다.");
               return ResponseEntity.badRequest().body(response);
           }
           
           try {
               // 피신고자 번호 가져오기
               int violatorNo = reportService.getViolatorNo(reportCategory, contentNo);
               
               // 신고 정보 저장
               Report report = new Report();
               report.setReportTitle(reportTitle);
               report.setReportContent(reportContent);
               report.setReporterNo(reporterNo);
               report.setViolatorNo(violatorNo);
               report.setReportStatus("W"); // 대기중
               report.setReportCategory(reportCategory);
               report.setContentNo(contentNo);
               
               boolean result = reportService.insertReport(report);
               
               // 신고 횟수 확인
               int reportCount = reportService.getReportCount(reportCategory, contentNo);
               
               // 신고 횟수가 3회 이상인 경우에만 관리자 페이지에 등록 
//               if (reportCount >= 3) {
//                   reportService.registerToAdmin(reportCategory, contentNo);
//               }
               
               response.put("success", result);
               response.put("reportCount", reportCount);
               response.put("message", "신고가 접수되었습니다.");
               
               return ResponseEntity.ok(response);
           } catch (IllegalArgumentException e) {
               // 피신고자를 찾을 수 없는 경우
               response.put("success", false);
               response.put("message", "신고할 수 없는 콘텐츠입니다. 이미 삭제되었거나 존재하지 않습니다.");
               return ResponseEntity.badRequest().body(response);
           }
       } catch (Exception e) {
           response.put("success", false);
           response.put("message", "신고 처리 중 오류가 발생했습니다: " + e.getMessage());
           return ResponseEntity.internalServerError().body(response);
       }
   }
   
   @GetMapping("/check")
   @ResponseBody
   public ResponseEntity<Map<String, Object>> checkReported(
           @RequestParam(value = "reportCategory") String reportCategory,
           @RequestParam(value = "contentNo") int contentNo,
           HttpSession session) {
       
       
       Map<String, Object> response = new HashMap<>();
       
       try {
           Users loginUser = (Users) session.getAttribute("loginUser");
           
           if (loginUser == null) {
               response.put("isReported", false);
               return ResponseEntity.ok(response);
           }
           
           int reporterNo = loginUser.getUserNo();
           boolean isReported = reportService.checkAlreadyReported(reportCategory, contentNo, reporterNo);
           
           response.put("isReported", isReported);
           return ResponseEntity.ok(response);
       } catch (Exception e) {
           response.put("error", "신고 상태 확인 중 오류가 발생했습니다: " + e.getMessage());
           response.put("isReported", false); // 오류 발생 시 기본값으로 false 반환
           return ResponseEntity.ok(response); // 클라이언트에서 처리할 수 있도록 200 OK 반환
       }
   }
}

