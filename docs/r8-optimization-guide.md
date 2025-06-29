# Hướng dẫn tối ưu hóa R8 và xử lý lỗi OutOfMemoryError

Tài liệu này cung cấp hướng dẫn về cách tối ưu hóa R8 và xử lý lỗi OutOfMemoryError khi build ứng dụng Android.

## Lỗi OutOfMemoryError khi sử dụng R8

Khi build bản release với R8 (minification và obfuscation), bạn có thể gặp lỗi OutOfMemoryError như sau:

```
> Task :app:minifyReleaseWithR8
The Daemon will expire after the build after running out of JVM heap space.
AGPBI: {"kind":"error","text":"java.lang.OutOfMemoryError: Java heap space","sources":[{}],"tool":"R8"}
java.lang.OutOfMemoryError: Java heap space

> Task :app:minifyReleaseWithR8 FAILED
```

Lỗi này xảy ra khi R8 không có đủ bộ nhớ heap để xử lý quá trình minification và obfuscation.

## Giải pháp

### 1. Tăng bộ nhớ heap cho Gradle

Cập nhật file `gradle.properties` để tăng bộ nhớ heap cho JVM:

```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8
```

Trong đó:
- `-Xmx4096m`: Tăng bộ nhớ heap lên 4GB (có thể tăng lên 6GB hoặc 8GB nếu máy tính của bạn có đủ RAM)
- `-XX:MaxMetaspaceSize=512m`: Tăng kích thước Metaspace lên 512MB

### 2. Bật các tùy chọn tối ưu hóa Gradle

Thêm các cấu hình sau vào file `gradle.properties` để tối ưu hóa quá trình build:

```properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
org.gradle.vfs.watch=true
org.gradle.daemon=true
org.gradle.unsafe.configuration-cache=true
org.gradle.unsafe.configuration-cache-problems=warn
```

### 3. Tối ưu hóa cấu hình R8 trong build.gradle

Thêm các cấu hình sau vào file `app/build.gradle`:

```gradle
// Cấu hình R8
androidComponents {
    beforeVariants { variantBuilder ->
        // Áp dụng cấu hình R8 cho tất cả các biến thể
        variantBuilder.enableUnitTest = false
        variantBuilder.enableAndroidTest = false
    }
}

// Tối ưu hóa R8
project.tasks.withType(com.android.build.gradle.internal.tasks.R8Task).configureEach {
    it.maxHeapSize = "2g"
    it.jvmArgs = ["-Xmx2g", "-XX:MaxMetaspaceSize=512m", "-Dfile.encoding=UTF-8"]
}

// Tối ưu hóa Dex
project.tasks.withType(com.android.build.gradle.tasks.DexArchiveBuilderTask).configureEach {
    it.maxHeapSize = "2g"
    it.jvmArgs = ["-Xmx2g", "-XX:MaxMetaspaceSize=512m", "-Dfile.encoding=UTF-8"]
}

// Tối ưu hóa Desugar
project.tasks.withType(com.android.build.gradle.tasks.DexFileDependenciesTask).configureEach {
    it.maxHeapSize = "2g"
    it.jvmArgs = ["-Xmx2g", "-XX:MaxMetaspaceSize=512m", "-Dfile.encoding=UTF-8"]
}
```

### 4. Tối ưu hóa packagingOptions

Cập nhật cấu hình `packagingOptions` trong file `app/build.gradle` để loại bỏ các file không cần thiết:

```gradle
packagingOptions {
    resources {
        excludes += '/META-INF/{AL2.0,LGPL2.1}'
        excludes += '/META-INF/LICENSE*'
        excludes += '/META-INF/NOTICE*'
        excludes += '/META-INF/INDEX.LIST'
        excludes += '/META-INF/DEPENDENCIES'
    }
    jniLibs {
        useLegacyPackaging = false
    }
    dex {
        useLegacyPackaging = false
    }
}
```

### 5. Tối ưu hóa quy tắc ProGuard

Cập nhật file `app/proguard-rules.pro` với các quy tắc tối ưu hóa:

```proguard
# Tối ưu hóa R8
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Tăng hiệu suất R8
-dontpreverify
-repackageclasses ''
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
```

### 5. Tối ưu hóa keep rules cho thư viện

Thay vì giữ lại toàn bộ thư viện, chỉ giữ lại các phần cần thiết:

```proguard
# Tối ưu hóa Compose
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.foundation.** { *; }
```

## Các tùy chọn bổ sung

### Tắt minification cho debug builds

Đảm bảo rằng minification chỉ được bật cho bản release:

