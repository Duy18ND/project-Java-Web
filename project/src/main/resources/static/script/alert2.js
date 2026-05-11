// File: static/script/alert2.js

// 1. Hàm Thành công
function showSuccessAlert(message) {
    Swal.fire({
        icon: 'success',
        title: 'Thành công!',
        text: message,
        confirmButtonColor: '#10b981',
        timer: 2500
    });
}

// 2. Hàm Lỗi
function showErrorAlert(message) {
    Swal.fire({
        icon: 'error',
        title: 'Lỗi!',
        text: message,
        confirmButtonColor: '#ef4444'
    });
}

// 3. Hàm Cảnh báo (Dùng cho nút Xóa)
// Lưu ý: Tên hàm phải là showConfirmDelete để khớp với HTML Duy viết
function showConfirmDelete(message, title, confirmCallback) {
    Swal.fire({
        icon: 'warning',
        title: title || 'Xác nhận xóa?',
        text: message || 'Dữ liệu này sẽ không thể khôi phục!',
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        cancelButtonColor: '#6b7280',
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed && typeof confirmCallback === 'function') {
            confirmCallback();
        }
    });
}