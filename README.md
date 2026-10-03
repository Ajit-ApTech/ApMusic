# 🎧 ApMusic

A modern, fast, and completely free Android music player app built from scratch with **Kotlin** and **Jetpack Compose**. 

Stream music online, download tracks for offline listening, and effortlessly play your local audio files — all without subscriptions, ads, or paywalls.

---

## ✨ Features

- **🌐 Online Streaming**: Browse and stream music directly with high-quality playback.
- **📥 1-Tap Downloads**: Save songs directly to your device storage for offline playback anytime, anywhere.
- **📂 Local Music Player**: Full device storage integration to scan, browse, and play your existing MP3s and audio files.
- **🎛️ Background Playback**: Seamless audio playback using Android Media3/ExoPlayer with lock screen and notification controls.
- **🎨 Modern Dark UI**: Sleek, immersive design crafted with Jetpack Compose and Material 3 design guidelines.
- **🚫 Zero Ads & Subscriptions**: Clean, uninterrupted listening experience.

---

## 🛠️ Tech Stack & Architecture

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) & Material 3
- **Audio Engine:** [AndroidX Media3 (ExoPlayer)](https://developer.android.com/guide/topics/media/media3)
- **Architecture:** MVVM + Clean Architecture + Unidirectional Data Flow (UDF)
- **Dependency Injection:** [Hilt](https://dagger.dev/hilt/)
- **Local Database:** [Room](https://developer.android.com/training/data-storage/room)
- **Local Storage Integration:** Android MediaStore API
- **Async & Concurrency:** Kotlin Coroutines & StateFlow
- **Networking & Image Loading:** Retrofit, OkHttp & Coil

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug (2024.2+) or newer
- JDK 17 or higher
- Android SDK with `minSdk 24` and `targetSdk 34`

### Installation & Build

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Ajit-ApTech/ApMusic.git
   cd ApMusic
   ```

2. **Open in Android Studio:**
   - Launch Android Studio and select **Open**.
   - Navigate to the cloned folder and let Gradle sync.

3. **Build and Run:**
   - Run via Android Studio or using Gradle from terminal:
     ```bash
     ./gradlew assembleDebug
     ```
   - The generated APK will be in `app/build/outputs/apk/debug/`.

---

## 📄 License & Disclaimer

This project is built and maintained for educational and personal use. All streamed content rights belong to their respective creators and copyright owners.
