package com.hapla.comm.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hapla.comm.model.service.ReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@RequestMapping("/report-viewer")
public class ReportViewerController {

   private final ReportService reportService;
   
   @GetMapping("/content")
   @ResponseBody
   public ResponseEntity<Map<String, Object>> getReportedContent(
           @RequestParam(value = "category", required = false) String category,
           @RequestParam(value = "contentNo", required = false, defaultValue = "0") Integer contentNo) {

       Map<String, Object> response = new HashMap<>();
       
       try {
           // 파라미터 유효성 검사
           if (category == null || category.trim().isEmpty()) {
               response.put("error", "카테고리 파라미터가 누락되었습니다.");
               return ResponseEntity.ok(response);
           }
           
           if (contentNo == null || contentNo <= 0) { 
               response.put("error", "유효한 콘텐츠 번호가 필요합니다. (전달된 값: " + contentNo + ")");
               return ResponseEntity.ok(response);
           }
           
           // 서비스 호출하여 콘텐츠 조회
           Map<String, Object> content = reportService.getReportedContent(category, contentNo);
           
           // 응답에 모든 필드 복사 (깊은 복사로 변경)
           for (Map.Entry<String, Object> entry : content.entrySet()) {
               // CLOB 또는 직렬화 불가능한 객체는 문자열로 변환
               if (entry.getValue() != null) {
                   try {
                       // Oracle CLOB 객체 등 직렬화 불가능한 객체 처리
                       if (entry.getValue().getClass().getName().contains("oracle.sql") || 
                           entry.getValue().getClass().getName().contains("oracle.jdbc")) {
                           response.put(entry.getKey(), entry.getValue().toString());
                       } else {
                           response.put(entry.getKey(), entry.getValue());
                       }
                   } catch (Exception e) {
                       // 직렬화 불가능한 객체는 toString()으로 변환
                       response.put(entry.getKey(), String.valueOf(entry.getValue()));
                   }
               } else {
                   response.put(entry.getKey(), null);
               }
           }
           
           return ResponseEntity.ok(response);
       } catch (Exception e) {
           response.put("error", "콘텐츠 조회 중 오류가 발생했습니다: " + e.getMessage());
           return ResponseEntity.ok(response);
       }
   }
}

