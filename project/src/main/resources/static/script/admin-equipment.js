// ==================== CÁC HÀM XỬ LÝ MODAL ====================

// Hàm mở Modal để THÊM MỚI
function openAddModal() {
    const modal = document.getElementById('addModal');
    if (modal) {
        document.getElementById('modalTitle').innerText = "Thêm thiết bị mới";

        // Quan trọng: Reset form về trống
        const form = modal.querySelector('form');
        if (form) form.reset();

        // Xóa ID ẩn để Server biết đây là thêm mới (INSERT)
        const idInput = document.getElementById('form-id');
        if (idInput) idInput.value = "";

        modal.classList.remove('hidden');
    }
}

// Hàm mở Modal để SỬA (Lấy data từ API)
function openEditModal(id) {
    const modal = document.getElementById('addModal');

    // Gọi API lấy dữ liệu JSON từ Controller
    fetch('/admin/equipment/api/' + id)
        .then(res => {
            if (!res.ok) throw new Error("Không tìm thấy thiết bị");
            return res.json();
        })
        .then(data => {
            document.getElementById('modalTitle').innerText = "Chỉnh sửa thiết bị";

            // Đổ dữ liệu vào Form dựa trên ID của từng Input
            document.getElementById('form-id').value = data.id;
            document.getElementById('form-name').value = data.name;
            document.getElementById('form-lab').value = data.labId;
            document.getElementById('form-total').value = data.totalQuantity;
            document.getElementById('form-avail').value = data.availableQuantity;
            document.getElementById('form-cate').value = data.category;

            modal.classList.remove('hidden');
        })
        .catch(err => {
            console.error("Lỗi Fetch:", err);
            if (typeof showErrorAlert === "function") {
                showErrorAlert("Lỗi: " + err.message);
            } else {
                alert("Lỗi: " + err.message);
            }
        });
}

// Hàm ĐÓNG Modal
function closeModal() {
    const modal = document.getElementById('addModal');
    if (modal) modal.classList.add('hidden');
}