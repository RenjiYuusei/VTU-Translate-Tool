# VTU Translate Tool

## Giới thiệu

VTU Translate Tool là một ứng dụng Android giúp dịch các file strings.xml trong các dự án Android từ tiếng Anh sang tiếng Việt. Ứng dụng sử dụng API của Groq để thực hiện việc dịch tự động, đồng thời cung cấp giao diện người dùng thân thiện để xem và chỉnh sửa các bản dịch.

## Tính năng

- **Dịch tự động**: Dịch các chuỗi từ tiếng Anh sang tiếng Việt sử dụng API của Groq
- **Xử lý thông minh**: Tự động nhận diện và xử lý các chuỗi đặc biệt như tên package, class, URL
- **Giao diện trực quan**: Hiển thị tiến trình dịch và trạng thái của từng chuỗi
- **Chỉnh sửa thủ công**: Cho phép người dùng chỉnh sửa bản dịch trước khi lưu
- **Xuất file**: Lưu kết quả dịch vào file strings.xml mới
- **Nhật ký**: Ghi lại quá trình dịch để dễ dàng theo dõi và gỡ lỗi

## Cài đặt

1. Clone repository này
2. Mở dự án trong Android Studio
3. Build và cài đặt ứng dụng trên thiết bị Android của bạn

## Cách sử dụng

1. Mở ứng dụng VTU Translate Tool
2. Vào màn hình Cài đặt để nhập API key của Groq và chọn model
3. Quay lại màn hình chính và chọn file strings.xml cần dịch
4. Nhấn nút "Bắt đầu dịch" để bắt đầu quá trình dịch
5. Xem và chỉnh sửa các bản dịch nếu cần
6. Lưu kết quả dịch vào file mới

## Cấu trúc dự án

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/vtu/translate/
│   │   │   ├── data/
│   │   │   │   ├── model/         # Các model dữ liệu
│   │   │   │   └── repository/    # Các repository xử lý logic
│   │   │   ├── ui/
│   │   │   │   ├── screens/       # Các màn hình của ứng dụng
│   │   │   │   ├── viewmodel/     # ViewModels
│   │   │   │   └── theme/         # Theme và styling
│   │   │   └── VtuTranslateApp.kt # Application class
│   │   └── res/                   # Resources
│   └── test/                      # Unit tests
└── build.gradle                   # Cấu hình build
```

## Cải tiến mã nguồn

### Xử lý trạng thái

Ứng dụng sử dụng sealed class để quản lý trạng thái UI một cách toàn diện:

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

### Xử lý lỗi tập trung

Ứng dụng sử dụng một hệ thống xử lý lỗi tập trung để hiển thị thông báo lỗi nhất quán:

```kotlin
fun handleError(error: Throwable) {
    val errorMessage = when (error) {
        is HttpException -> {
            when (error.code()) {
                429 -> "Đạt giới hạn tốc độ API. Vui lòng thử lại sau."
                401 -> "API key không hợp lệ. Vui lòng kiểm tra lại."
                else -> "Lỗi HTTP: ${error.code()}"
            }
        }
        is IOException -> "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối của bạn."
        else -> error.message ?: "Đã xảy ra lỗi không xác định."
    }
    _errorEvent.value = Event(errorMessage)
}
```

### Tối ưu hiệu suất

Ứng dụng sử dụng các kỹ thuật tối ưu hiệu suất như:

- Sử dụng `key` trong `LazyColumn` để cải thiện hiệu suất khi danh sách thay đổi
- Sử dụng `remember` và `derivedStateOf` để tránh tính toán lại không cần thiết
- Xử lý batch và delay để tránh bị giới hạn tốc độ API

## Đóng góp

Chúng tôi rất hoan nghênh mọi đóng góp! Nếu bạn muốn đóng góp, vui lòng:

1. Fork repository
2. Tạo branch mới (`git checkout -b feature/amazing-feature`)
3. Commit các thay đổi của bạn (`git commit -m 'Add some amazing feature'`)
4. Push lên branch (`git push origin feature/amazing-feature`)
5. Mở Pull Request

## Giấy phép

Dự án này được phân phối dưới giấy phép GNU GPL v3.0. Xem file `LICENSE` để biết thêm chi tiết.

## Liên hệ

Nếu bạn có bất kỳ câu hỏi hoặc góp ý nào, vui lòng tạo issue trong repository này.
