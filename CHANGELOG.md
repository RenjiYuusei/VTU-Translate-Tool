# Changelog

## Phiên bản 1.1.0 (29-6-2025)

### Cải tiến giao diện người dùng

- Cập nhật toàn bộ giao diện người dùng theo Material Design 3 với các thành phần hiện đại hơn.
- Thêm animation cho các chuyển đổi màn hình và hiệu ứng tương tác.
- Cải thiện màn hình Dịch với các thẻ nâng cao (ElevatedCard) và hiển thị trạng thái rõ ràng hơn.
- Cải thiện màn hình Nhật ký với hiệu ứng animation khi hiển thị log mới và thiết kế thẻ theo loại log.
- Cải thiện màn hình Cài đặt với bố cục thẻ riêng biệt cho từng mục: API Key, Chọn Model và Thông tin.
- Nâng cấp thanh điều hướng với hiệu ứng animation và màu sắc phù hợp với theme.

### Cải tiến quy trình phát triển

- Tích hợp GitHub Actions để tự động build APK khi có thay đổi code.
- Thêm tài liệu hướng dẫn sử dụng GitHub Actions để build APK.
- Cải thiện quy trình phân phối APK thông qua artifacts của GitHub Actions.
- Cập nhật các actions từ phiên bản v3 lên v4 để đáp ứng thông báo loại bỏ của GitHub và cải thiện hiệu suất.
- Sử dụng gradle thay vì gradlew trong workflow để tránh lỗi quyền thực thi và đơn giản hóa quy trình.

## Phiên bản 1.0.1 (28-6-2025)

### Tính năng mới

- Thêm nút "Dừng Dịch" để cho phép người dùng dừng dịch bất cứ lúc nào.
- Đã thêm khả năng nhận diện thông minh các chuỗi không cần dịch như tên package, class, URL và format specifiers.
- Đã chuyển đổi model mặc định từ Groq sang Meta Llama 4 Scout 17B để tăng chất lượng dịch.
- Đã triển khai một prompt dịch có ý nghĩa để hướng dẫn model dịch giữ lại các định dạng đặc biệt và các định danh kỹ thuật.

### Sửa lỗi

- Đã sửa lỗi một số vấn đề liên quan đến việc dịch các chuỗi kỹ thuật.
