// ✈️ 여행 일정 모달 열기
function openTripPlanModal() {
    document.getElementById("tripPlanModal").classList.remove("hidden");
}

// ❌ 여행 일정 모달 닫기
function closeTripPlanModal() {
    document.getElementById("tripPlanModal").classList.add("hidden");
}

// 🌍 도시 검색 모달 열기
function openCitySearchModal() {
    closeTripPlanModal();
    document.getElementById("citySearchModal").classList.remove("hidden");
    //filterCities();
}

// ❌ 도시 검색 모달 닫기
function closeCitySearchModal() {
    document.getElementById("citySearchModal").classList.add("hidden");
}

// 일정 목록 버튼 클릭 시 페이지 이동
document.addEventListener("DOMContentLoaded", function () {
    const planListBtn = document.getElementById("planListBtn");

    if (planListBtn) {
        planListBtn.addEventListener("click", function () {
            console.log("📋 일정 목록 페이지로 이동");
            window.location.href = "/schedule/list";
        });
    }
});

// 🌍 Google Places API 자동완성 검색 적용
function initCitySearch() {
    const input = document.getElementById("searchInput");

    // 브라우저 자동완성 끄기
    input.setAttribute("autocomplete", "off");

    const autocomplete = new google.maps.places.Autocomplete(input, {
        types: ["(cities)"], // 도시만 검색
        fields: ["name", "formatted_address", "geometry", "types"],
    });

    // 도시 선택 시 이벤트
    autocomplete.addListener("place_changed", function () {
        const place = autocomplete.getPlace();
        if (!place.geometry || !place.types) {
            console.error("도시 정보를 찾을 수 없습니다.");
            return;
        }

		
        let addressParts = place.formatted_address.split(", "); // 🔥 주소를 배열로 변환
        let cityName = addressParts[0]; // 🔥 첫 번째 요소만 저장 (지역명)
		const countryName = addressParts[addressParts.length - 1]; // 마지막 요소 = 국가명

        // 국가명 없이 지역명만 저장하여 넘기기
        selectCity({
            name: cityName,
			country: countryName,
            lat: place.geometry.location.lat(),
            lng: place.geometry.location.lng(),
        });
    });

    // 검색어 입력 이벤트 추가 (인기 도시 리스트 변경)
    input.addEventListener("input", function () {
        const searchTerm = input.value.trim();
        if (searchTerm == "") {
            displayCityList(); // 검색어가 없으면 다시 인기 도시 출력
        } else {
            filterCities(searchTerm);
        }
    });
}

// ✅ 인기 도시 리스트
const popularCities = [
    { name: "서울", country: "대한민국", lat: 37.5665, lng: 126.9780 },
    { name: "부산", country: "대한민국", lat: 35.1796, lng: 129.0756 },
    { name: "제주", country: "대한민국", lat: 33.4996, lng: 126.5312 },
    { name: "오사카", country: "일본", lat: 34.6937, lng: 135.5023 },
    { name: "도쿄", country: "일본", lat: 35.682839, lng: 139.759455 },
    { name: "방콕", country: "태국", lat: 13.7563, lng: 100.5018 },
];

// 🌆 기본 도시 리스트 출력
function displayCityList() {
    const cityList = document.getElementById("cityList");
    cityList.innerHTML = ""; // 기존 리스트 초기화

    popularCities.forEach((city) => {
        const li = createCityListItem(city);
        cityList.appendChild(li);
    });
}

// 🔍 도시 검색 기능 (검색된 결과 출력)
function filterCities(searchTerm) {
	const autocompleteService = new google.maps.places.AutocompleteService();
	    
	    autocompleteService.getPlacePredictions({
	        input: searchTerm,
	        types: ['(cities)'] // 도시만 예측
	    }, function(predictions, status) {
	        const cityList = document.getElementById("cityList");
	        cityList.innerHTML = ""; // 기존 리스트 초기화

	        if (status !== google.maps.places.PlacesServiceStatus.OK || !predictions) {
	            console.error("도시 검색 결과가 없습니다.");
	            return;
	        }

	        predictions.forEach(function(prediction) {
	            const li = createCityListItem(prediction);
	            cityList.appendChild(li);
	        });
	    });
	}

	// 🌍 도시 리스트 아이템 생성 함수 (인기도시와 검색 예측 모두 지원)
	// 인기 도시 객체: { name, country, lat, lng }
	// 예측 객체: { description, place_id }
	function createCityListItem(item) {
	    const li = document.createElement("li");
	    li.classList.add("city-item");

	    // 📍 아이콘 추가
	    const icon = document.createElement("span");
	    icon.classList.add("city-icon");
	    icon.textContent = "📍";

	    // 도시 정보 텍스트
	    const textSpan = document.createElement("span");
	    textSpan.classList.add("city-text");

	    if (item.description && item.place_id) {
	        // 검색 예측 결과
	        textSpan.textContent = item.description;
	        li.onclick = function () {
	            // place_id를 이용해 상세 정보를 가져옴
	            const service = new google.maps.places.PlacesService(document.createElement("div"));
	            service.getDetails({
	                placeId: item.place_id,
	                fields: ["name", "formatted_address", "geometry"]
	            }, function(place, status) {
	                if (status !== google.maps.places.PlacesServiceStatus.OK || !place.geometry) {
	                    console.error("도시 상세 정보를 가져오지 못했습니다.");
	                    return;
	                }
	                let addressParts = place.formatted_address.split(", ");
	                let cityName = addressParts[0];
	                const countryName = addressParts[addressParts.length - 1];
	                const city = {
	                    name: cityName,
	                    country: countryName,
	                    lat: place.geometry.location.lat(),
	                    lng: place.geometry.location.lng()
	                };
	                selectCity(city);
	            });
	        };
	    } else if (item.name && item.country) {
	        // 인기 도시 객체
	        textSpan.textContent = `${item.name}, ${item.country}`;
	        li.onclick = function () {
	            selectCity(item);
	        };
	    }

	    li.appendChild(icon);
	    li.appendChild(textSpan);

	    return li;
	}
	// 도시 선택 시 실행되는 함수 (도시 정보 저장 & 페이지 이동)
	function selectCity(city) {
	    localStorage.setItem("selectedCity", JSON.stringify(city));
	    console.log(`📍 선택한 도시: ${city.name}, ${city.country}`);
	
	    // 일정 페이지로 이동 (기본 페이지가 /schedule/scheduleCalendar 인 경우)
	    window.location.href = "/schedule/calendar";
	}
	
	// 페이지 로드 후 Google Places API 초기화 실행
	window.onload = function () {
	    initCitySearch();
	    displayCityList();
	};
