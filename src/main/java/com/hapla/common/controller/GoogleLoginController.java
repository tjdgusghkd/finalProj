package com.hapla.common.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

@RestController
@RequestMapping("/google-login")
public class GoogleLoginController {

    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyGoogleToken(@RequestBody Map<String, String> requestBody) {
        // 클라이언트로부터 받은 요청 본문을 Map 형태로 처리
        String token = requestBody.get("token"); // 요청 본문에서 "token" 키에 해당하는 JWT 토큰 값 추출
        try {
            // 구글의 공개 키를 사용해 JWT 검증
            String url = "https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=" + token;
            // 구글의 토큰 검증 API 엔드포인트에 토큰을 쿼리 파라미터로 추가
            URL obj = new URL(url); // URL 객체 생성
            InputStreamReader reader = new InputStreamReader(obj.openStream());
            // URL로부터 데이터를 읽어오기 위한 스트림 생성
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            // 읽어온 JSON 데이터를 파싱하여 JsonObject로 변환

            String userId = jsonObject.get("sub").getAsString(); // 구글 사용자 고유 ID (sub 필드)
            String name = jsonObject.get("name").getAsString(); // 사용자 이름
            String pictureUrl = jsonObject.get("picture").getAsString(); // 사용자 프로필 사진 URL

            // 반환할 사용자 정보
            JsonObject response = new JsonObject(); // 응답용 JSON 객체 생성
            response.addProperty("tokenId", userId); // 사용자 ID 추가
            response.addProperty("name", name); // 이름 추가
            response.addProperty("picture", pictureUrl); // 프로필 사진 URL 추가
            System.out.println(response); // 디버깅용으로 응답 데이터 출력

            return ResponseEntity.ok(response.toString()); // 성공 시 HTTP 200 상태와 함께 JSON 문자열 반환
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to verify token");
        }
    }
}