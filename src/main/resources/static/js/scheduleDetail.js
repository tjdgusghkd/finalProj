let trip;
window.onload = () => {
	trip = window.trip;
	console.log("trip : " + trip.cityName);
}

		function initMap(){
			// 'tripTitle'요소에서 'data-city'속성 가져오기
			const cityElement = document.getElementById("tripTitle");
			const cityName = cityElement ? cityElement.getAttribute("data-city") : null;
			
			console.log("선택한 도시 : " + cityName);
			
			// 기본 위치(서울)
			let defaultLocation = { lat: 37.5665, lng:126.9780};
			
			// Google Maps Geocoder 생성 (도시명을 좌표로 변환)
		    const geocoder = new google.maps.Geocoder();

		    if (cityName) {
		        geocoder.geocode({ address: cityName }, function(results, status) {
		            if (status == "OK") {
		                defaultLocation = results[0].geometry.location;
		                console.log("📌 변환된 좌표:", defaultLocation);

		                // Google 지도 생성
		                const map = new google.maps.Map(document.getElementById("map"), {
		                    center: defaultLocation,
		                    zoom: 12
		                });

		                // 마커 추가
		                new google.maps.Marker({
		                    position: defaultLocation,
		                    map: map,
		                    title: cityName
		                });

		            } else {
		                console.error("📍 도시 좌표 변환 실패:", status);
		            }
		        });
		    } else {
		        console.warn("🚨 도시 정보 없음! 기본 위치 사용");

		        // Google 지도 기본값 (서울)
		        const map = new google.maps.Map(document.getElementById("map"), {
		            center: defaultLocation,
		            zoom: 12
		        });
		    }
		}		
		
		// Google Places API를 사용하여 placeId로 장소 이름 가져오는 함수
		function getPlaceNameById(placeId, callback) {
		    const service = new google.maps.places.PlacesService(document.createElement('div'));

		    const request = {
		        placeId: placeId,
		        fields: ['name'] // 장소 이름만 요청
		    };

		    service.getDetails(request, (place, status) => {
		        if (status === google.maps.places.PlacesServiceStatus.OK && place && place.name) {
		            callback(place.name); // 성공 시 이름 전달
		        } else {
		            callback("이름 불러오기 실패"); // 실패 시 대체 텍스트
		        }
		    });
		}
		
		// 날짜 범위(start ~ end) 기준으로 일정 리스트를 생성하는 함수
		function generateDateList(start, end) {
		    const dateList = document.getElementById("dateList"); // HTML에 있는 일정 출력 영역
		    dateList.innerHTML = ""; // 기존에 있던 일정들 초기화

		    let startDate = new Date(start); // 시작 날짜 객체
		    let endDate = new Date(end);     // 종료 날짜 객체

		    // 시작일부터 종료일까지 반복
		    while (startDate <= endDate) {
		        const formattedDate = startDate.toISOString().split("T")[0]; // 날짜 포맷: yyyy-MM-dd

		        // 이 날짜에 해당하는 detail 데이터 필터링
		        const dailyDetails = window.detailList.filter(d => d.selectDate === formattedDate);

		        // 해당 날짜의 placeId 목록 (중복 제거, null 제거)
		        const placeIds = [...new Set(dailyDetails.map(d => d.placeId).filter(Boolean))];

		        // 해당 날짜에 저장된 메모가 있으면 가져오기 (없으면 기본 메시지)
		        const memo = dailyDetails.find(d => d.content)?.content || "메모 없음";

		        // 일정 하나를 표시할 컨테이너 요소 생성
		        const dateContainer = document.createElement("div");
		        dateContainer.classList.add("date-container"); // 날짜별 일정 묶음

		        // 날짜 표시 요소
		        const planDate = document.createElement("div");
		        planDate.classList.add("plan-date");
		        planDate.textContent = formattedDate;

		        // 일정 내용 표시 영역
		        const dateItem = document.createElement("div");
		        dateItem.classList.add("date-item");

		        // 메모 / 장소 내용을 담는 div
		        const contentDiv = document.createElement("div");
		        contentDiv.classList.add("plan-content");

				// ✅ 메모 DOM 요소 먼저 만들어두기
				const memoEl = document.createElement("div");
				memoEl.innerHTML = `<strong> 메모:</strong> ${memo}`;
				contentDiv.appendChild(memoEl); // 메모는 마지막에 붙이기

				// ✅ 장소 먼저 삽입 (메모 위에)
				if (placeIds.length > 0) {
				    placeIds.forEach(placeId => {
				        getPlaceNameById(placeId, (placeName) => {
				            const placeEl = document.createElement("div");
				            placeEl.innerHTML = `<strong> 장소:</strong> ${placeName}`;
				            // 📍 장소를 메모 위에 삽입
				            contentDiv.insertBefore(placeEl, memoEl);
				        });
				    });
				} else {
				    const placeEl = document.createElement("div");
				    placeEl.innerHTML = `<strong> 장소:</strong> 없음`;
				    contentDiv.insertBefore(placeEl, memoEl); // 장소 없을 때도 메모 위로
				}

		        // 모든 요소 조립해서 DOM에 추가
		        dateItem.appendChild(contentDiv);
		        dateContainer.appendChild(planDate);
		        dateContainer.appendChild(dateItem);
		        dateList.appendChild(dateContainer);

		        // 다음 날짜로 이동
		        startDate.setDate(startDate.getDate() + 1);
		    }
		}
		
		document.addEventListener("DOMContentLoaded", function () {
			    const trip = window.trip;
			
			    if (trip && trip.startDate && trip.endDate) {
			        const start = new Date(trip.startDate);
			        const end = new Date(trip.endDate);
			        generateDateList(start, end);
			    }
			});
		
		
			
			

        // 메뉴바 선택시 일정 목록으로 페이지 이동
        document.addEventListener("DOMContentLoaded", function(){
        	const menuBtn = document.getElementById("menuBtn");
        	console.log(menuBtn);
        	console.log(menuBtn);
        	
        	if(menuBtn){
        		menuBtn.addEventListener("click", function(){
        		console.log("일정 목록 페이지로 이동")
        		window.location.href = "/schedule/list";
        	});
        }
        });

		// 말풍선 메뉴 토글
		document.getElementById("editBtn").addEventListener("click", function (e) {
		    e.stopPropagation(); // 다른 클릭 막기
		    const menu = document.getElementById("editMenu");
		    menu.style.display = menu.style.display === "block" ? "none" : "block";
		});

		// 바깥 클릭하면 닫기
		window.addEventListener("click", function () {
		    const menu = document.getElementById("editMenu");
		    if (menu) menu.style.display = "none";
		});

		// 메뉴 항목 클릭 시 실행할 함수
		function onEdit(tripNo) {
			window.location.href = `/schedule/edit/${tripNo}`;
		}

		function onDelete(tripNo) {
		    if (confirm("정말 삭제하시겠습니까?")) {
		        window.location.href = `/schedule/delete/${tripNo}`;
			}
		}
		
		// 패널 닫기 버튼 기능 추가
		document.addEventListener("DOMContentLoaded", function () {
		    const closeButton = document.querySelector(".close-btn");
		    if (closeButton) {
		        closeButton.addEventListener("click", function () {
		            document.getElementById("side-panel").style.display = "none";
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
	