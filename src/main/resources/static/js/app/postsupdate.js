//map
var mapContainer = document.getElementById('map'), // 지도를 표시할 div
    mapOption = {
        center: new kakao.maps.LatLng(33.450701, 126.570667), // 지도의 중심좌표
        level: 3 // 지도의 확대 레벨
    };

// 지도를 생성합니다
var map = new kakao.maps.Map(mapContainer, mapOption);

// 현재 위치를 가져와 지도의 중심으로 설정하고 마커를 생성하는 함수입니다.
function getCurrentLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function(position) {
            var lat = position.coords.latitude, // 위도
                lon = position.coords.longitude; // 경도
            var locPosition = new kakao.maps.LatLng(lat, lon); // 현재 위치를 좌표 객체로 생성

            // 지도 중심을 현재 위치로 이동합니다.
            map.setCenter(locPosition);

            // 사용자가 지도를 클릭하는 위치에 마커를 생성합니다.
            kakao.maps.event.addListener(map, 'click', function(mouseEvent) {
                var latlng = mouseEvent.latLng;

                // 마커를 클릭한 위치로 이동합니다.
                marker.setPosition(latlng);

                // 위도와 경도 입력 필드에 값을 설정합니다.
                document.getElementById('latitude').value = latlng.getLat();
                document.getElementById('longitude').value = latlng.getLng();
            });

            // 현재 위치에 마커를 생성합니다.
            var marker = new kakao.maps.Marker({
                position: locPosition,
                map: map
            });

        }, function(error) {
            console.error("Geolocation error: ", error);
            alert('현재 위치를 가져올 수 없습니다. 위치 정보 사용을 허용해주세요.');
        }, {
            maximumAge: 0,
            timeout: 10000,
            enableHighAccuracy: true
        });
    } else {
        alert('이 브라우저에서는 Geolocation이 지원되지 않습니다.');
    }
}

// 페이지 로딩 시 현재 위치를 가져오도록 합니다.
window.onload = function() {
    getCurrentLocation();
};

//image
$('#image').on('change', function(){
    var fileInput = this;
    if (fileInput.files && fileInput.files[0]) {
        var reader = new FileReader();

        reader.onload = function(e) {
            $('#image-preview').html('<img src="' + e.target.result + '" style="max-width: 250px; max-height: 250px;" class="img-fluid mt-3" />');
        }

        reader.readAsDataURL(fileInput.files[0]);
    }
});