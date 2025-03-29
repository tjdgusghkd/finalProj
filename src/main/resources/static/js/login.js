let loginImage = ''; // 로그인한 사용자의 프로필 이미지 URL을 저장
let loginName = ''; // 로그인한 사용자의 닉네임(이름)을 저장
let loginType = ''; // 로그인 타입 (K: 카카오, G: 구글)을 저장
let tokenId = ''; // 로그인한 사용자의 고유 ID를 저장

// ✅ 카카오 SDK 초기화
Kakao.init("04cff7eb62f268a2bb506c81bf1de17b"); // 카카오 JavaScript 키를 사용해 SDK 초기화

// ✅ 카카오 로그인 버튼 클릭 시 실행
document.getElementById("kakao-login-btn").addEventListener("click", function () {
    Kakao.Auth.login({
        scope: "profile_nickname, profile_image", // 요청할 사용자 정보 범위 (닉네임, 프로필 이미지)
        success: function (authObj) {
            console.log("✅ 로그인 성공! 액세스 토큰:", authObj.access_token); // 로그인 성공 시 액세스 토큰 출력
            document.getElementById('accessToken').value = authObj.access_token;
            // ✅ 액세스 토큰을 이용하여 사용자 정보 가져오기
            fetchUserInfo(authObj.access_token); // 사용자 정보를 가져오는 함수 호출
        },
        fail: function (err) {
            console.error("❌ 로그인 실패", err); // 로그인 실패 시 에러 출력
        },
    });
});

// ✅ 사용자 정보 가져와서 모달 업데이트
function fetchUserInfo(accessToken) {
    Kakao.API.request({
        url: "/v2/user/me", // 카카오 API에서 사용자 정보를 가져오는 엔드포인트
        success: function (response) {
            console.log("✅ 사용자 정보:", response); // 가져온 사용자 정보 출력
            console.log(response.id); // 사용자 ID 출력

            loginImage = response.properties.profile_image; // 프로필 이미지 URL 저장
            loginName = response.properties.nickname; // 닉네임 저장
            loginType = "K"; // 로그인 타입을 'K'(카카오)로 설정
            tokenId = response.id; // 사용자 고유 ID 저장

            checkUser(tokenId, loginType); // 사용자 확인 함수 호출
        },
        fail: function (error) {
            console.error("❌ 사용자 정보 가져오기 실패", error); // 정보 가져오기 실패 시 에러 출력
        },
    });
}

// ✅ 모달창 업데이트 (프로필 이미지 & 닉네임)
function updateModal(loginImage, loginName, loginType, tokenId) {
    const modal = document.getElementById("joinModal"); // 회원가입 모달 요소
    const profileImage = document.getElementById("loginImage"); // 프로필 이미지 요소
    const nicknameInput = document.getElementById("loginName"); // 닉네임 입력 필드
    const type = document.getElementById("loginType"); // 로그인 타입 입력 필드
    const id = document.getElementById('tokenId'); // 사용자 ID 입력 필드
    const profile = document.getElementById('profile'); // 프로필 이미지 URL 입력 필드

    // 🔹 사용자 정보 적용
    profileImage.src = loginImage; // 프로필 이미지 설정
    nicknameInput.value = loginName || "사용자"; // 닉네임 설정 (없으면 "사용자"로 기본값)
    type.value = loginType; // 로그인 타입 설정
    id.value = tokenId; // 사용자 ID 설정
    profile.value = loginImage; // 프로필 이미지 URL 설정

    // 🔹 모달창 표시
    document.getElementById('loginModal').classList.add('hidden'); // 로그인 모달 숨김
    modal.classList.remove("hidden"); // 회원가입 모달 표시
}

// ✅ 구글 로그인 버튼 설정
google.accounts.id.initialize({
    client_id: '157461561471-a5mi8bhvlq4rt4ci11j6ep6h8ftd6tmq.apps.googleusercontent.com', // 구글 클라이언트 ID
    callback: handleCredentialResponse // 로그인 성공 시 호출할 콜백 함수
});
google.accounts.id.renderButton(
    document.getElementById("google-login-btn"), // 구글 로그인 버튼 요소
    {theme: "outline", size: "large", width: "100%"} // 버튼 스타일 설정
);

// ✅ 구글 로그인 콜백 함수
function handleCredentialResponse(response) {
    console.log("Encoded JWT ID token: " + response.credential); // JWT 토큰 출력
    fetchUserProfile(response.credential); // 사용자 프로필 정보를 가져오는 함수 호출
}

// ✅ 구글 사용자 프로필 정보 가져오기
function fetchUserProfile(token) {
    fetch('http://192.168.40.15:8080/google-login/verify-token', { // 서버 엔드포인트로 토큰 검증 요청
        method: 'POST',
        headers: {
            'Content-Type': 'application/json' // 요청 헤더 설정
        },
        body: JSON.stringify({ token: token }) // JWT 토큰을 JSON 형태로 전송
    })
        .then(response => response.json()) // 응답을 JSON으로 변환
        .then(data => {
            loginImage = data.picture; // 프로필 이미지 URL 저장
            loginName = data.name; // 사용자 이름 저장
            loginType = "G"; // 로그인 타입을 'G'(구글)로 설정
            tokenId = data.tokenId; // 사용자 ID 저장
            checkUser(tokenId, loginType); // 사용자 확인 함수 호출
        })
        .catch(error => {
            console.error('Error fetching user profile:', error); // 프로필 정보 가져오기 실패 시 에러 출력
        });
}

// ✅ 사용자 확인 함수
const checkUser = (id, type) => {
    fetch("/checkUser", { // 서버에 사용자 확인 요청
        method: "POST",
        headers: {
            "Content-Type": "application/json", // 요청 헤더 설정
        },
        body: JSON.stringify({
            tokenId: id, // 사용자 ID
            type: type, // 로그인 타입
            accessToken: document.getElementById('accessToken').value
        }), // 요청 본문에 JSON 데이터 포함
    })
        .then((response) => response.json()) // 응답을 JSON으로 변환
        .then((data) => {
            console.log(data); // 서버 응답 출력
            if (data.checkResult == '0') { // 사용자가 신규 사용자일 경우
                updateModal(loginImage, loginName, loginType, tokenId); // 모달창 업데이트
            } else { // 이미 등록된 사용자일 경우
                document.getElementById('loginModal').classList.add('hidden'); // 로그인 모달 숨김

                location.reload(); // 페이지 새로고침
            }
        })
}