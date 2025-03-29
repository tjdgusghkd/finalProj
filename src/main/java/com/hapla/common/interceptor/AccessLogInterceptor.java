package com.hapla.common.interceptor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AccessLogInterceptor implements HandlerInterceptor {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 세션에 접속 시간 저장
        request.getSession().setAttribute("accessTime", System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        try {
            // 관리자 페이지 접근 시 로깅 제외
            String requestURI = request.getRequestURI();
            if (requestURI.startsWith("/admin/") || requestURI.contains("accessStats") || requestURI.contains("createTestData")) {
                return;
            }
            
            // 사용자 정보 가져오기
            HttpSession session = request.getSession();
            Long userId = null;
            
            // 세션에서 userId 가져오기 (로그인 사용자)
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj != null) {
                if (userIdObj instanceof Long) {
                    userId = (Long) userIdObj;
                } else if (userIdObj instanceof Integer) {
                    userId = ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof String) {
                    try {
                        userId = Long.parseLong((String) userIdObj);
                    } catch (NumberFormatException e) {
                        // userId가 숫자 형식이 아닌 경우 처리
                        userId = null;
                    }
                }
            }
            
            // 비로그인 사용자는 IP 주소와 User-Agent를 조합하여 식별
            String visitorId = null;
            if (userId == null) {
                String ipAddress = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");
                // IP와 User-Agent의 해시값을 사용하여 비로그인 사용자 식별
                visitorId = generateVisitorId(ipAddress, userAgent);
            }
            
            // 페이지 ID 가져오기 (URI 사용)
            String pageId = request.getRequestURI();
            
            // 세션 지속 시간 계산
            Long accessTime = (Long) session.getAttribute("accessTime");
            int sessionDuration = 0;
            if (accessTime != null) {
                sessionDuration = (int) ((System.currentTimeMillis() - accessTime) / 1000);
            }
            
            // IP 주소 가져오기
            String ipAddress = getClientIpAddress(request);
            
            // 사용자 에이전트 가져오기
            String userAgent = request.getHeader("User-Agent");
            if (userAgent != null && userAgent.length() > 500) {
                userAgent = userAgent.substring(0, 500);
            }
            
            // 리퍼러 가져오기
            String referrer = request.getHeader("referer");
            if (referrer != null && referrer.length() > 500) {
                referrer = referrer.substring(0, 500);
            }
            
            // 접속 로그 저장
            saveAccessLog(userId, visitorId, pageId, sessionDuration, ipAddress, userAgent, referrer);
        } catch (Exception e) {
            // 로깅 중 오류가 발생해도 사용자 경험에 영향을 주지 않도록 예외 처리
            System.err.println("접속 로그 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String generateVisitorId(String ipAddress, String userAgent) {
        // IP 주소와 User-Agent를 조합하여 해시 생성
        String combined = ipAddress + "|" + (userAgent != null ? userAgent : "");
        return UUID.nameUUIDFromBytes(combined.getBytes()).toString();
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
    
    private void saveAccessLog(Long userId, String visitorId, String pageId, 
                              int sessionDuration, String ipAddress, String userAgent, String referrer) {
        try {
            String sql = "INSERT INTO access_log (id, user_id, visitor_id, page_id, access_time, " +
                        "session_duration, ip_address, user_agent, referrer) " +
                        "VALUES (access_log_seq.NEXTVAL, ?, ?, ?, SYSDATE, ?, ?, ?, ?)";
            
            jdbcTemplate.update(sql, 
                userId, 
                visitorId, 
                pageId, 
                sessionDuration, 
                ipAddress, 
                userAgent, 
                referrer
            );
        } catch (Exception e) {
            System.err.println("접속 로그 DB 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