```gradle
buildTypes {
    debug {
        minifyEnabled false
        shrinkResources false
    }
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

### Tối ưu hóa R8

Thêm cấu hình sau vào file `gradle.properties`:

```properties
android.enableR8.fullMode=true
android.enableR8.failOnMissingClasses=false
```

**Lưu ý**: Cấu hình `android.enableD8.desugaring=true` đã bị loại bỏ từ Android Gradle Plugin 7.0 và không cần thiết nữa vì D8 desugaring được sử dụng mặc định.

## Xử lý lỗi thiếu các lớp (Missing Classes)

Khi sử dụng R8, bạn có thể gặp lỗi thiếu các lớp như sau:

```
> Task :app:minifyReleaseWithR8
AGPBI: {"kind":"error","text":"Missing classes detected while running R8. Please add the missing classes or apply additional keep rules that are generated in /path/to/app/build/outputs/mapping/release/missing_rules.txt.","sources":[{}]}
AGPBI: {"kind":"error","text":"Missing class com.google.errorprone.annotations.CanIgnoreReturnValue (referenced from: com.google.crypto.tink.KeysetManager com.google.crypto.tink.KeysetManager.add(com.google.crypto.tink.KeyTemplate) and 52 other contexts)\nMissing class com.google.errorprone.annotations.CheckReturnValue (referenced from: com.google.crypto.tink.InsecureSecretKeyAccess and 1 other context)\nMissing class com.google.errorprone.annotations.Immutable (referenced from: com.google.crypto.tink.InsecureSecretKeyAccess and 40 other contexts)\nMissing class com.google.errorprone.annotations.RestrictedApi (referenced from: com.google.crypto.tink.aead.AesEaxKey$Builder com.google.crypto.tink.aead.AesEaxKey.builder() and 6 other contexts)","sources":[{}],"tool":"R8"}

> Task :app:minifyReleaseWithR8 FAILED
```

### Giải pháp

#### 1. Thêm thư viện error-prone-annotations

Thêm thư viện error-prone-annotations vào file `app/build.gradle`:

```gradle
dependencies {
    // Các phụ thuộc khác...
    
    // Error Prone Annotations (cần thiết cho R8)
    implementation 'com.google.errorprone:error_prone_annotations:2.18.0'
}
```

#### 2. Thêm quy tắc ProGuard cho error-prone-annotations

Thêm các quy tắc sau vào file `app/proguard-rules.pro`:

```proguard
# Keep Error Prone Annotations
-dontwarn com.google.errorprone.annotations.**
-keep class com.google.errorprone.annotations.** { *; }
-keep @com.google.errorprone.annotations.Immutable class * { *; }
-keep @com.google.errorprone.annotations.CanIgnoreReturnValue class * { *; }
-keep @com.google.errorprone.annotations.CheckReturnValue class * { *; }
-keep @com.google.errorprone.annotations.RestrictedApi class * { *; }
```

#### 3. Sử dụng file missing_rules.txt

Nếu vẫn gặp lỗi thiếu các lớp khác, bạn có thể sử dụng file `missing_rules.txt` được tạo ra trong thư mục `app/build/outputs/mapping/release/` để thêm các quy tắc ProGuard cần thiết.

## Xử lý sự cố OutOfMemoryError

Nếu vẫn gặp lỗi OutOfMemoryError sau khi áp dụng các giải pháp trên, hãy thử:

1. **Tăng bộ nhớ heap hơn nữa**: Tăng `-Xmx` lên 6GB hoặc 8GB nếu máy tính của bạn có đủ RAM
2. **Giảm số lượng thư viện**: Xem xét loại bỏ các thư viện không cần thiết
3. **Sử dụng ProGuard thay vì R8**: Trong một số trường hợp, ProGuard có thể sử dụng ít bộ nhớ hơn R8
4. **Tắt shrinkResources**: Nếu vẫn gặp vấn đề, hãy thử tắt `shrinkResources`
5. **Tắt cấu hình không an toàn**: Nếu gặp lỗi với `org.gradle.unsafe.configuration-cache`, hãy thử tắt cấu hình này
6. **Tắt R8 full mode**: Nếu gặp lỗi với `android.enableR8.fullMode`, hãy thử đặt giá trị này thành `false`
7. **Sử dụng máy tính có nhiều RAM hơn**: Nếu đang build trên máy tính có ít RAM, hãy thử build trên máy tính có nhiều RAM hơn hoặc sử dụng dịch vụ CI/CD như GitHub Actions
8. **Tắt các ứng dụng khác**: Đóng các ứng dụng không cần thiết để giải phóng bộ nhớ cho quá trình build

## Kết luận

Việc tối ưu hóa R8 và xử lý lỗi OutOfMemoryError đòi hỏi sự cân bằng giữa hiệu suất build và chất lượng của APK đầu ra. Các giải pháp trên sẽ giúp bạn xử lý hầu hết các trường hợp gặp lỗi OutOfMemoryError khi sử dụng R8.