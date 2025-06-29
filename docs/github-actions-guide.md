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
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle

    # Cài đặt Gradle
    - name: Setup Gradle
      uses: gradle/gradle-build-action@v2
      
    # Sử dụng Gradle từ môi trường GitHub Actions
    - name: Build with Gradle
      run: gradle build
      
    - name: Build Debug APK
      run: gradle assembleDebug
      
    - name: Upload APK
      uses: actions/upload-artifact@v4
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
  run: gradle assembleRelease
  
- name: Upload APK
  uses: actions/upload-artifact@v4
  with:
    name: app-release
    path: app/build/outputs/apk/release/app-release-unsigned.apk
```

## Phiên bản và Cấu hình

### Sử dụng Gradle thay vì Gradle Wrapper

Trong workflow của chúng tôi, chúng tôi sử dụng lệnh `gradle` trực tiếp thay vì `./gradlew` vì:

1. **Tính sẵn có**: Môi trường GitHub Actions đã có sẵn Gradle được cài đặt, do đó không cần phải sử dụng Gradle Wrapper.

2. **Tránh lỗi quyền thực thi**: Sử dụng `gradle` trực tiếp giúp tránh lỗi "No such file or directory" khi thực hiện lệnh `chmod +x gradlew` trong trường hợp file gradlew không tồn tại.

3. **Đơn giản hóa workflow**: Giảm số lượng bước cần thiết để build ứng dụng.

Nếu bạn gặp lỗi "chmod: cannot access 'gradlew': No such file or directory", hãy sử dụng `gradle` thay vì `./gradlew` trong file workflow của bạn.

### Cập nhật từ v3 lên v4

Lưu ý rằng chúng tôi đã cập nhật các actions từ phiên bản v3 lên v4 vì:

1. **Thông báo loại bỏ**: GitHub đã thông báo rằng từ ngày 30/01/2025, v3 của actions/upload-artifact và actions/download-artifact sẽ không còn được hỗ trợ.

2. **Cải thiện hiệu suất**: Phiên bản v4 cải thiện tốc độ tải lên và tải xuống lên đến 98% và bao gồm nhiều tính năng mới.

3. **Tính nhất quán**: Chúng tôi đã cập nhật tất cả các actions (checkout, setup-java, upload-artifact) lên phiên bản v4 để đảm bảo tính nhất quán.

Nếu bạn gặp lỗi "This request has been automatically failed because it uses a deprecated version of `actions/upload-artifact: v3`", hãy cập nhật phiên bản của actions trong file workflow của bạn lên v4.

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