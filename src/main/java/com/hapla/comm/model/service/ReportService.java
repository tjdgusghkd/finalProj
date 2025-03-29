package com.hapla.comm.model.service;

import java.sql.Clob;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hapla.admin.model.vo.Report;
import com.hapla.comm.model.mapper.ReportMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final ReportMapper reportMapper;
    
    public boolean checkAlreadyReported(String reportCategory, int contentNo, int reporterNo) {
        try {
            Integer count = reportMapper.checkAlreadyReported(reportCategory, contentNo, reporterNo);
            return count != null && count > 0;
        } catch (Exception e) {
            // 오류 발생 시 기본적으로 false 반환 (신고하지 않은 것으로 처리)
            return false;
        }
    }
    
    public int getViolatorNo(String reportCategory, int contentNo) {
        try {
            
            // 카테고리 값 검증
            if (reportCategory == null || (!reportCategory.equals("C") && !reportCategory.equals("R"))) {
                throw new IllegalArgumentException("유효하지 않은 신고 카테고리입니다.");
            }
            
            Integer violatorNo = reportMapper.getViolatorNo(reportCategory, contentNo);
        
            // 피신고자 번호가 없는 경우 기본값 반환 대신 예외 발생
            if (violatorNo == null || violatorNo == 0) {
                throw new IllegalArgumentException("피신고자를 찾을 수 없습니다.");
            }
            return violatorNo;
        } catch (Exception e) {
            throw new RuntimeException("피신고자 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    public boolean insertReport(Report report) {
        try {
            return reportMapper.insertReport(report) > 0;
        } catch (Exception e) {
            throw new RuntimeException("신고 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    public int getReportCount(String reportCategory, int contentNo) {
        try {
            Integer count = reportMapper.getReportCount(reportCategory, contentNo);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
 // 신고된 콘텐츠 조회 
    public Map<String, Object> getReportedContent(String category, int contentNo) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 카테고리 유효성 검사
            if (category == null || (!category.equals("C") && !category.equals("R"))) {
                result.put("error", "유효하지 않은 카테고리입니다.");
                return result;
            }
            
            // 콘텐츠 번호 유효성 검사
            if (contentNo <= 0) {
                result.put("error", "유효하지 않은 콘텐츠 번호입니다.");
                return result;
            }
            
            try {
                Map<String, Object> contentData = null;
                
                if ("C".equals(category)) {
                    // 게시글 조회 시도
                    contentData = reportMapper.getReportedComm(contentNo);
                } else if ("R".equals(category)) {
                    // 댓글 조회 시도
                    contentData = reportMapper.getReportedReply(contentNo);
                    
                    // 댓글이 속한 게시글 정보도 함께 가져오기
                    if (contentData != null && contentData.containsKey("commNo") && contentData.get("commNo") != null) {
                        try {
                            int commNo = Integer.parseInt(contentData.get("commNo").toString());
                            if (commNo > 0) {
                                Map<String, Object> commInfo = reportMapper.getCommInfo(commNo);
                                
                                if (commInfo != null && !commInfo.isEmpty()) {
                                    contentData.put("commTitle", commInfo.get("title"));
                                } else {
                                    contentData.put("commTitle", "원본 게시글 정보 없음");
                                }
                            } else {
                                contentData.put("commTitle", "원본 게시글 정보 없음");
                            }
                        } catch (Exception e) {
                            contentData.put("commTitle", "원본 게시글 정보 조회 오류");
                        }
                    } else {
                        if (contentData != null) {
                            contentData.put("commTitle", "원본 게시글 정보 없음");
                        }
                    }
                }
                
                if (contentData == null || contentData.isEmpty()) {
                    result.put("error", "해당 " + (category.equals("C") ? "게시글" : "댓글") + "을 찾을 수 없습니다.");
                    return result;
                }
                
                // 결과 데이터 정제 - CLOB 객체 및 직렬화 불가능한 객체 처리
                Map<String, Object> cleanData = new HashMap<>();
                for (Map.Entry<String, Object> entry : contentData.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    
                    if (value != null) {
                        // CLOB 객체 처리
                        if (value instanceof Clob) {
                            try {
                                Clob clob = (Clob) value;
                                int length = (int) clob.length();
                                if (length > 0) {
                                    String clobText = clob.getSubString(1, length);
                                    cleanData.put(key, clobText);
                                } else {
                                    cleanData.put(key, "");
                                }
                            } catch (Exception e) {
                                cleanData.put(key, "내용을 불러올 수 없습니다.");
                            }
                        }
                        // Oracle JDBC 관련 객체 처리
                        else if (value.getClass().getName().contains("oracle.")) {
                            try {
                                // toString() 메소드 호출
                                String stringValue = value.toString();
                                cleanData.put(key, stringValue);
                            } catch (Exception e) {
                                cleanData.put(key, "내용을 불러올 수 없습니다.");
                            }
                        } else {
                            cleanData.put(key, value);
                        }
                    } else {
                        cleanData.put(key, null);
                    }
                }
                
                return cleanData;
            } catch (Exception e) {
                result.put("error", "콘텐츠 조회 중 데이터베이스 오류가 발생했습니다: " + e.getMessage());
                return result;
            }
        } catch (Exception e) {
            result.put("error", "콘텐츠 조회 중 오류가 발생했습니다: " + e.getMessage());
            return result;
        }
    }

    // null 값 처리를 위한 유틸리티 메소드 추가
//    private void ensureNotNull(Map<String, Object> map, String key, Object defaultValue) {
//        if (!map.containsKey(key) || map.get(key) == null) {
//            map.put(key, defaultValue);
//        }
//    }
    
    // 관리자 페이지에 등록 (3회 이상 신고된 경우)
//    public void registerToAdmin(String reportCategory, int contentNo) {
//        log.info("신고 횟수 확인: 카테고리={}, 콘텐츠번호={}", reportCategory, contentNo);
//    }
}

