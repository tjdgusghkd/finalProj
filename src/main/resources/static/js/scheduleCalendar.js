	// 일정 페이지가 로드될 때 실행되는 함수
    function initMap() {
        // ✅ localStorage에서 선택한 도시 정보 가져오기
        const storedCity = localStorage.getItem("selectedCity");

        // 기본 지도 위치 설정
        let mapOptions = {
            center: { lat: 37.5665, lng: 126.9780 }, // 서울 기본 위치
            zoom: 12
        };

        // 저장된 도시 정보가 있으면 지도 위치 변경
        if (storedCity) {
            const city = JSON.parse(storedCity);
            mapOptions.center = { lat: city.lat, lng: city.lng }; // 선택한 도시 좌표로 이동
            console.log(`📍 지도 위치 변경: ${city.name}, ${city.country}`);
        }

        // Google 지도 생성
        new google.maps.Map(document.getElementById('map'), mapOptions);
    }

		// 캘린더 모달 자동 열기
		document.addEventListener("DOMContentLoaded", function () {
		    openCalendarModal();
		    initDatePickers();
		});

		function openCalendarModal() {
		    document.getElementById("calendarModal").style.display = "block";
		}

		function closeCalendarModal() {
		    document.getElementById("calendarModal").style.display = "none";
		} 

		// 날짜 선택 라이브러리 (Flatpickr) 적용
		function initDatePickers() {
		    flatpickr("#startDate", {
		        dateFormat: "Y-m-d",
		        minDate: "today",
		        static: true,	// 캘린더 항상 열려 있도록 설정
		       	locale: "ko",	// 한국어 설정
		        onClose: function(selectedDates) {
		            if (selectedDates.length > 0) {
		                flatpickr("#endDate", {
		                    dateFormat: "Y-m-d",
		                    minDate: selectedDates[0],	//(당일 선택 가능)
		                    // minDate: selectedDates[0].fp_incr(1), // 출발일 다음 날부터 선택 가능
		                    static: true,
		                    locale: "ko"
		                });
		            }
		        }
		    });

		    flatpickr("#endDate", {
		        dateFormat: "Y-m-d",
		        minDate: "today",
		        static: true,
		        locale: "ko"
		    });
		}

		function saveDates() {
		    const startDate = document.getElementById("startDate").value;
		    const endDate = document.getElementById("endDate").value;
		    const storedCity = localStorage.getItem("selectedCity"); // 기존에 선택한 도시 정보 가져오기
		    const form = document.querySelector('form');
		    
		    let cityName = '';
			let tripData = '';
			
			if(storedCity){
				const cityData = JSON.parse(storedCity); // 문자열을 객체로 반환
				cityName = cityData.name;
				
				console.log(cityName);
			}
			
			
		    document.querySelector('input[name=endDate]').value = endDate;
		    document.querySelector('input[name=startDate]').value = startDate;
		    document.querySelector('input[name=cityName]').value = cityName;
		    
		    if (startDate && endDate && storedCity) {
		        tripData = {
		        		city: JSON.parse(storedCity), // 기존 도시 정보 유지
		        		startDate: startDate,
		        		endDate: endDate
		        };
		        console.log(tripData);
		        console.log(cityName);
		        localStorage.setItem("tripData", JSON.stringify(tripData)); // 🔥 날짜까지 저장
		        form.action = "/schedule/schedule"; // 🔥 일정 페이지로 이동
		        form.submit();
		    } else {
		        alert("출발 날짜와 도착 날짜를 선택하세요!");
		    }
		}