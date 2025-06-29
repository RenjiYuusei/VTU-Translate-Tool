# Hướng dẫn thiết lập Keystore cho GitHub Actions

## Mã hóa Keystore

Để sử dụng file Keystore (Yuusei.jks) trong GitHub Actions, bạn cần mã hóa nó thành chuỗi Base64 và lưu trữ trong GitHub Secrets. Hãy làm theo các bước sau:

### Trên Windows

1. Mở PowerShell và di chuyển đến thư mục chứa file Yuusei.jks
2. Chạy lệnh sau để mã hóa file thành Base64:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("Yuusei.jks")) | Out-File -Encoding ASCII keystore_base64.txt
```

3. Mở file `keystore_base64.txt` và sao chép toàn bộ nội dung

### Trên Linux/macOS

1. Mở Terminal và di chuyển đến thư mục chứa file Yuusei.jks
2. Chạy lệnh sau để mã hóa file thành Base64:

```bash
base64 Yuusei.jks > keystore_base64.txt
```

3. Mở file `keystore_base64.txt` và sao chép toàn bộ nội dung

## Thêm vào GitHub Secrets

1. Truy cập vào repository GitHub của bạn
2. Chọn "Settings" > "Secrets and variables" > "Actions"
3. Nhấn "New repository secret"
4. Đặt tên là `KEYSTORE_BASE64`
5. Dán nội dung Base64 đã sao chép vào ô "Value"
6. Nhấn "Add secret"

## Xác nhận cấu hình

Sau khi hoàn thành các bước trên, GitHub Actions sẽ tự động sử dụng keystore này để ký ứng dụng khi build bản release. Bạn có thể kiểm tra workflow trong file `.github/workflows/android.yml` để xem cấu hình chi tiết.

## Lưu ý bảo mật

- Không bao giờ commit trực tiếp file keystore vào repository
- Không chia sẻ chuỗi Base64 của keystore với người khác
- Hãy đảm bảo rằng bạn lưu trữ file keystore gốc ở một nơi an toàn, vì nếu mất nó, bạn sẽ không thể cập nhật ứng dụng trên Google Play Store