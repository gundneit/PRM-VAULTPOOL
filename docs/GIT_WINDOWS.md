# Git trên Windows — commit Android (lỗi path dài / `build/`)

## Lỗi "Filename too long"

Git đang cố **commit thư mục `build/`** (file Gradle tạo ra). Đường dẫn rất dài → Windows/Git báo lỗi.

**Không commit `build/`** — đã có trong `.gitignore` (`**/build/`).

### Bước 1 — Bật long paths (một lần)

Trong terminal có `git` (Git Bash, Android Studio Terminal, hoặc CMD sau khi cài Git):

```bat
git config core.longpaths true
```

Hoặc cho mọi repo:

```bat
git config --global core.longpaths true
```

Repo này có thể đã set `longpaths = true` trong `.git/config` local.

### Bước 2 — Gỡ `build/` khỏi Git (chỉ index, không xóa file trên máy)

Từ thư mục gốc repo:

```bat
cd C:\Users\HP\Documents\GitHub\PRM-VAULTPOOL
git rm -r --cached apps\android-customer\build
```

(Lặp tương tự nếu còn folder `build` khác đang bị track.)

### Bước 3 — Commit lại

Chỉ chọn file nguồn (`.java`, `.xml`, `.gradle`, `.gitignore`, …). **Không** tick `build/`.

---

## `git` không nhận trong PowerShell

Cài [Git for Windows](https://git-scm.com/download/win) và chọn **add to PATH**, hoặc dùng **Terminal trong Android Studio** (thường đã trỏ tới `git.exe`).

---

## LF / CRLF

Đã có `.gitattributes` (`* text=auto`). Cảnh báo LF→CRLF trên file trong `build/` sẽ hết khi không còn stage `build/`.
