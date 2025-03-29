package com.hapla.users.controller;

import com.hapla.exception.Exception;
import com.hapla.users.model.service.UsersService;
import com.hapla.users.model.vo.Users;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@SessionAttributes({"loginUser", "token"})
public class UsersController {

    private final UsersService usersService;

    @PostMapping("/checkUser")
    @ResponseBody
    public String checkUser(@RequestBody Users user, HttpSession session) {
        int checkResult = usersService.checkUser(user);
        Users u = usersService.login(user);
        JSONObject json = new JSONObject();
        json.put("checkResult", checkResult);
        json.put("user", u);

        session.setAttribute("loginUser", u);
        session.setAttribute("token", user.getAccessToken());

        System.out.println(session.getAttribute("loginUser"));

        return json.toString();
    }

    @PostMapping("/join")
    @ResponseBody
    public String join(@RequestBody Users user) {
        System.out.println(user);
        int result = usersService.join(user);
        if (result == 1) {
            JSONObject json = new JSONObject();
            json.put("success", true);
            return json.toString();
        }
        throw new Exception("회원가입에 실패하였습니다.");
    }

    @GetMapping("/users/logout")
    public String logout(SessionStatus session, Model model) {
        String accessToken = (String) model.getAttribute("token");

        if (accessToken == null) {
            // 토큰이 없다면 로그인 페이지로 리다이렉트
            return "redirect:/users/login";
        }

        // 카카오 로그아웃 처리
        boolean result = kakaoLogout(accessToken);
        if (result) {
            System.out.println("카카오 로그아웃 성공");
        } else {
            System.out.println("카카오 로그아웃 실패");
        }

        // 세션 종료
        session.setComplete();
        return "redirect:/";  // 로그아웃 후 다시 로그인 페이지로 리다이렉트
    }

    private boolean kakaoLogout(String accessToken) {
        final String KAKAO_LOGOUT_URL = "https://kapi.kakao.com/v1/user/logout";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken); // 액세스 토큰을 Authorization 헤더에 설정
        headers.setContentType(MediaType.APPLICATION_JSON); // 요청의 Content-Type을 JSON으로 설정

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            // POST 요청을 보내고 응답 받기
            ResponseEntity<String> response = restTemplate.exchange(KAKAO_LOGOUT_URL, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return true;
            } else {
                System.out.println("카카오 로그아웃 실패");
                return false;
            }
        } catch (Exception e) {
            System.out.println("카카오 로그아웃 오류: " + e.getMessage());
            return false;
        }
    }

    // 환경 변수에서 R2 설정 가져오기
    @Value("${r2.access-key}")
    private String r2AccessKey;

    @Value("${r2.secret-key}")
    private String r2SecretKey;

    @Value("${r2.bucket-name}")
    private String r2BucketName;

    // R2 엔드포인트와 공개 URL 설정
    private static final String R2_ENDPOINT = "https://33a78afb4d99dee34672fc2e9b3a305d.r2.cloudflarestorage.com/";
    private static final String R2_PUBLIC_URL = "https://pub-04c6fc4d78f440d78896dc3d2f55f689.r2.dev/";

    private S3Client s3Client; // final 제거

    @PostConstruct
    public void initS3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(r2AccessKey, r2SecretKey);
        this.s3Client = S3Client.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(java.net.URI.create(R2_ENDPOINT))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadProfileImage(@RequestParam("image") MultipartFile image) {
        if (image.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("파일이 비어 있습니다.");
        }

        try {
            // 파일 이름 생성
            String originalFilename = image.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String renameFileName = new SimpleDateFormat("yyyyMMddHHmmssSSS")
                    .format(new Date()) + (int)(Math.random() * 100000) + extension;

            // R2에 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(r2BucketName)
                    .key(renameFileName)
                    .build();

            s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(image.getInputStream(), image.getSize()));

            // 클라이언트에 반환할 공개 URL
            String imagePath = R2_PUBLIC_URL + renameFileName;
            Map<String, String> response = new HashMap<>();
            response.put("imagePath", imagePath);
            System.out.println("파일 업로드 성공: " + imagePath);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/checkNickname")
    @ResponseBody
    public String checkNickname(@RequestParam("nickname") String nickname) {
        int result = usersService.checkNickname(nickname);
        JSONObject json = new JSONObject();
        if (result > 0) {
            json.put("result", "fail");
        }else{
            json.put("result", "success");
        }
        return json.toString();
    }

    @PostMapping("/updateUser")
    @ResponseBody
    public String updateUser(@RequestBody Users user, Model model) {
        int result = usersService.updateUser(user);
        JSONObject json = new JSONObject();
        if (result > 0) {
            json.put("result", true);
            Users u = usersService.login(user);
            model.addAttribute("loginUser", u);
        }else{
            json.put("result", false);
        }
        System.out.println(json.toString());
        return json.toString();
    }

    @GetMapping("/deleteUser")
    public String deleteUser(Model model) {
        int no = ((Users) model.getAttribute("loginUser")).getUserNo();
        int result = usersService.deleteUser(no);
        if (result > 0){
            return "redirect:/users/logout";
        }
        throw new Exception("실패");
    }
}
