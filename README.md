# Daily Task Manager

Aplikasi to-do list Android menggunakan **Kotlin**, **Jetpack Compose**, **Material Design 3**, dan **Room Database**.

## Cara Menjalankan Project

### 1. Clone repository

```bash
git clone https://github.com/USERNAME/NAMA_REPOSITORY.git
cd NAMA_REPOSITORY
```

Ganti `USERNAME` dan `NAMA_REPOSITORY` sesuai link repo GitHub.

### 2. Buka di Android Studio

Buka Android Studio, lalu pilih:

```text
File > Open
```

Pilih folder utama project, yaitu folder yang berisi:

```text
settings.gradle.kts
build.gradle.kts
gradlew
gradlew.bat
```

Jangan buka folder `app` saja.

### 3. Tunggu Gradle Sync

Saat project dibuka, tunggu proses **Gradle Sync** selesai.

Jika muncul tombol:

```text
Sync Now
```

klik tombol tersebut.

### 4. Install SDK jika diminta

Jika Android Studio menampilkan pesan SDK/build tools belum tersedia, klik:

```text
Install missing SDK package(s)
```

Atau buka:

```text
Tools > SDK Manager
```

lalu install SDK yang diminta.

### 5. Jalankan aplikasi

Setelah sync berhasil:

```text
Build > Make Project
```

Lalu pilih emulator/device dan klik:

```text
Run ▶
```

## Catatan

File `local.properties` tidak ikut diupload ke GitHub karena berisi lokasi Android SDK di komputer masing-masing.

Jika muncul error:

```text
SDK location not found
```

buka ulang project lewat Android Studio. Biasanya Android Studio akan membuat file `local.properties` otomatis.

## Fitur Aplikasi

- Tambah task
- Input deadline tanggal dan waktu
- Checkbox status selesai/belum selesai
- Hapus task dengan konfirmasi
- Sorting berdasarkan deadline/status
- Simpan data lokal menggunakan Room Database
