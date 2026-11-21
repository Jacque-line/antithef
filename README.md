# AntiTheft – Smart Phone Security 🛡️📱

Aplikasi keamanan smartphone berbasis Android tingkat menengah yang dirancang untuk melindungi perangkat dari perpindahan, pengambilan, atau sentuhan tanpa izin menggunakan sensor **Accelerometer** dan **Proximity Sensor** secara real-time dengan penyimpanan riwayat persisten pada **SQLite Database**.

---

## 1. Deskripsi Aplikasi

**AntiTheft** memungkinkan pengguna meletakkan HP di atas meja, lemari, atau tempat tertentu dan mengaktifkan mode proteksi (*ARM*). Ketika mode keamanan aktif, sistem tripwire akan memantau perubahan akselerasi dan objek di sekitar sensor depan. Jika perangkat dipindahkan, diangkat, atau disentuh melewati ambang sensitivitas yang telah dikonfigurasi, alarm sirene frekuensi tinggi akan berbunyi otomatis, notifikasi peringatan ditampilkan melalui Foreground Service, dan seluruh data insiden dicatat ke dalam database SQLite.

---

## 2. Fitur Utama & Tambahan

### A. Fitur Utama
1. **Arm / Disarm Security Mode**
   - Mengaktifkan (*ARM*) dan menonaktifkan (*DISARM*) proteksi secara instan.
   - Dilengkapi *grace countdown* (3 detik) agar pengguna dapat meletakkan HP di meja dengan tenang tanpa memicu alarm palsu.
   - Status visual real-time (Hijau = Aman, Kuning = Countdown, Merah = Alarm Berbunyi).
2. **Motion Detection (Accelerometer)**
   - Mengukur akselerasi 3-sumbu ($X, Y, Z$) dan magnitudo $A = \sqrt{x^2 + y^2 + z^2}$.
   - Mengukur perubahan kecepatan/laju pergerakan $\Delta A$.
   - Memicu alarm saat perubahan melampaui ambang batas sensitivitas (*dynamic threshold*).
3. **Proximity Detection (Proximity Sensor)**
   - Mendeteksi objek atau tangan yang mendekat / menutupi bagian depan layar.
   - Menangani ketersediaan perangkat (graceful fallback jika HP tidak memiliki proximity hardware).
   - Dapat diaktifkan atau dinonaktifkan secara fleksibel melalui pengaturan.
4. **Alarm & Siren System**
   - Memutar audio sirene peringatan keamanan multi-harmonik (sweeping frequency 700Hz – 1600Hz) secara looping melalui `AudioTrack`.
   - Mengaktifkan pola getar dinamis (*vibration pulse*).
   - Dilengkapi tombol **STOP ALARM** dan **Test Alarm Sound**.
5. **Security History & Incident Logging**
   - Riwayat kejadian tersimpan permanen di SQLite.
   - Mencatat waktu insiden, jenis sensor pemicu (Motion / Proximity / System), deskripsi detail, dan status alarm.

### B. Fitur Tambahan
- **Sensitivity Settings (1% – 100%):** Mengatur kepekaan deteksi gerak (Ultra, High, Medium, Low).
- **Alarm Volume Controller (0% – 100%):** Mengatur volume audio sirene peringatan.
- **Test Alarm Sound:** Tombol instan untuk menguji speaker dan volume sirene selama 2 detik.
- **Hardware Sensor Status:** Indikator operasional dan ketersediaan hardware sensor pada perangkat.
- **Security Statistics:** Ringkasan statistik jumlah total insiden dan frekuensi alarm terpicu.
- **Clear History:** Pembersihan riwayat log keamanan dari SQLite dengan dialog konfirmasi.
- **Sensor Simulation Triggers:** Tombol simulasi pergerakan dan halangan pada layar Monitor untuk kemudahan demonstrasi pengujian saat presentasi.
- **Background Foreground Service:** Sistem pemantauan tetap berjalan aktif di latar belakang saat aplikasi diminimalkan atau layar mati, lengkap dengan notifikasi persisten status sistem.

---

## 3. Teknologi yang Digunakan

