# Hướng dẫn sử dụng GitHub Actions để build APK

Tài liệu này hướng dẫn chi tiết cách sử dụng GitHub Actions để tự động build APK cho ứng dụng VTU Translate Tool.

## Giới thiệu về GitHub Actions

GitHub Actions là một nền tảng CI/CD (Continuous Integration/Continuous Deployment) được tích hợp sẵn trong GitHub. Nó cho phép tự động hóa các quy trình như build, test và deploy ứng dụng mỗi khi có thay đổi được push lên repository.

## Lợi ích của việc sử dụng GitHub Actions

- **Tự động hóa**: Tự động build APK mỗi khi có thay đổi code
- **Không cần máy tính cá nhân**: Build APK trên máy chủ của GitHub, không cần cấu hình môi trường phát triển trên máy tính cá nhân
- **Nhất quán**: Đảm bảo môi trường build luôn nhất quán
- **Tiết kiệm thời gian**: Không cần thực hiện thủ công các bước build
- **Dễ dàng phân phối**: Cung cấp APK cho người dùng thông qua artifacts của GitHub Actions

## Cấu hình GitHub Actions trong dự án

Dự án VTU Translate Tool đã được cấu hình với GitHub Actions thông qua file `.github/workflows/android.yml`. File này định nghĩa các bước để build APK tự động.

```yaml
name: Android CI

on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle

    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
      
    - name: Build with Gradle
      run: ./gradlew build
      
    - name: Build Debug APK
      run: ./gradlew assembleDebug
      
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

## Cách sử dụng GitHub Actions để build APK

### Phương pháp 1: Tự động build khi push code

Mỗi khi bạn push code lên branch `main`, GitHub Actions sẽ tự động chạy workflow để build APK:

1. Push code lên branch `main`
2. Truy cập tab "Actions" trong repository GitHub
3. Bạn sẽ thấy workflow "Android CI" đang chạy
4. Sau khi workflow hoàn tất, nhấn vào workflow đó
5. Cuộn xuống phần "Artifacts" và tải xuống file `app-debug`
6. Giải nén file tải về để lấy file APK
7. Cài đặt APK trên thiết bị Android của bạn

### Phương pháp 2: Chạy workflow thủ công

Bạn cũng có thể chạy workflow thủ công bất kỳ lúc nào:

1. Truy cập tab "Actions" trong repository GitHub
2. Chọn workflow "Android CI" từ danh sách bên trái
3. Nhấn nút "Run workflow" màu xanh bên phải
4. Chọn branch muốn build từ dropdown menu
5. Nhấn nút "Run workflow" màu xanh
6. Sau khi workflow hoàn tất, nhấn vào workflow đó
7. Cuộn xuống phần "Artifacts" và tải xuống file `app-debug`
8. Giải nén file tải về để lấy file APK
9. Cài đặt APK trên thiết bị Android của bạn

## Tùy chỉnh workflow

Bạn có thể tùy chỉnh workflow bằng cách chỉnh sửa file `.github/workflows/android.yml`. Một số tùy chỉnh phổ biến:

### Build APK Release

Để build APK release thay vì debug, thay đổi lệnh:

```yaml
- name: Build Release APK
  run: ./gradlew assembleRelease
  
- name: Upload APK
  uses: actions/upload-artifact@v3
  with:
    name: app-release
    path: app/build/outputs/apk/release/app-release-unsigned.apk
```

### Ký APK Release

Để ký APK release, bạn cần thêm các bước để cấu hình keystore và ký APK. Tham khảo tài liệu của GitHub Actions để biết thêm chi tiết.

## Xử lý sự cố

### Workflow thất bại

Nếu workflow thất bại, kiểm tra log lỗi trong tab "Actions" để xác định nguyên nhân. Các lỗi phổ biến bao gồm:

- Lỗi biên dịch: Kiểm tra code của bạn
- Lỗi Gradle: Kiểm tra cấu hình Gradle
- Lỗi JDK: Kiểm tra phiên bản JDK trong workflow

### Không tìm thấy APK

Nếu không tìm thấy APK trong artifacts, kiểm tra đường dẫn trong bước "Upload APK" có chính xác không.

## Kết luận

GitHub Actions là một công cụ mạnh mẽ để tự động hóa quy trình build APK cho ứng dụng Android. Bằng cách sử dụng GitHub Actions, bạn có thể tiết kiệm thời gian và đảm bảo quy trình build nhất quán.