# Hướng dẫn khắc phục lỗi Gradle

## Lỗi khóa tệp lịch sử thực thi (Execution History Lock)

Nếu bạn gặp lỗi tương tự như sau:

```
Timeout waiting to lock execution history cache. It is currently in use by another Gradle instance.
Owner PID: xxxx
Our PID: xxxx
```

Đây là lỗi xảy ra khi có nhiều tiến trình Gradle đang chạy đồng thời và cố gắng truy cập vào cùng một tệp khóa lịch sử thực thi.

### Cách khắc phục

1. **Giải pháp tạm thời**:
   - Tắt tất cả các tiến trình Gradle đang chạy
   - Xóa thư mục `.gradle` trong thư mục dự án
   - Khởi động lại IDE

2. **Giải pháp cấu hình**:
   Đã thêm các cấu hình sau vào `gradle.properties` để ngăn chặn lỗi:
   ```properties
   org.gradle.unsafe.configuration-cache=false
   org.gradle.daemon=false
   org.gradle.unsafe.isolated-projects=true
   ```

## Lỗi R8

Nếu bạn gặp lỗi liên quan đến R8 trong quá trình build, hãy kiểm tra cấu hình trong `gradle.properties`:

```properties
android.enableR8=true
```

Cấu hình này phải phù hợp với cấu hình trong `app/build.gradle` nếu bạn đang sử dụng minification:

```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

## Tối ưu hóa hiệu suất Gradle

Để tối ưu hóa hiệu suất build, bạn có thể sử dụng các cấu hình sau trong `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
```

Tuy nhiên, nếu bạn gặp lỗi khóa tệp lịch sử thực thi, hãy tắt một số tính năng này như đã hướng dẫn ở trên.