| Komponen | Teknologi |
| :--- | :--- |
| **Bahasa Pemrograman** | Kotlin (2.2+) |
| **UI Framework** | Jetpack Compose (BOM 2026.02+) |
| **Desain UI** | Material 3 (Modern Dark Security Palette) |
| **Arsitektur** | MVVM (Model-View-ViewModel) + Repository Pattern |
| **Manajemen State** | Kotlin StateFlow & Coroutines |
| **Database** | SQLite via `SQLiteOpenHelper` (PRAGMA foreign keys active) |
| **Sensor API** | Android `SensorManager` & `SensorEventListener` |
| **Audio Engine** | Real-time PCM Synthesizer via Android `AudioTrack` |
| **Background Processing**| Android `ForegroundService` dengan Notification Channel |
| **Navigasi** | Navigation Compose & Material 3 NavigationBar |

---

## 4. Sensor yang Digunakan

### 1. Accelerometer (`Sensor.TYPE_ACCELEROMETER`)
- **Kategori:** Motion Sensor
- **Fungsi:** Mengukur akselerasi fisik pada 3 sumbu:
  - **X:** Gerakan lateral (kiri - kanan).
  - **Y:** Gerakan vertikal (atas - bawah).
  - **Z:** Gerakan tegak lurus layar (depan - belakang).
- **Rumus Magnitudo:**
  $$A = \sqrt{x^2 + y^2 + z^2}$$
- **Rumus Perubahan Kecepatan:**
  $$\Delta A = |A_{\text{sekarang}} - A_{\text{sebelumnya}}|$$
- **Formula Ambang Sensitivitas:**
  $$\text{Threshold} = 1.0 + (100 - \text{Sensitivitas}) \times \frac{7.0}{99.0}$$

### 2. Proximity Sensor (`Sensor.TYPE_PROXIMITY`)
- **Kategori:** Position Sensor
- **Fungsi:** Mendeteksi ada atau tidaknya objek yang menutupi bagian sensor depan ponsel (jarak mendekati 0 cm / status `NEAR`).

---

## 5. Struktur Database SQLite (`antitheft_database.db`)

```sql
-- 1. Tabel users
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL,
    created_at TEXT
);

-- 2. Tabel security_sessions
CREATE TABLE security_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    start_time TEXT NOT NULL,
    end_time TEXT,
    status TEXT NOT NULL,
    total_events INTEGER DEFAULT 0,
    FOREIGN KEY(user_id) REFERENCES users(id)
);

-- 3. Tabel security_logs
CREATE TABLE security_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER,
    event_type TEXT NOT NULL,
    description TEXT,
    timestamp TEXT NOT NULL,
    alarm_triggered INTEGER DEFAULT 0,
    FOREIGN KEY(session_id) REFERENCES security_sessions(id)
);

-- 4. Tabel settings
CREATE TABLE settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    motion_sensitivity INTEGER DEFAULT 50,
    proximity_enabled INTEGER DEFAULT 1,
    alarm_enabled INTEGER DEFAULT 1,
    alarm_volume INTEGER DEFAULT 80
);
```

---

## 6. Struktur Folder Proyek (MVVM)

