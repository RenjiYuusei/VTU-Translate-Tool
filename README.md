# VTU Translate Tool

## Giới thiệu

VTU Translate Tool là một ứng dụng Android giúp dịch các file strings.xml trong các dự án Android từ tiếng Anh sang tiếng Việt. Ứng dụng sử dụng API của Groq để thực hiện việc dịch tự động với model Meta Llama 4 Scout 17B mặc định, đồng thời cung cấp giao diện người dùng thân thiện để xem và chỉnh sửa các bản dịch. Ứng dụng có khả năng nhận diện thông minh các chuỗi không cần dịch như tên package, class, URL và format specifiers, đồng thời cho phép người dùng kiểm soát quá trình dịch với nút "Dừng Dịch".

## Tính năng

- **Dịch tự động**: Dịch các chuỗi từ tiếng Anh sang tiếng Việt sử dụng API của Groq
- **Xử lý thông minh**: Tự động nhận diện và xử lý các chuỗi đặc biệt như tên package, class, URL, format specifiers
- **Kiểm soát quá trình dịch**: Có thể dừng quá trình dịch bất cứ lúc nào với nút "Dừng Dịch"
- **Giao diện trực quan**: Hiển thị tiến trình dịch và trạng thái của từng chuỗi
- **Chỉnh sửa thủ công**: Cho phép người dùng chỉnh sửa bản dịch trước khi lưu
- **Xuất file**: Lưu kết quả dịch vào file strings.xml mới
- **Nhật ký**: Ghi lại quá trình dịch để dễ dàng theo dõi và gỡ lỗi
- **Model AI tiên tiến**: Sử dụng model Meta Llama 4 Scout 17B mặc định cho chất lượng dịch tốt nhất

## Cài đặt

### Phương pháp 1: Sử dụng Android Studio

1. Clone repository này
2. Mở dự án trong Android Studio
3. Build và cài đặt ứng dụng trên thiết bị Android của bạn

### Phương pháp 2: Sử dụng GitHub Actions

Dự án này đã được cấu hình với GitHub Actions để tự động build APK release đã được ký và tối ưu hóa:

1. Truy cập tab "Actions" trong repository GitHub
2. Chọn workflow "Android CI"
3. Nhấn nút "Run workflow" và chọn branch muốn build
4. Sau khi workflow hoàn tất, tải xuống APK từ artifacts
5. Cài đặt APK trên thiết bị Android của bạn

Xem [Hướng dẫn sử dụng GitHub Actions](docs/github-actions-guide.md), [Hướng dẫn thiết lập Keystore](docs/keystore-setup.md) và [Hướng dẫn tối ưu hóa R8](docs/r8-optimization-guide.md) để biết thêm chi tiết.

### Tối ưu hóa và Bảo mật

Ứng dụng sử dụng R8 để tối ưu hóa kích thước APK và mã hóa mã nguồn:

- **Minification**: Giảm kích thước APK bằng cách loại bỏ mã không sử dụng
- **Obfuscation**: Mã hóa tên lớp, phương thức và biến để bảo vệ mã nguồn
- **Resource Shrinking**: Loại bỏ tài nguyên không sử dụng
- **Signing**: APK được ký tự động bằng keystore đã cấu hình
- **Tối ưu hóa bộ nhớ**: Cấu hình tối ưu để tránh lỗi OutOfMemoryError khi build với R8
- **Cấu hình Gradle**: Tăng bộ nhớ heap và bật các tùy chọn tối ưu hóa như parallel, caching

## Cách sử dụng

1. Mở ứng dụng VTU Translate Tool
2. Vào màn hình Cài đặt để nhập API key của Groq (model Meta Llama 4 Scout 17B đã được cài đặt mặc định)
3. Quay lại màn hình chính và chọn file strings.xml cần dịch
4. Nhấn nút "Bắt đầu dịch" để bắt đầu quá trình dịch
5. Nếu cần dừng quá trình dịch giữa chừng, nhấn nút "Dừng Dịch"
6. Xem và chỉnh sửa các bản dịch nếu cần
7. Lưu kết quả dịch vào file mới bằng nút "Lưu file đã dịch"

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

### [Nhật kí thay đổi](CHANGELOG.md)

## Đóng góp

Chúng tôi rất hoan nghênh mọi đóng góp! Nếu bạn muốn đóng góp, vui lòng:

1. Fork repository
2. Tạo branch mới (`git checkout -b feature/amazing-feature`)
3. Commit các thay đổi của bạn (`git commit -m 'Add some amazing feature'`)
4. Push lên branch (`git push origin feature/amazing-feature`)
5. Mở Pull Request

## [Giấy phép](LICENSE)

Dự án này được phân phối dưới giấy phép GNU GPL v3.0. Xem file [LICENSE](LICENSE) để biết thêm chi tiết.

## Liên hệ

Nếu bạn có bất kỳ câu hỏi hoặc góp ý nào, vui lòng tạo issue trong repository này.

## Thông tin

- **Tác giả**: RenjiYuusei
- **Discord**: [https://discord.gg/hVQm9fNV](https://discord.gg/hVQm9fNV)
