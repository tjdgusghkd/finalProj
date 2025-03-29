let trip;
let p = 0; // 패널 인덱스
const sidePanel = [
    document.getElementById('memo-panel'),
    document.getElementById('stay-panel')
];
window.onload = () => {
	trip = window.trip;
	console.log("trip : " + trip.cityName);
}



		function initMap(){
			// 'tripTitle'요소에서 'data-city'속성 가져오기
			const cityElement = document.getElementById("tripTitle");
			const cityName = cityElement ? cityElement.getAttribute("data-city") : null;
			
			// 기본 위치(서울)
			let defaultLocation = { lat: 37.5665, lng:126.9780};
			
			// Google Maps Geocoder 생성 (도시명을 좌표로 변환)
		    const geocoder = new google.maps.Geocoder();

		    if (cityName) {
		        geocoder.geocode({ address: cityName }, function(results, status) {
		            if (status == "OK") {
		                defaultLocation = results[0].geometry.location;

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
		        fields: ['name']
		    };

		    service.getDetails(request, function(place, status) {
		        if (status == google.maps.places.PlacesServiceStatus.OK && place) {
		            callback(place.name);
		        } else {
		            callback("이름 불러오기 실패");
		        }
		    });
		}
		
		document.addEventListener("DOMContentLoaded", function () {
		    document.querySelectorAll(".place-name").forEach(el => {
		        const placeId = el.dataset.placeId;
		        getPlaceNameById(placeId, function (placeName) {
		            el.textContent = placeName || "이름 불러오기 실패";
		        });
		    });
		});

		
		// ✅ 일정 리스트 생성 함수
		function generateDateList(start, end) {
		    const dateList = document.getElementById("dateList");
		    dateList.innerHTML = "";

		    let startDate = new Date(start);
		    let endDate = new Date(end);

		    while (startDate <= endDate) {
		        const formattedDate = startDate.toISOString().split("T")[0];
		        const dailyDetails = window.detailList.filter(d => d.selectDate === formattedDate);

		        const dateContainer = document.createElement("div");
		        dateContainer.classList.add("date-container");

		        const planDate = document.createElement("div");
		        planDate.classList.add("plan-date");
		        planDate.textContent = formattedDate;
		        dateContainer.appendChild(planDate);

		        // 날짜에 detail이 없더라도 1개의 date-item은 만들기
		        const dateItem = document.createElement("div");
		        dateItem.classList.add("date-item");

		        // detail이 있을 경우 detailNo 사용 / 없을 경우 기본값
		        //const detailNo = dailyDetails.length > 0 ? dailyDetails[0].detailNo : `new-${formattedDate}`;
				const detailNo = dailyDetails.length > 0 ? dailyDetails[0].detailNo : "";
				
		        const hiddenInput = document.createElement("input");
		        hiddenInput.type = "hidden";
		        hiddenInput.classList.add("detailNo");
		        hiddenInput.value = detailNo;
		        dateItem.appendChild(hiddenInput);

		        const dateInput = document.createElement("input");
		        dateInput.type = "hidden";
		        dateInput.classList.add("selectDate");
		        dateInput.value = formattedDate;
		        dateItem.appendChild(dateInput);

		        const addDetail = document.createElement("div");
		        addDetail.classList.add("addDetail");

		        const addMemo = document.createElement("div");
		        addMemo.classList.add("addMemo");

		        // detail이 있는 경우 데이터 렌더링
		        if (dailyDetails.length > 0) {
		            const placeIds = toArray(window.placeMap[dailyDetails[0].detailNo]);
		            placeIds.forEach(placeId => {
		                getPlaceNameById(placeId, (placeName) => {
		                    const placeEl = document.createElement("div");
		                    placeEl.classList.add("place-item");
		                    placeEl.setAttribute("data-place-id", placeId);
		                    placeEl.innerHTML = `
		                        <span class="place-name">${placeName}</span>
		                        <input type="hidden" value="${placeId}"/>
		                        <button class="remove-btn" onclick="removePlace(this)">X</button>
		                    `;
		                    addDetail.appendChild(placeEl);
		                });
		            });

		            const memos = toArray(window.memoMap[dailyDetails[0].detailNo]);
		            memos.forEach(content => {
		                const memoEl = document.createElement("div");
		                memoEl.classList.add("memo-item");
		                memoEl.innerHTML = `
		                    <span class="memo-text">${content}</span>
		                    <button class="remove-btn" onclick="removeMemo(this)">X</button>
		                `;
		                addMemo.appendChild(memoEl);
		            });
		        }

		        dateItem.appendChild(addDetail);
		        dateItem.appendChild(addMemo);

		        const template = document.getElementById("controls-template");
		        if (template) {
		            const clone = template.content.cloneNode(true);
		            dateItem.appendChild(clone);
		        }

		        dateContainer.appendChild(dateItem);
		        dateList.appendChild(dateContainer);

		        startDate.setDate(startDate.getDate() + 1);
		    }
		}

		function toArray(value) {
		    if (Array.isArray(value)) return value;
		    if (value === undefined || value === null) return [];
		    return [value];
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
        	
        	if(menuBtn){
        		menuBtn.addEventListener("click", function(){
        		console.log("일정 목록 페이지로 이동")
        		window.location.href = "/schedule/list";
        	});
        }
        });

		// +추가 버튼 클릭시 control-btns 보이게 처리
		function toggleControls(addButton) {
		    // 해당 .date-item을 찾기
		    const dateItem = addButton.closest('.date-item');

		    // control-btns와 control-add 찾기
		    const controlBtns = dateItem.querySelector('.control-btns');
		    const controlAdd = dateItem.querySelector('.control-add');

		    // control-btns 보이게하고 control-add 숨김
		    controlBtns.hidden = false;
		    controlAdd.hidden = true;
		}


		// 사이드 패널 열기 함수
		function openSidePanel(panelType) {

		    const memoPanel = document.getElementById('memo-panel');
		    const stayPanel = document.getElementById('stay-panel');

		    // 모든 하위 패널 숨기기
		    memoPanel.style.display = 'none';
		    stayPanel.style.display = 'none';

		    // panelType에 따라 해당 패널만 보이게 설정
		    if (panelType == 'memo') {
		        memoPanel.style.display = 'block';
		        p = 0;
		    } else if (panelType == 'stay') {
		        stayPanel.style.display = 'block';
		        p = 1;
		    }
		    displayPlaceList(p, trip.cityName);
		}

		// Close 버튼 클릭 시, +추가 버튼으로 돌아가게 처리
		function closeControls(event) {
		    const dateItem = event.target.closest('.date-item');  // 해당 날짜 항목 찾기
		    const controlBtns = dateItem.querySelector('.control-btns');
		    const controlAdd = dateItem.querySelector('.control-add');
		    const sidePanels = document.querySelectorAll(".side-panel")

		    // ✅ 모든 사이드 패널을 닫기
		    sidePanels.forEach(panel => {
		        panel.style.display = "none"; // ✅ 패널이 정상적으로 선택되었는지 확인
		    });

		    // control-btns 숨기고 control-add 보이게 하기
		    controlBtns.hidden = true;
		    controlAdd.hidden = false;
		    sidePanels.style.display = "none";
		}


		const input = document.getElementById("searchInput");

		// 🌍 Google Places API 자동완성 장소 검색 적용
		function initPlaceSearch() {
		    console.log("initPlaceSearch 실행됨!");

		    // 브라우저 자동완성 끄기
		    input.setAttribute("autocomplete", "off");

		    const autocomplete = new google.maps.places.Autocomplete(input, {
		        types: ['establishment'] // 장소(업체)만 검색, 지역(주소) 정보는 제외
		    });

		    // ✅ 장소 선택 시 이벤트
		    autocomplete.addEventListener("place_changed", function () {
		        const place = autocomplete.getPlace();
		        if (!place.geometry) {
		            /*	        if (!place.geometry || !place.formatted_address) {*/
		            console.error("장소 정보를 찾을 수 없습니다.");
		            return;
		        }
		        selectPlace({
		            name: place.name,
		            placeId: place.place_id,
		            lat: place.geometry.location.lat(),
		            lng: place.geometry.location.lng(),
		        });
		    });
		}

		// 검색어 입력 이벤트 (인기 장소 리스트 또는 검색 결과 변경)
		input.addEventListener("input", function () {
		    const searchTerm = input.value.trim();
		    if (input && input.value.trim() == "") {
		        displayPlaceList(p, trip.cityName); // 검색어가 없으면 인기 장소 출력
		    } else {
		        filterPlaces(searchTerm);
		    }
		});


		// 🌆 기본 장소 리스트 출력
		function displayPlaceList(p) {
		    const city = trip?.cityName || "서울";
		    const geocoder = new google.maps.Geocoder();

		    geocoder.geocode({ address: city }, function (results, status) {
		        if (status === "OK" && results[0]) {
		            const location = results[0].geometry.location; // ✅ 올바른 좌표 추출
		            const service = new google.maps.places.PlacesService(document.createElement('div'));

		            const request = {
		                location: location,
		                radius: 3000, // 또는 rankBy: google.maps.places.RankBy.PROMINENCE
		                type: ['establishment']
		            };

		            service.nearbySearch(request, function (results, status) {
		                if (status === google.maps.places.PlacesServiceStatus.OK) {
		                    const resultsList = document.getElementById("search-results");
		                    resultsList.innerHTML = "";

		                    results.forEach(place => {
		                        const li = document.createElement("li");
		                        li.classList.add("place-item");
		                        li.innerHTML = `<span class="place-icon">📍</span> <span class="place-text">${place.name}</span>`;
		                        resultsList.appendChild(li);
		                    });
		                } else {
		                    console.error("❌ nearbySearch 실패:", status);
		                }
		            });
		        } else {
		            console.error("❌ 도시 지오코딩 실패:", status);
		        }
		    });
		}



		// 🔍 장소 검색 기능 (검색 예측 결과 출력)
		function filterPlaces(searchTerm) {
		    const autocompleteService = new google.maps.places.AutocompleteService();

		    autocompleteService.getPlacePredictions({
		        input: searchTerm,
//		        types: ['museum', 'park', 'restaurant', 'lodging', 'tourist_attraction']
		        types: ['establishment']
		    }, function (predictions, status) {
		        const resultsList = document.getElementById("search-results");
		        resultsList.innerHTML = "";

		        if (status !== google.maps.places.PlacesServiceStatus.OK || !predictions) {
		            console.error("장소 검색 결과가 없습니다.");
		            return;
		        }

		        predictions.forEach(function (prediction) {
		            const li = createPlaceListItem(prediction);
		            resultsList.appendChild(li);
		        });
		    });
		}


		// 🌍 장소 리스트 아이템 생성 함수 (인기 장소와 검색 예측 모두 지원)
		// 인기 장소 객체: { name, address, lat, lng }
		// 예측 객체: { description, place_id }
		function createPlaceListItem(item) {
		    const li = document.createElement("li");
		    li.classList.add("place-item");

		    // 📍 아이콘 추가
		    const icon = document.createElement("span");
		    icon.classList.add("place-icon");
		    icon.textContent = "📍";

		    // 장소 정보 텍스트
		    const textSpan = document.createElement("span");
		    textSpan.classList.add("place-text");

		    // ✨ 1. 검색 예측 결과 (Autocomplete)
		    if (item.description && item.place_id) {
		        textSpan.textContent = item.description;
		        li.onclick = function () {
		            const service = new google.maps.places.PlacesService(document.createElement("div"));
		            service.getDetails({
		                placeId: item.place_id,
		                fields: ["name", "geometry", "place_id"]
		            }, function (place, status) {
		                if (status !== google.maps.places.PlacesServiceStatus.OK || !place.geometry) return;

		                selectPlace({
		                    name: place.name,
		                    placeId: place.place_id,
		                    lat: place.geometry.location.lat(),
		                    lng: place.geometry.location.lng()
		                });
		            });
		        };

		    // ✨ 2. 기본 인기 장소 (nearbySearch → 직접 만든 객체)
		    } else if (item.name && item.placeId) {
		        textSpan.textContent = `${item.name}${item.address ? ', ' + item.address : ''}`;
		        li.onclick = function () {
		            selectPlace(item); // 이건 바로 객체 넘겨도 돼
		        };
		    }

		    li.appendChild(icon);
		    li.appendChild(textSpan);
		    return li;
		}

		let places = [];
		let placeMap = new Map();
		let selDate, activeDateItem;
		// 선택한 장소 사이드바 일정('addDetail)에 추가하기
		function selectPlace(place) {
		    console.log("선택한 장소:", place);

		    const activeDateItem = document.querySelector(".date-item.active");
		    if (!activeDateItem) {
		        alert("날짜를 먼저 선택해주세요!");
		        return;
		    }

		    const selDateInput = activeDateItem.querySelector('.selectDate');
		    if (!selDateInput) {
		        console.error("❌ selectDate input not found in date item");
		        return;
		    }

		    const selDate = selDateInput.value;

		    let places = placeMap.get(selDate) || [];

		    if (!places.includes(place.placeId)) {
		        places.push(place.placeId);
		        placeMap.set(selDate, places);

		        let addDetail = activeDateItem.querySelector(".addDetail");
		        if (!addDetail) {
		            addDetail = document.createElement("div");
		            addDetail.classList.add("addDetail");
		            activeDateItem.appendChild(addDetail);
		        }

		        const placeItem = document.createElement("div");
		        placeItem.classList.add("place-item");
		        placeItem.setAttribute("data-place-id", place.placeId);
		        placeItem.innerHTML = `
		            <span class="place-name">${place.name}</span>
		            <input type="hidden" value="${place.placeId}"/>
		            <button class="remove-btn" onclick="removePlace(this)">X</button>
		        `;

		        addDetail.appendChild(placeItem);
		    }
		}


		// 장소 삭제 기능 추가
		function removePlace(button) {
		    const placeItem = button.closest(".place-item");
		    const placeId = placeItem.querySelector("input[type=hidden]").value;

		    const dateItem = button.closest(".date-item");
		    const selDate = dateItem.querySelector(".selectDate").value;

		    if (placeMap.has(selDate)) {
		        let places = placeMap.get(selDate);
		        places = places.filter(id => id !== placeId);
		        placeMap.set(selDate, places);
		    }

		    placeItem.remove();
		}


		document.getElementById("dateList").addEventListener("click", function (e) {
		    const dateItem = e.target.closest(".date-item");
		    if (dateItem) {
		        document.querySelectorAll(".date-item").forEach(el => el.classList.remove("active"));
		        dateItem.classList.add("active");
		    }
		});

		let memos = [];
		let memoMap = new Map();
		function saveMemo() {
		    activeDateItem = document.querySelector(".date-item.active"); // 현재 활성화된 날짜
		    //selDate = activeDateItem.previousElementSibling.getElementsByClassName('selectDate')[0].value;
			selDate = activeDateItem.querySelector('.selectDate').value;
			// 메모 입력값 가져오기
		    const memoText = document.getElementById("memo-text").value.trim();

		    if (memoText == "") {
		        alert("메모를 입력하세요!");
		        return;
		    }

		    if (memoMap.has(selDate)){
		        let me = memoMap.get(selDate);
		        me.push(memoText);
		        memoMap.set(selDate, me);
		    }else{
		        memos = [];
		        memos.push(memoText);
		        memoMap.set(selDate, memos);
		    }

		    // ✅ 선택한 날짜의 `addMemo` 요소 찾기 (없으면 생성)
		    let addMemo = activeDateItem.querySelector(".addMemo");
		    if (!addMemo) {
		        addMemo = document.createElement("div");
		        addMemo.classList.add("addMemo");
		        activeDateItem.appendChild(addMemo);
		    }

		    // ✅ 메모 요소 추가 (HTML 요소 생성)
		    const memoItem = document.createElement("div");
		    memoItem.classList.add("memo-item");
		    memoItem.innerHTML = `
		        <span class="memo-text">${memoText}</span>
		        <button class="remove-btn" onclick="removeMemo(this)">X</button>
		    `;

		    // `addMemo`에 메모 추가
		    addMemo.appendChild(memoItem);

		    // 메모 입력창 초기화
		    document.getElementById("memo-text").value = "";
		}

		//saveDetail(memoText, null);
		document.getElementById("memo-text").value = "";

		// ✅ 메모 삭제 기능 추가
		function removeMemo(button) {
		    const memoItem = button.closest(".memo-item");
		    const memoText = memoItem.querySelector(".memo-text").innerText;

		    const dateItem = button.closest(".date-item");
		    const selDate = dateItem.querySelector(".selectDate").value;

		    if (memoMap.has(selDate)) {
		        let memos = memoMap.get(selDate);
		        memos = memos.filter(m => m !== memoText);
		        memoMap.set(selDate, memos);
		    }

		    memoItem.remove(); // DOM에서도 제거
		}

		function editDetail() {
		    const detailList = [];
		    const placeObj = {};
		    const memoObj = {};

		    document.querySelectorAll(".date-item").forEach(item => {
		        const detailNo = item.querySelector(".detailNo").value;
				const selectDate = item.querySelector(".selectDate").value;
		        // detailList에 detailNo 추가
		        detailList.push({ detailNo, selectDate });

		        // 장소 정보 수집
		        const placeIds = Array.from(item.querySelectorAll(".addDetail .place-item input"))
		                              .map(input => input.value);
		        placeObj[detailNo] = placeIds;

		        // 메모 정보 수집
		        const memoTexts = Array.from(item.querySelectorAll(".addMemo .memo-text"))
		                               .map(span => span.innerText);
		        memoObj[detailNo] = memoTexts;
		    });

		    const data = {
				tripNo: trip.tripNo,
		        detailList: detailList,
		        placeMap: placeObj,
		        memoMap: memoObj
		    };

		    fetch('/schedule/editDetail', {
		        method: 'PUT',
		        headers: {
		            'Content-Type': 'application/json'
		        },
		        body: JSON.stringify(data)
		    })
		        .then(response => response.text())
		        .then(message => {
		            alert(message); // 수정 완료 알림
					window.location.href = `/schedule/detail/${data.tripNo}`;
		        })
		        .catch(error => console.error("수정 중 에러 발생:", error));
		}

