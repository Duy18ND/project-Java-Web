/* ===================== TAB (Chuyển tab mượt mà) ===================== */
function switchTab(name, btn) {
    document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.getElementById('tab-' + name).classList.add('active');
    btn.classList.add('active');
}

/* ===================== PASSWORD (Đổi mật khẩu) ===================== */
// Hiển thị/Ẩn mật khẩu (Cái icon con mắt)
function togglePw(id, icon) {
    var inp = document.getElementById(id);
    if (inp.type === 'password') {
        inp.type = 'text';
        icon.classList.replace('fa-eye', 'fa-eye-slash');
    } else {
        inp.type = 'password';
        icon.classList.replace('fa-eye-slash', 'fa-eye');
    }
}

// Kiểm tra độ mạnh mật khẩu (Cái thanh màu chạy)
function checkStrength(val) {
    var fill  = document.getElementById('strengthFill');
    var label = document.getElementById('strengthLabel');
    var score = 0;
    if (val.length >= 8) score++;
    if (/[A-Z]/.test(val)) score++;
    if (/[0-9]/.test(val)) score++;
    if (/[^A-Za-z0-9]/.test(val)) score++;

    var levels = [
        { w:'0%',   color:'#e5e7eb', text:'' },
        { w:'25%',  color:'#ef4444', text:'Yếu' },
        { w:'50%',  color:'#f59e0b', text:'Trung bình' },
        { w:'75%',  color:'#3b82f6', text:'Khá mạnh' },
        { w:'100%', color:'#10b981', text:'Mạnh' },
    ];
    var l = val.length === 0 ? levels[0] : levels[score] || levels[1];
    fill.style.width = l.w;
    fill.style.background = l.color;
    label.textContent = l.text;
    label.style.color = l.color;
}

// Submit đổi mật khẩu (Sử dụng SweetAlert2 để báo lỗi Validate)
function savePassword() {
    var cur = document.getElementById('pw-current').value;
    var nw = document.getElementById('pw-new').value;
    var confirm = document.getElementById('pw-confirm').value;
    var ok = true;

    // Reset lỗi
    ['pw-current','pw-new','pw-confirm'].forEach(function(id) {
        document.getElementById('err-' + id).textContent = '';
        document.getElementById(id).classList.remove('error');
    });

    if (!cur) {
        document.getElementById('err-pw-current').textContent = 'Vui lòng nhập mật khẩu hiện tại';
        document.getElementById('pw-current').classList.add('error');
        ok = false;
    }
    if (nw.length < 8) {
        document.getElementById('err-pw-new').textContent = 'Mật khẩu mới phải có ít nhất 8 ký tự';
        document.getElementById('pw-new').classList.add('error');
        ok = false;
    }
    if (nw !== confirm) {
        document.getElementById('err-pw-confirm').textContent = 'Xác nhận mật khẩu không khớp';
        document.getElementById('pw-confirm').classList.add('error');
        ok = false;
    }

    if (!ok) return;

    // Gọi API Backend để đổi mật khẩu thực sự (Chỗ này bạn sẽ gọi Fetch/Axios tới Controller Đổi Mật Khẩu của bạn)
    // Tạm thời hiển thị Alert2 thông báo giả lập.
    if(typeof showSuccessAlert === 'function') {
        showSuccessAlert('Mật khẩu đã được cập nhật thành công!');

        // Reset ô nhập
        document.getElementById('pw-current').value = '';
        document.getElementById('pw-new').value = '';
        document.getElementById('pw-confirm').value = '';
        checkStrength('');
    } else {
        alert("Đã đổi mật khẩu. Đang chờ liên kết Backend!");
    }
}

/* ===================== AVATAR (Cập nhật ảnh tĩnh) ===================== */
/* ===================== CẬP NHẬT AVATAR (Hỗ trợ 5MB & Screenshots) ===================== */
var avatarInput = document.getElementById('avatarInput');
if(avatarInput) {
    avatarInput.addEventListener('change', function(e) {
        var file = e.target.files[0];
        if (!file) return;

        // CẬP NHẬT: Cho phép tối đa 5MB (5 * 1024 * 1024 bytes)
        const maxSize = 5 * 1024 * 1024;
        if (file.size > maxSize) {
            showErrorAlert('Ảnh quá lớn! Vui lòng chọn ảnh dưới 5MB (Ảnh chụp màn hình thường rất nặng).');
            return;
        }

        var formData = new FormData();
        formData.append("avatar", file);

        // Hiển thị loading nhẹ hoặc đổi ảnh tạm thời
        fetch('/profile/update-avatar', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (!response.ok) return response.json().then(err => { throw err; });
                return response.json();
            })
            .then(data => {
                if(data.success) {
                    const display = document.getElementById('avatarDisplay');
                    // Force reload ảnh bằng timestamp để tránh cache trình duyệt
                    display.innerHTML = `<img src="${data.avatarPath}?t=${new Date().getTime()}" class="avatar-img-actual">`;
                    showSuccessAlert('Cập nhật ảnh đại diện thành công!');
                }
            })
            .catch(err => {
                console.error(err);
                showErrorAlert(err.message || 'Lỗi khi tải ảnh lên. Hãy thử lại!');
            });
    });
}