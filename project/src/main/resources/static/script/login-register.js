// ==========================================
// 1. Hàm ẩn/hiện mật khẩu
// ==========================================
function togglePassword(inputId, iconElement) {
    const input = document.getElementById(inputId);
    const isPassword = input.getAttribute("type") === "password";

    input.setAttribute("type", isPassword ? "text" : "password");

    if (isPassword) {
        iconElement.classList.remove("fa-eye-slash");
        iconElement.classList.add("fa-eye");
    } else {
        iconElement.classList.remove("fa-eye");
        iconElement.classList.add("fa-eye-slash");
    }
}

// ==========================================
// 2. Xử lý các sự kiện khi trang đã load xong
// ==========================================
document.addEventListener("DOMContentLoaded", () => {
    const loginView = document.getElementById("loginView");
    const registerView = document.getElementById("registerView");
    const showRegisterBtn = document.getElementById("showRegisterBtn");
    const showLoginBtn = document.getElementById("showLoginBtn");

    // Chuyển đổi giữa Đăng nhập và Đăng ký
    if(showRegisterBtn && showLoginBtn) {
        showRegisterBtn.addEventListener("click", () => {
            loginView.classList.add("hidden");
            registerView.classList.remove("hidden");
        });

        showLoginBtn.addEventListener("click", () => {
            registerView.classList.add("hidden");
            loginView.classList.remove("hidden");
        });
    }

    // Tự động giữ form Đăng ký nếu có lỗi validation (báo đỏ)
    const hasErrors = document.querySelector('.error-text') !== null;
    if (hasErrors) {
        loginView.classList.add("hidden");
        registerView.classList.remove("hidden");
    }

    // ==========================================
    // TÍCH HỢP SWEETALERT2 BẮT LỖI TỪ URL PARAMS
    // ==========================================
    const urlParams = new URLSearchParams(window.location.search);

    // Nếu URL có ?success (Từ Controller đăng ký trả về)
    if (urlParams.has('success')) {
        if(typeof showSuccessAlert === 'function') {
            showSuccessAlert('Đăng ký thành công! Vui lòng đăng nhập.');
        }
        // Đảm bảo ở màn hình đăng nhập
        registerView.classList.add("hidden");
        loginView.classList.remove("hidden");
    }

    // Nếu URL có ?error (Từ Spring Security trả về khi đăng nhập sai)
    if (urlParams.has('error')) {
        if(typeof showErrorAlert === 'function') {
            showErrorAlert('Sai tên đăng nhập hoặc mật khẩu!');
        }
    }
});