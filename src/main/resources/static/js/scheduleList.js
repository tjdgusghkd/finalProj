// ✅ Google 지도 초기화 함수
function initMap(lat = 37.5665, lng = 126.9780) {

	new google.maps.Map(document.getElementById('map'), {
		center: { lat: lat, lng: lng },
		zoom: 10
	});
}

// 메뉴바 선택시 
document.addEventListener("DOMContentLoaded", function() {
	const menuBtn = document.getElementById("menuBtn");
	console.log(menuBtn);


	if (menuBtn) {
		menuBtn.addEventListener("click", function() {
			console.log("일정 목록 페이지로 이동")
			window.location.href = "/schedule/schedule";
		});
	}
});




// 사이드 패널 열기 / 닫기 기능
for (const button of document.querySelectorAll('.panel-open-btn')) {
	button.addEventListener('click', () => {
		document.getElementById('side-panel').classList.add('active');
	});
}


document.querySelector('.close-btn').addEventListener('click', () => {
	document.getElementById('side-panel').classList.remove('active');
});

// 일정 내용 박스 클릭시 일정 내용으로 페이지 이동
document.addEventListener("DOMContentLoaded", function() {
    const containers = document.querySelectorAll(".date-container");
    for (let i = 0; i < containers.length; i++) {
        containers[i].addEventListener("click", function() {
            const tripNo = this.getAttribute("data-id");
            console.log("클릭된 tripNo:", tripNo);
            if (tripNo) {
                window.location.href = '/schedule/detail/' + tripNo;
            }
        });
    }
});

