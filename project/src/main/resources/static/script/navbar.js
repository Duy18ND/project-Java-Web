document.addEventListener('DOMContentLoaded', () => {

    // ==========================================
    // 1. Quản lý State Tabs (Chuyển đổi Active Tab)
    // ==========================================
    const desktopBtns = document.querySelectorAll('.desktop-nav .nav-item');
    const mobileBtns = document.querySelectorAll('.mobile-nav-grid .mobile-nav-item');

    function setActiveTab(clickedBtn) {
        // Lấy tên tab được click để đồng bộ giữa cả 2 giao diện (Desktop & Mobile)
        const tabText = clickedBtn.querySelector('.nav-item-text').innerText;

        // Cập nhật class cho Desktop
        desktopBtns.forEach(btn => {
            if (btn.querySelector('.nav-item-text').innerText === tabText) {
                btn.className = "nav-item nav-item-active";
            } else {
                btn.className = "nav-item nav-item-inactive";
            }
        });

        // Cập nhật class cho Mobile
        mobileBtns.forEach(btn => {
            if (btn.querySelector('.nav-item-text').innerText === tabText) {
                btn.className = "mobile-nav-item nav-item-active";
            } else {
                btn.className = "mobile-nav-item nav-item-inactive";
            }
        });
    }

    // Lắng nghe sự kiện click trên Tabs
    desktopBtns.forEach(btn => btn.addEventListener('click', () => setActiveTab(btn)));
    mobileBtns.forEach(btn => btn.addEventListener('click', () => {
        setActiveTab(btn);
        toggleMobileMenu(false); // Tự động đóng menu trên mobile sau khi chọn xong
    }));


    // ==========================================
    // 2. Quản lý Mobile Menu Hamburger (Mở/Đóng)
    // ==========================================
    const mobileMenuBtn = document.querySelector('.mobile-toggle-btn');
    const mobileMenu = document.querySelector('.mobile-nav-wrapper');
    let isMobileMenuOpen = false;

    function toggleMobileMenu(forceState) {
        isMobileMenuOpen = forceState !== undefined ? forceState : !isMobileMenuOpen;

        if (isMobileMenuOpen) {
            mobileMenu.classList.remove('hidden');
        } else {
            mobileMenu.classList.add('hidden');
        }
    }

    if (mobileMenuBtn && mobileMenu) {
        mobileMenuBtn.addEventListener('click', () => toggleMobileMenu());
    }


    // ==========================================
    // 3. Quản lý User Profile Dropdown
    // ==========================================
    const profileWrapper = document.querySelector('.profile-wrapper');
    const dropdownMenu = document.querySelector('.dropdown-menu');
    let isDropdownOpen = false;
    let timeoutRef = null;

    function setDropdownState(state) {
        isDropdownOpen = state;
        if (state) {
            dropdownMenu.classList.remove('hidden');
        } else {
            dropdownMenu.classList.add('hidden');
        }
    }

    if (profileWrapper && dropdownMenu) {
        // Xử lý khi di chuột vào (Desktop)
        profileWrapper.addEventListener('mouseenter', () => {
            clearTimeout(timeoutRef);
            setDropdownState(true);
        });

        // Xử lý khi di chuột ra (Desktop) - Có độ trễ để không bị chớp tắt
        profileWrapper.addEventListener('mouseleave', () => {
            timeoutRef = setTimeout(() => {
                setDropdownState(false);
            }, 150);
        });

        // Xử lý click (Dành cho Mobile/Tablet)
        profileWrapper.addEventListener('click', () => {
            if (window.innerWidth <= 1024) {
                setDropdownState(!isDropdownOpen);
            }
        });
    }

    // Đóng dropdown khi click ra ngoài vùng profile
    document.addEventListener('mousedown', (event) => {
        if (profileWrapper && !profileWrapper.contains(event.target)) {
            setDropdownState(false);
        }
    });
});