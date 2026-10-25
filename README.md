# Filee for Android

> **Offline-first, privacy-respecting universal file converter for Android.**
> Adapted from the open-source [KnifeLemon/Filee](https://github.com/KnifeLemon/Filee) desktop project (MIT License).

---

## 🚀 Overview

**Filee** brings the signature offline conversion power and innovative UX of the Windows desktop application to native Android. It converts files across **250+ format combinations** completely on-device, with zero cloud uploads and zero tracking.

### Key Capabilities

1. **Smart Multi-Hop Route Planner**
   - Automatically computes direct conversions and multi-hop transition routes (up to 3 hops) using a Breadth-First Search (BFS) graph.
   - Example 1: `CSV ➔ HTML ➔ PDF`
   - Example 2: `Markdown ➔ PDF ➔ PNG`
   - Example 3: `XML ➔ JSON ➔ YAML`
   - Example 4: `SVG ➔ PNG ➔ WebP`
   - Displays hop count, intermediate formats, and fidelity estimates.

2. **Signature "Donut" Radial Selector**
   - Interactive touch-friendly radial selector inspired by Filee's desktop donut drop menu.
   - Categorized into **Images**, **Documents**, **Data & Code**, **Audio & Voice**, **Archives**, and **Hashes/Binary**.
   - Dynamically highlights only valid and reachable target formats for the loaded file.

3. **100% Offline Processing Engine**
   - **Images**: PNG, JPEG (configurable quality), WebP (lossy/lossless), BMP (24-bit uncompressed), ICO (Windows icon format), SVG rasterization, GIF frame extraction.
   - **Documents**: PDF generation via Android `PdfDocument`, PDF page rasterization via `PdfRenderer`, Markdown conversion to HTML/PDF/Plain Text, HTML parsing, RTF generation and stripping.
   - **Data & Tabular**: CSV, TSV, JSON, XML, YAML with bidirectional parsing and auto-formatting.
   - **Audio & Speech**: Offline vocal formant synthesis into 16-bit PCM WAV audio, audio telemetry metadata extraction (sample rate, bit depth, duration, ID3 tags), WAV/MP3 container wrapping.
   - **Archives & Compression**: Standard ZIP multi-file bundle creation, GZIP compression, and ZIP manifest inspection.
   - **Cryptographic & Binary**: SHA-256, MD5, CRC32, Base64 encoder/decoder, and formatted Hex byte dumps.

4. **Multi-File Batch Queue**
   - Queue multiple files at once.
   - One-tap "Convert All" background runner with individual progress and size reduction tracking.
   - One-tap "Load Sample Pack" for immediate testing without external files.

5. **Local History & Metrics (Room Database)**
   - Persists all conversion records, source/target file sizes, duration in milliseconds, and executed route paths.
   - Re-open or share converted files via Android's secure `FileProvider`.
   - Star favorite conversions for quick reference.

6. **System Integration & Privacy**
   - Registered `ACTION_SEND` and `ACTION_SEND_MULTIPLE` intent filters to receive files shared directly from browsers, photos, or messaging apps.
   - Zero internet permissions requested in `AndroidManifest.xml`.

---

## 🛠 Architecture & Tech Stack

* **Language**: Kotlin 2.2
* **UI**: Jetpack Compose with Material Design 3 (M3)
* **Design Philosophy**: Cyber Slate & Electric Cyan high-contrast dark theme
* **Architecture**: MVVM with Repository pattern (`FileeViewModel` ➔ `ConversionRepository` ➔ `ConversionEngine` & Room `ConversionRecordDao`)
* **Persistence**: AndroidX Room 2.7 with KSP symbol processing
* **Image Loading**: Coil Compose 2.7
* **Testing**: JUnit 4 & Robolectric 4.16

---

## 📱 Build & Test Instructions

### Build Debug APK
```bash
gradle assembleDebug
```
The resulting APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Run Local Unit Tests
```bash
gradle :app:testDebugUnitTest
```

---

## 📄 License

Original project by **KnifeLemon** ([KnifeLemon/Filee](https://github.com/KnifeLemon/Filee)), licensed under the **MIT License**.
Android implementation adapted and optimized for mobile devices.
