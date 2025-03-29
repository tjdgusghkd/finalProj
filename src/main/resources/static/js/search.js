let query='';

const categoryButtons = document.querySelectorAll('.search-categories button');
const inputText = document.querySelector('#search-input-text');
const searchBar = document.querySelector('.search-bar');
const flightSearchBar = document.querySelector('.flight-search-bar');
const datePickerInput = document.querySelector('.date-picker');

const placeholderMap = {
    '여행지': '여행지',
    '관광명소': '관광명소, 액티비티 또는 여행지',
    '숙박': '호텔 이름 또는 여행지',
    '음식점': '음식점 또는 여행지'
};

// 일반 검색 ------------------------------------------------------------------------
let selectedCategory = "전체"; // 기본 카테고리

categoryButtons.forEach(button => {
    button.addEventListener('click', function () {
        categoryButtons.forEach(btn => btn.classList.remove('active'));
        button.classList.add('active');
        inputText.placeholder = placeholderMap[button.innerText] || '여행지, 관광명소, 숙박 등';
        const isFlightSearch = button.innerText == '항공권';
        searchBar.style.display = isFlightSearch ? 'none' : 'block';
        flightSearchBar.style.display = isFlightSearch ? 'block' : 'none';
        selectedCategory = this.getAttribute("data-category");
    });
});

// ✅ 장소 검색 함수 (서버로 요청을 보냄)
async function searchPlaces() {
    let city = document.querySelector('#search-input-text').value; // 검색 입력창에서 도시 이름 가져오기
    if (!city) { // 도시 이름이 입력되지 않은 경우
        alert("도시명을 입력하세요."); // 경고 메시지 표시
        return; // 함수 종료
    }

    const category = document.querySelector('.active').innerHTML;

    if (selectedCategory == '전체'){
        location.href=`/searchAll/${city}`;
    }else{
        location.href=`/search/${city}/${selectedCategory}/${category}`;
    }
}

// 항공 -----------------------------------------------------------------------------

let fpInitialized = false;

let fp = flatpickr(datePickerInput, {
    mode: "range",
    dateFormat: "Y-m-d",
    minDate: "today",
    defaultDate: [new Date(), new Date(new Date().setDate(new Date().getDate() + 7))],
    monthSelectorType: "static",
    showMonths: 2,
    locale: "ko",
    position: "below",
    closeOnSelect: true, // 두 번째 날짜 선택 후 자동으로 닫힘
    onOpen: function (selectedDates, dateStr, instance) {
        if (!fpInitialized) {
            if (!document.querySelector(".calendar-title")) {
                const titleDiv = document.createElement("div");
                titleDiv.classList.add("calendar-title");
                titleDiv.style.cssText = `
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    text-align: center;
                    font-size: 24px;
                    font-weight: bold;
                    padding: 20px 0;
                    background: white;
                    border-bottom: 1px solid #eee;
                    z-index: 1;
                `;
                updateTitle(selectedDates, titleDiv);
                instance.calendarContainer.insertBefore(titleDiv, instance.calendarContainer.firstChild);
            }

            instance.calendarContainer.style.cssText += `
                padding-top: 70px;
                border-radius: 8px;
                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            `;

            fpInitialized = true;
        }
        updateCalendarStyles(selectedDates, instance); // 달력 열릴 때 스타일 초기화
    },
    onChange: function (selectedDates, dateStr, instance) {
        const titleDiv = instance.calendarContainer.querySelector(".calendar-title");
        if (titleDiv) {
            updateTitle(selectedDates, titleDiv);
        }

        // 두 날짜가 선택되면 자동으로 적용
        if (selectedDates.length == 2) {
            const startDate = selectedDates[0].toISOString().split('T')[0]; // YYYY-MM-DD 형식
            const endDate = selectedDates[1].toISOString().split('T')[0];   // YYYY-MM-DD 형식
            const startDateKr = selectedDates[0].toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' });
            const endDateKr = selectedDates[1].toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' });
			

            // 입력 필드에 두 형식으로 값 설정
            datePickerInput.value = `${startDate} ~ ${endDate}`; // 데이터 전송용 형식
            datePickerInput.dataset.displayValue = `${startDateKr} ~ ${endDateKr}`; // 사용자 표시용 형식
			console.log(datePickerInput.value);
			console.log(datePickerInput.dataset.displayValue);

            // 달력 닫기
            instance.close();
            updateCalendarStyles(selectedDates, instance); // 스타일 업데이트
        }
    }
});

function updateTitle(selectedDates, titleDiv) {
    if (selectedDates.length == 2) {
        const startDate = selectedDates[0].toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' });
        const endDate = selectedDates[1].toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' });
        titleDiv.textContent = `${startDate} 출발 → ${endDate} 도착`;
    } else {
        titleDiv.textContent = "출발일과 도착일을 선택하세요";
    }
}

