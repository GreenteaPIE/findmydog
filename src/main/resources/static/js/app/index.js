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
    validateFields: function(fields) {
        var invalidFields = [];

        fields.forEach(function(field) {
            if (!field.value) {
                invalidFields.push(field.name);
            }
        });

        return invalidFields;
    },
    save: function() {
        // 각 입력 필드의 값을 가져옵니다.
        var userId = $('#userId').val();
        var kind = $('#kind').val();
        var title = $('#title').val();
        var author = $('#author').val();
        var reporterName = $('#reporterName').val();
        var contact = $('#contact').val();
        var lostDate = $('#lostDate').val();
        var landmark = $('#landmark').val();
        var breed = $('#breed').val();
        var pname = $('#pname').val();
        var color = $('#color').val();
        var gender = $('#gender').val();
        var age = $('#age').val();
        var features = $('#features').val();
        var hasMicrochip = $('#hasMicrochip').val() === 'true';
        var latitude = $('#latitude').val();
        var longitude = $('#longitude').val();
        var imageFiles = $('#image')[0].files;

        var fields = [
            {name: '분류', value: kind},
            {name: '제목', value: title},
            {name: '작성자', value: author},
            {name: '신고자 이름', value: reporterName},
            {name: '연락처', value: contact},
            {name: '분실 날짜', value: lostDate},
            {name: '주변건물', value: landmark},
            {name: '품종', value: breed},
            {name: '색상', value: color},
            {name: '특징', value: features},
            {name: '위도', value: latitude},
            {name: '경도', value: longitude},
            {name: '이미지 파일', value: imageFiles.length > 0}
        ];

        var invalidFields = this.validateFields(fields);

        if (invalidFields.length > 0) {
            alert(invalidFields.join(', ') + ' 입력을 해주세요.');
            return;
        }

        // 연락처 유효성 검사
        if (!/^\d{3}-\d{3,4}-\d{4}$/.test(contact)) {
            alert('유효하지 않은 연락처 형식입니다. (000-0000-0000)');
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
            userId: userId,
            content: {
                reporterName: reporterName,
                contact: contact,
                lostDate: lostDate,
                landmark: landmark,
                breed: breed,
                pname: pname,
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
            window.location.href = '/posts/paging';
        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
    },

    update: function() {
        var id = $('#id').val();
        var kind = $('#kind').val();
        var title = $('#title').val();
        var author = $('#author').val();
        var reporterName = $('#reporterName').val();
        var contact = $('#contact').val();
        var lostDate = $('#lostDate').val();
        var landmark = $('#landmark').val();
        var breed = $('#breed').val();
        var pname = $('#pname').val();
        var color = $('#color').val();
        var gender = $('#gender').val();
        var age = $('#age').val();
        var features = $('#features').val();
        var hasMicrochip = $('#hasMicrochip').val() === 'true';
        var latitude = $('#latitude').val();
        var longitude = $('#longitude').val();
        var imageFiles = $('#image')[0].files;

        var fields = [
            {name: '분류', value: kind},
            {name: '제목', value: title},
            {name: '작성자', value: author},
            {name: '신고자 이름', value: reporterName},
            {name: '연락처', value: contact},
            {name: '분실 날짜', value: lostDate},
            {name: '주변건물', value: landmark},
            {name: '품종', value: breed},
            {name: '색상', value: color},
            {name: '특징', value: features},
            {name: '위도', value: latitude},
            {name: '경도', value: longitude}
        ];

        var invalidFields = this.validateFields(fields);

        if (invalidFields.length > 0) {
            alert(invalidFields.join(', ') + ' 입력을 해주세요.');
            return;
        }

        // 연락처 유효성 검사
        if (!/^\d{3}-\d{3,4}-\d{4}$/.test(contact)) {
            alert('유효하지 않은 연락처 형식입니다. (000-0000-0000)');
            return;
        }


        // 위도, 경도 유효성 검사
        if (isNaN(latitude) || isNaN(longitude)) {
            alert('유효하지 않은 위치 정보입니다.');
            return;
        }

        // 이미지 파일 유효성 검사
        var imageChanged = imageFiles.length > 0;
        if (imageChanged) {
            var maxSizeInBytes = 10e6; // 10MB
            for (var i = 0; i < imageFiles.length; i++) {
                var fileSize = imageFiles[i].size;
                if (fileSize > maxSizeInBytes) {
                    alert('이미지 파일 크기는 10MB 이하여야 합니다.');
                    return;
                }
            }
        }

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
                pname: pname,
                color: color,
                gender: gender,
                age: age,
                features: features,
                hasMicrochip: hasMicrochip,
                latitude: latitude,
                longitude: longitude
            },
            imageChanged: imageChanged // 이미지가 변경되었는지 여부 추가
        };

        var formData = new FormData();
        formData.append('post', new Blob([JSON.stringify(data)], { type: "application/json" }));

        if (imageChanged) {
            for (var i = 0; i < imageFiles.length; i++) {
                formData.append("images", imageFiles[i]);
            }
        }

        $.ajax({
            type: 'PUT',
            url: '/api/v1/posts/' + id,
            processData: false, // 필수
            contentType: false, // 필수
            data: formData
        }).done(function() {
            alert('글이 수정되었습니다.');
            window.location.href = '/posts/detail/' + id;
        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
    },

    delete: function() {
        var id = $('#id').val();
        var confirmed = confirm('이 글을 삭제하시겠습니까?\n\n삭제된 글은 복구할 수 없습니다.');

        if (confirmed) {
            $.ajax({
                type: 'DELETE',
                url: '/api/v1/posts/' + id,
                dataType: 'json',
                contentType: 'application/json; charset=UTF-8',
            }).done(function() {
                alert('글이 삭제되었습니다.');
                window.location.href = '/posts/paging';
            }).fail(function(error) {
                alert(JSON.stringify(error));
            });
        }
    }
};

main.init();
