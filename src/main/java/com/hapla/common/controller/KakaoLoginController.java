package com.hapla.common.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth")
public class KakaoLoginController {

    // application.properties 또는 application.yml에서 설정된 Kakao API 클라이언트 ID와 리디렉트 URI 주입
    @Value("${kakao.client.id}")
    private String clientId;

    @Value("${kakao.redirect.uri}")
    private String redirectUri;

    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> kakaoCallback(@RequestParam("code") String code) {
        // 1️⃣ 액세스 토큰 가져오기
        String accessToken = getAccessToken(code);

        // 2️⃣ 사용자 정보 가져오기
        String userInfo = getUserInfo(accessToken);

        // 3️⃣ JSON 응답 생성
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("userInfo", userInfo);

        return ResponseEntity.ok(response);
    }

    private String getAccessToken(String code) {
        // 카카오 토큰 요청 URL
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        // 요청 바디 생성 (application/x-www-form-urlencoded 형식)
        String body = "grant_type=authorization_code" + "&client_id=" + clientId + "&redirect_uri=" + redirectUri + "&code=" + code;

        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        // 요청 엔터티 생성
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        // 카카오 API로 POST 요청을 보내 액세스 토큰을 받아옴
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);

        try {
            return extractAccessToken(response.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("액세스 토큰 파싱 오류", e);
        }
    }

    private String extractAccessToken(String responseBody) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();
    }

    private String getUserInfo(String accessToken) {
        // 사용자 정보 요청 URL
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        // HTTP 요청 헤더 설정 (Bearer 인증 방식 사용)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        // 요청 엔터티 생성
        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        // GET 요청을 보내 사용자 정보를 받아옴
        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }
}