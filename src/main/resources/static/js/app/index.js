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
        var imageFiles = $('#image')[0].files;
        if (imageFiles.length < 1) {
            alert('이미지 파일을 한 장 이상 올려주세요.');
            return;
        }

        var data = {
            kind: $('#kind').val(),
            title: $('#title').val(),
            author: $('#author').val(),
            content: {
                reporterName: $('#reporterName').val(),
                contact: $('#contact').val(),
                lostDate: $('#lostDate').val(),
                landmark: $('#landmark').val(),
                breed: $('#breed').val(),
                color: $('#color').val(),
                gender: $('#gender').val(),
                age: $('#age').val(),
                features: $('#features').val(),
                hasMicrochip: $('#hasMicrochip').val() === 'true',
                latitude: $('#latitude').val(),
                longitude: $('#longitude').val()
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
