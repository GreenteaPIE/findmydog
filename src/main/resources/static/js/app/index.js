var main = {
    init: function() {
        var _this = this;
        $('#btn-save').on('click', function() {
            _this.save();
        });
        $('#btn-update').on('click', function() {
            _this.update();
        });
        $('#btn-delete').on('click', function() {
            _this.delete();
        });
    },
    save: function() {
        // 각 입력 필드의 값을 가져옵니다.
        var kind = $('#kind').val();
        var title = $('#title').val();
        var author = $('#author').val();
        var reporterName = $('#reporterName').val();
        var contact = $('#contact').val();
        var lostDate = $('#lostDate').val();
        var landmark = $('#landmark').val();
        var breed = $('#breed').val();
        var color = $('#color').val();
        var gender = $('#gender').val();
        var age = $('#age').val();
        var features = $('#features').val();
        var hasMicrochip = $('#hasMicrochip').val() === 'true';
        var latitude = $('#latitude').val();
        var longitude = $('#longitude').val();
        var imageFiles = $('#image')[0].files;

        // 각 항목의 유효성을 검사합니다.
        if (!kind || !title || !author || !reporterName || !contact || !lostDate || !landmark || !breed || !color || !gender || !age || !features || !latitude || !longitude || imageFiles.length < 1) {
            alert('모든 항목을 입력해주세요.');
            return;
        }

        // 연락처 유효성 검사
        if (!/^\d{3}-\d{3,4}-\d{4}$/.test(contact)) {
            alert('유효하지 않은 연락처 형식입니다. (000-0000-0000)');
            return;
        }

        // 나이 유효성 검사
        if (isNaN(age) || age <= 0) {
            alert('유효하지 않은 나이입니다.');
            return;
        }

        // 위도, 경도 유효성 검사
        if (isNaN(latitude) || isNaN(longitude)) {
            alert('유효하지 않은 위치 정보입니다.');
            return;
        }

        // 이미지 파일 유효성 검사
        var maxSizeInBytes = 10e6; // 10MB
        for (var i = 0; i < imageFiles.length; i++) {
            var fileSize = imageFiles[i].size;
            if (fileSize > maxSizeInBytes) {
                alert('이미지 파일 크기는 10MB 이하여야 합니다.');
                return;
            }
        }


        // 유효성 검사를 통과하면 글을 저장합니다.
        var data = {
            kind: kind,
            title: title,
            author: author,
            content: {
                reporterName: reporterName,
                contact: contact,
                lostDate: lostDate,
                landmark: landmark,
                breed: breed,
                color: color,
                gender: gender,
                age: age,
                features: features,
                hasMicrochip: hasMicrochip,
                latitude: latitude,
                longitude: longitude
            }
        };

        var formData = new FormData();
        formData.append('post', new Blob([JSON.stringify(data)], { type: "application/json" }));

        for (var i = 0; i < imageFiles.length; i++) {
            formData.append("images", imageFiles[i]);
        }

        $.ajax({
            type: 'POST',
            url: '/api/v1/posts',
            processData: false, // 필수
            contentType: false, // 필수
            data: formData
        }).done(function() {
            alert('글이 등록되었습니다.');
            window.location.href = '/posts/list';
        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
    },

    update: function() {
        var data = {
            title: $('#title').val(),
            content: $('#content').val()
        };

        var id = $('#id').val();

        $.ajax({
            type: 'PUT',
            url: '/api/v1/posts/' + id,
            dataType: 'json',
            contentType: 'application/json; charset=UTF-8',
            data: JSON.stringify(data)
        }).done(function() {
            alert('글이 수정되었습니다.');
            window.location.href = '/';
        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
    },
    delete: function() {
        var id = $('#id').val();

        $.ajax({
            type: 'DELETE',
            url: '/api/v1/posts/' + id,
            dataType: 'json',
            contentType: 'application/json; charset=UTF-8',
        }).done(function() {
            alert('글이 삭제되었습니다.');
            window.location.href = '/';
        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
    }
};

main.init();
