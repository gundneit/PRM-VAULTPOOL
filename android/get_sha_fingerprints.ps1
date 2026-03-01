# Script để lấy SHA-1 và SHA-256 fingerprints cho Firebase

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Firebase SHA Fingerprints Tool" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Lấy SHA từ debug keystore
Write-Host "1. SHA từ Debug Keystore:" -ForegroundColor Yellow
Write-Host ""

$debugKeystore = "$env:USERPROFILE\.android\debug.keystore"

if (Test-Path $debugKeystore) {
    Write-Host "   Đang lấy SHA từ: $debugKeystore" -ForegroundColor Gray
    Write-Host ""
    
    # SHA-1
    Write-Host "   SHA-1:" -ForegroundColor Green
    keytool -list -v -keystore $debugKeystore -alias androiddebugkey -storepass android -keypass android | Select-String -Pattern "SHA1:" | ForEach-Object {
        $sha1 = ($_ -split "SHA1:")[1].Trim()
        Write-Host "   $sha1" -ForegroundColor White
        Write-Host ""
        Write-Host "   Copy SHA-1 này vào Firebase Console!" -ForegroundColor Yellow
    }
    
    Write-Host ""
    
    # SHA-256
    Write-Host "   SHA-256:" -ForegroundColor Green
    keytool -list -v -keystore $debugKeystore -alias androiddebugkey -storepass android -keypass android | Select-String -Pattern "SHA256:" | ForEach-Object {
        $sha256 = ($_ -split "SHA256:")[1].Trim()
        Write-Host "   $sha256" -ForegroundColor White
        Write-Host ""
        Write-Host "   Copy SHA-256 này vào Firebase Console!" -ForegroundColor Yellow
    }
} else {
    Write-Host "   Debug keystore không tìm thấy tại: $debugKeystore" -ForegroundColor Red
    Write-Host "   Hãy chạy app một lần để tạo debug keystore." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Cách thêm vào Firebase:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Vào Firebase Console: https://console.firebase.google.com/" -ForegroundColor White
Write-Host "2. Chọn project của bạn" -ForegroundColor White
Write-Host "3. Project Settings (icon ⚙️)" -ForegroundColor White
Write-Host "4. Scroll xuống 'Your apps'" -ForegroundColor White
Write-Host "5. Tìm app Android (com.lavelahotel.poolbooking)" -ForegroundColor White
Write-Host "6. Click 'Add fingerprint'" -ForegroundColor White
Write-Host "7. Paste SHA-1 → Save" -ForegroundColor White
Write-Host "8. Click 'Add fingerprint' lần nữa" -ForegroundColor White
Write-Host "9. Paste SHA-256 → Save" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