// 날짜 범위 선택 시 스타일 변경 함수 (기존 유지)
function updateCalendarStyles(selectedDates, instance) {
    const days = instance.calendarContainer.querySelectorAll('.flatpickr-day');

    // 모든 날짜 초기화
    days.forEach(day => {
        day.classList.remove('selected-range-start', 'selected-range-end', 'selected-range-in-between');
        day.style.backgroundColor = '';
        day.style.borderColor = '';
        day.style.color = '';
    });

    // 날짜 범위가 2개 이상 선택되었을 때 스타일 적용
    if (selectedDates.length == 2) {
        const [startDate, endDate] = selectedDates;
        const startUnix = startDate.getTime();
        const endUnix = endDate.getTime();

        days.forEach(day => {
            const dayDate = day.dateObj.getTime();
            if (dayDate === startUnix) {
                day.classList.add('selected-range-start');
                day.style.backgroundColor = '#000';
                day.style.borderColor = '#000';
                day.style.color = '#fff';
                day.style.borderRadius = '50% 0 0 50%'; // 시작 날짜 왼쪽 둥글게
            } else if (dayDate === endUnix) {
                day.classList.add('selected-range-end');
                day.style.backgroundColor = '#000';
                day.style.borderColor = '#000';
                day.style.color = '#fff';
                day.style.borderRadius = '0 50% 50% 0'; // 끝 날짜 오른쪽 둥글게
            } else if (dayDate > startUnix && dayDate < endUnix) {
                day.classList.add('selected-range-in-between');
                day.style.backgroundColor = '#f0f0f0';
                day.style.borderColor = '#f0f0f0';
                day.style.color = '#333';
            }
        });
    }
}

let style = document.createElement('style');
style.textContent = `
    .flatpickr-calendar {
        width: 900px;
        background: white;
    }
    .flatpickr-day.selected {
        background: black !important;
        border-color: black !important;
    }
    .flatpickr-day.inRange {
        background: #f0f0f0 !important;
        border-color: #f0f0f0 !important;
    }
    .flatpickr-day {
        border-radius: 50% !important;
        margin: 2px;
    }
    .flatpickr-day.selected.startRange,
    .flatpickr-day.selected.endRange {
        background: black !important;
    }
    .flatpickr-day.selected-range-start {
        background: #000 !important;
        border-color: #000 !important;
        color: #fff !important;
        border-radius: 50% 0 0 50% !important;
    }
    .flatpickr-day.selected-range-end {
        background: #000 !important;
        border-color: #000 !important;
        color: #fff !important;
        border-radius: 0 50% 50% 0 !important;
    }
    .flatpickr-day.selected-range-in-between {
        background: #f0f0f0 !important;
        border-color: #f0f0f0 !important;
        color: #333 !important;
    }
    .flatpickr-months {
        padding-top: 20px;
    }
    .iata-dropdown {
        position: absolute;
        top: 100%;
        left: 0;
        right: 0;
        background: white;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        margin-top: 4px;
        max-height: 300px;
        overflow-y: auto;
        z-index: 1000;
    }
    .search-result-item {
        padding: 12px;
        display: flex;
        align-items: center;
        gap: 12px;
        cursor: pointer;
        border-bottom: 1px solid #f0f0f0;
        transition: background-color 0.2s;
    }
    .search-result-item:hover,
    .search-result-item.active {
        background-color: #f8f8f8;
    }
    .search-result-content {
        flex: 1;
    }
    .search-result-title {
        font-weight: 500;
        font-size: 14px;
    }
    .search-result-subtitle {
        color: #666;
        font-size: 12px;
        margin-top: 2px;
    }
`;
document.head.appendChild(style);

datePickerInput.addEventListener('click', (event) => {
    if (!fp.isOpen) {
        fp.open();
    }
    event.stopPropagation();
});

document.addEventListener('click', (event) => {
    const calendar = document.querySelector('.flatpickr-calendar');
    if (calendar && !calendar.contains(event.target) && event.target !== datePickerInput) {
        event.stopPropagation();
    }
});

const selection = document.querySelector('.traveler-selection');
const traveler = document.querySelector('.traveler-picker');
const resetBtn = document.querySelector('.reset');

const applyButton = document.querySelector('.apply');
const incrementButtons = document.querySelectorAll('.increment');
const decrementButtons = document.querySelectorAll('.decrement');
const countElements = document.querySelectorAll('.count');
const maxTravelers = 9;

const warningMessage = document.createElement('p');
warningMessage.textContent = "최대 9명까지만 선택할 수 있습니다.";
warningMessage.style.cssText = `
        color: red;
        font-size: 14px;
        margin-top: 8px;
        display: none;
    `;
selection.appendChild(warningMessage);

traveler.addEventListener('click', function (e) {
	console.log('click');
    e.stopPropagation();
    selection.style.display = 'block';
});



document.addEventListener('click', function (e) {
    if (!selection.contains(e.target) && e.target !== traveler) {
        selection.style.display = 'none';
    }
});

selection.addEventListener('click', function (e) {
    e.stopPropagation();
});