```
app/src/main/java/com/example/antithef/
│
├── data/
│   ├── local/
│   │   └── AppDatabaseHelper.kt       # SQLiteOpenHelper & DDL
│   ├── dao/
│   │   └── SecurityDao.kt             # Query CRUD Database
│   ├── model/
│   │   ├── User.kt                    # Entity Pengguna
│   │   ├── SecuritySession.kt         # Entity Sesi Keamanan
│   │   ├── SecurityLog.kt             # Entity Log Insiden
│   │   ├── AppSettings.kt             # Entity Pengaturan
│   │   └── SensorReading.kt           # Entity Nilai Sensor Real-time
│   └── repository/
│       └── SecurityRepository.kt      # Abstraksi Sumber Data & Dispatcher IO
│
├── sensor/
│   ├── MotionDetector.kt              # Handler Sensor Accelerometer
│   ├── ProximityDetector.kt           # Handler Sensor Proximity
│   └── SensorManagerHelper.kt         # Koordinator Pembacaan Sensor
│
├── service/
│   ├── AlarmSoundPlayer.kt            # Synthesizer Sirene AudioTrack & Vibrator
│   └── SecurityForegroundService.kt   # Layanan Latar Belakang & Notifikasi
│
├── ui/
│   ├── components/
│   │   ├── SecurityShield.kt          # Indikator visual shield animasi
│   │   ├── ArmDisarmButton.kt         # Tombol ARM/DISARM interaktif
│   │   ├── SensorStatusCard.kt        # Kartu status ketersediaan sensor
│   │   ├── SecurityStatCard.kt        # Kartu metrik statistik
│   │   └── BottomNavBar.kt            # Navigasi tab bawah Material 3
│   ├── screens/
│   │   ├── SplashScreen.kt            # Splash screen animasi AntiTheft
│   │   ├── DashboardScreen.kt         # Dashboard utama ARM/DISARM
│   │   ├── MonitorScreen.kt           # Telemetri live sensor & rumus
│   │   ├── AlarmScreen.kt             # Layar peringatan merah bahaya
│   │   ├── HistoryScreen.kt           # Riwayat kejadian & filter log
│   │   └── SettingsScreen.kt          # Pengaturan sensitivitas, sirene & DB
│   ├── navigation/
│   │   └── AppNavigation.kt           # Scaffold & pengatur transisi layar
│   └── theme/
│       ├── Color.kt                   # Modern Dark Security Theme Palette
│       ├── Theme.kt                   # Konfigurasi MaterialTheme
│       └── Type.kt                    # Tipografi
│
├── viewmodel/
│   └── SecurityViewModel.kt           # StateFlow & Business Logic MVVM
│
└── MainActivity.kt                    # Entry Point & Izin Notifikasi
```

---

## 7. Checklist Pengujian (12/12 Berhasil)

| No | Skenario Pengujian | Status |
|:---:|:---|:---:|
| 1 | Aplikasi dapat dibuka tanpa error dan menampilkan Splash Screen | ✅ Lulus |
| 2 | Status ketersediaan Accelerometer terdeteksi dengan benar | ✅ Lulus |
| 3 | Status ketersediaan Proximity Sensor terdeteksi dengan benar | ✅ Lulus |
| 4 | Tombol ARM dapat mengaktifkan mode keamanan dengan grace countdown | ✅ Lulus |
| 5 | Tombol DISARM dapat menonaktifkan keamanan dan menghentikan alarm | ✅ Lulus |
| 6 | Gerakan HP dapat memicu deteksi pergerakan (Motion Detection) | ✅ Lulus |
| 7 | Alarm berbunyi otomatis dan perangkat bergetar saat deteksi terpicu | ✅ Lulus |
| 8 | Tombol STOP ALARM dapat menghentikan suara sirene seketika | ✅ Lulus |
| 9 | Riwayat kejadian tersimpan secara otomatis ke database SQLite | ✅ Lulus |
| 10 | Riwayat tetap tersedia utuh setelah aplikasi ditutup dan dibuka kembali | ✅ Lulus |
| 11 | Pengaturan sensitivitas dan volume tersimpan ke tabel `settings` | ✅ Lulus |
| 12 | Menangani HP yang tidak memiliki Proximity Sensor secara aman (*no crash*) | ✅ Lulus |

---

## 8. Cara Menjalankan Aplikasi

1. Buka folder proyek ini di **Android Studio**.
2. Tunggu proses **Gradle Sync** selesai secara otomatis.
3. Hubungkan smartphone Android melalui kabel USB (aktifkan USB Debugging) atau gunakan Android Virtual Device (AVD).
4. Klik tombol **Run 'app'** (`Shift + F10`) atau jalankan perintah CLI:
   ```bash
   ./gradlew assembleDebug
   ```
   *File APK debug akan dihasilkan pada direktori:*  
   `app/build/outputs/apk/debug/app-debug.apk`
5. Berikan izin notifikasi saat pertama kali aplikasi dibuka untuk mengaktifkan notifikasi Foreground Service.

---

## 9. Developer Info

- **Aplikasi:** AntiTheft – Smart Phone Security
- **Platform:** Android (Kotlin, Jetpack Compose, SQLite, MVVM)
- **Kategori:** Security / Utility
- **Tingkat:** Menengah
