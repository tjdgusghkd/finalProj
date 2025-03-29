const goGoogleMap = () => {
    location.href = 'https://www.google.com/maps/place/?q=place_id:' + document.getElementById('placeId').value;
}

const userNo = window.userNo;
console.log(userNo);
document.getElementById('star').addEventListener('click', () => {
    console.log(userNo)
    if (userNo == 0) {
        alert('로그인 후 이용해 주세요.');
    } else {
        $.ajax({
            url: '/star',
            data: {
                userNo: userNo,
                type: document.getElementById('type').value,
                apiId: document.getElementById('placeId').value
            },
            type: "post",
            success: data => {
                const count = document.getElementById('count');
                const star = document.getElementById('star');
                if (data == 'insert') {
                    count.innerText = parseInt(count.innerText) + 1;
                    star.innerHTML = '<img src="/img/heart2.svg" alt="Filled Heart" style="width: 32px; height: 32px;">';
                } else if (data == 'delete') {
                    count.innerText = parseInt(count.innerText) - 1;
                    star.innerHTML = '<img src="/img/heart1.svg" alt="Empty Heart" style="width: 32px; height: 32px;">';
                } else {
                    alert('오류 발생');
                }
            },
            error: data => console.log(data)
        });
    }
});