incrementButtons.forEach(button => {
    button.addEventListener('click', function () {
        let totalTravelers = getTotalTravelers();
        if (totalTravelers < maxTravelers) {
            let countElement = button.previousElementSibling;
            countElement.textContent = parseInt(countElement.textContent) + 1;
        }

    });

    document.getElementsByName('arrivalName')[0].addEventListener('input', function() {
        query = this.value;
        if (query.length >= 1) {
            searchAirports(query, 'arrival-dropdown');
        } else {
            document.getElementById('arrival-dropdown').style.display = 'none';
        }
    });

    // 드롭다운 외부 클릭 시 숨김 처리
    document.addEventListener('click', function(event) {
        if (!event.target.closest('.dropdown-list') && !event.target.matches('input')) {
            document.getElementById('departure-dropdown').style.display = 'none';
            document.getElementById('arrival-dropdown').style.display = 'none';
        }
    });

    document.querySelector('.apply').addEventListener('click', () => {
        updateWarningMessage();
    });


decrementButtons.forEach(button => {
    button.addEventListener('click', function () {
        let countElement = button.nextElementSibling;
        let currentCount = parseInt(countElement.textContent);
        if (currentCount > 0) {
            countElement.textContent = currentCount - 1;
        }
        updateWarningMessage();
    });
});


function getTotalTravelers() {
    return Array.from(countElements).reduce((total, el) => total + parseInt(el.textContent), 0);
}

function updateTravelerCount() {
    let totalTravelers = getTotalTravelers();
    traveler.textContent = `여행자 ${totalTravelers}명`;
    document.querySelector('input[name="travelers"]').value = totalTravelers;
}

resetBtn.addEventListener('click', function () {
    countElements.forEach(countElement => {
        countElement.textContent = '0';
    });
    updateTravelerCount();
});

applyButton.addEventListener('click', function () {
    updateTravelerCount();
    selection.style.display = 'none';
});

function updateWarningMessage() {
    let totalTravelers = getTotalTravelers();
    warningMessage.style.display = totalTravelers >= maxTravelers ? 'block' : 'none';
}

function searchAirports(query, dropdownId) {
    $.ajax({
        url: '/flight/search',
        data: {query: query},
        dataType: 'json',
        success: data => {
            const dropdown = document.getElementById(dropdownId);
            dropdown.innerHTML = ''; // 기존 목록 초기화
            if (data.length > 0) {
                data.forEach(airport => {
                    const li = document.createElement('li');
					li.innerHTML = `${airport.korCountryName}, ${airport.korCityName}<br>${airport.korAirportName} (${airport.iataCode})`;
                    li.dataset.korAirportName = airport.korAirportName + '(' + airport.iataCode + ')'; // 클릭 시 사용
                    li.addEventListener('click', function () {
                        const input = document.getElementsByName(dropdownId.includes('departure') ? 'departureName' : 'arrivalName')[0];
                        input.innerText = airport.korAirportName + '(' + airport.iataCode + ')';
                        input.value = airport.korAirportName + '(' + airport.iataCode + ')'; // 공항 이름 + IATA 코드 입력
                        dropdown.style.display = 'none';
                    });
                    dropdown.appendChild(li);
                });
                dropdown.style.display = 'block';
            } else {
                dropdown.style.display = 'none';
            }
        },
        error: () => console.log("공항 검색 실패")
    });
}

document.getElementsByName('departureName')[0].addEventListener('input', function () {
    query = this.value;
    if (query.length >= 1) {
		setTimeout(() => {
        	searchAirports(query, 'departure-dropdown');
		}, 1000);
    } else {
        document.getElementById('departure-dropdown').style.display = 'none';
    }
});

document.getElementsByName('arrivalName')[0].addEventListener('input', function () {
    query = this.value;
    if (query.length >= 1) {
		setTimeout(()=>{
        	searchAirports(query, 'arrival-dropdown');
		}, 1000);
    } else {
        document.getElementById('arrival-dropdown').style.display = 'none';
    }
});

// 드롭다운 외부 클릭 시 숨김 처리
document.addEventListener('click', function (event) {
    if (!event.target.closest('.dropdown-list') && !event.target.matches('input')) {
        document.getElementById('departure-dropdown').style.display = 'none';
        document.getElementById('arrival-dropdown').style.display = 'none';
    }
});

document.querySelector('.search-btn').addEventListener('click', () => {
    const form = document.querySelector('.search-form');
    form.action = '/flight/flightSearch';
    form.submit();

	});
});

document.querySelector('.search-bar').addEventListener('keyup', (e) => {
	if(e.key=='Enter') {
//		console.log('enter');
		searchPlaces();
		}
});


window.onbeforeunload = function () { 
    	$('#loading').show();
    	$('body').css({overflow:'hidden'});
    }  //현재 페이지에서 다른 페이지로 넘어갈 때 표시해주는 기능
    window.addEventListener('load', () =>{
    	 $('#loading').hide();
    	 $('body').css({overflow:'auto'});
    });
