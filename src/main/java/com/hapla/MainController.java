package com.hapla;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import com.hapla.admin.model.vo.DashBoard;
import com.hapla.users.model.service.UsersService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UsersService uService;

    @GetMapping("/")
    public String main(Model model, HttpServletRequest request, HttpSession session) {
        ArrayList<DashBoard> logList = uService.selectAllLog();
        HashMap<String, String> map = new HashMap<>();
        String visitIp = getClientIp(request);
        String visitUserAgent = request.getHeader("User-Agent");
        System.out.println("visitIp==" + visitIp);
        System.out.println("visitUserAgent" + visitUserAgent);
        
        boolean isTodayExists = false; 
        boolean isIpAgentDuplicated = false;

        Date today = new Date(System.currentTimeMillis());
        LocalDate todayLocalDate = today.toLocalDate();

        for (DashBoard log : logList) {
            Date visitDate = log.getVisitDate();
            LocalDate visitLocalDate = visitDate.toLocalDate();
//
            	if(log.getVisitIp().equals(visitIp) && log.getVisitUserAgent().equals(visitUserAgent)) {
////            if (log.getIpAddress().equals(ipAddress) && log.getUserAgent().equals(userAgent)) {
////            		isIpAgentDuplicated = true;
                if (visitLocalDate.equals(todayLocalDate)) {
                    isTodayExists = true;
                    break;
                }
            }
        }

        // 방문 기록 삽입 조건 수정
        if (!isTodayExists) { 
            System.out.println("로그 삽입됨 (새로운 방문)");
            map.put("visitIp", visitIp);
            map.put("visitUserAgent", visitUserAgent);
            uService.insertLog(map);
        }

        return "index";
    }

//    private String getClientIp(HttpServletRequest request) {
//        String ip = request.getHeader("X-Forwarded-For");
//        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("Proxy-Client-IP");
//        }
//        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("WL-Proxy-Client-IP");
//        }
//        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getRemoteAddr();
//        }
//        return ip;
//    }
 // client 실ip 가져오는 메소드
 	public static String getClientIp(HttpServletRequest request) {
 	    String clientIp = null;
 	    boolean isIpInHeader = false;
 	    
 	    List<String> headerList = new ArrayList<>();
 	    
 	    headerList.add("X-Forwarded-For");
 	    headerList.add("HTTP_CLIENT_IP");
 	    headerList.add("HTTP_X_FORWARDED_FOR");
 	    headerList.add("HTTP_X_FORWARDED");
 	    headerList.add("HTTP_FORWARDED_FOR");
 	    headerList.add("HTTP_FORWARDED");
 	    headerList.add("Proxy-Client-IP");
 	    headerList.add("WL-Proxy-Client-IP");
 	    headerList.add("HTTP_VIA");
 	    headerList.add("IPV6_ADR");
 	    
 	    for (String header : headerList) {
 	        clientIp = request.getHeader(header);
 	        if (StringUtils.hasText(clientIp) && !"unknown".equalsIgnoreCase(clientIp)) {
 	            isIpInHeader = true;
 	            break;
 	        }
 	    }
 	    if (!isIpInHeader) {
 	        clientIp = request.getRemoteAddr();
 	    }
 	    
 	    if ("0:0:0:0:0:0:0:1".equals(clientIp) || "127.0.0.1".equals(clientIp)) {
 	        InetAddress address = null;
 			try {
 				address = InetAddress.getLocalHost();
 			} catch (UnknownHostException e) {
 				e.printStackTrace();
 			}
 	        clientIp = address.getHostAddress();
 	    }
 	    System.out.println(clientIp);
 	    System.out.println(isIpInHeader);
 	    
 	    return clientIp;
 	}
}
