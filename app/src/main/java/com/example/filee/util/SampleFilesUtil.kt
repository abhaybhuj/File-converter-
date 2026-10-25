package com.example.filee.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.filee.data.model.FileFormat
import java.io.ByteArrayOutputStream

data class SampleFileItem(
    val name: String,
    val format: FileFormat,
    val description: String,
    val bytes: ByteArray
)

object SampleFilesUtil {

    fun getSampleFiles(): List<SampleFileItem> {
        return listOf(
            SampleFileItem(
                name = "financial_summary.csv",
                format = FileFormat.CSV,
                description = "Tabular balance sheet with quarterly revenue and profits",
                bytes = """
                    Quarter,Revenue_USD,Operating_Cost_USD,Net_Profit_USD,Growth_Rate
                    Q1_2025,1240000,810000,430000,14.2%
                    Q2_2025,1420000,905000,515000,19.7%
                    Q3_2025,1680000,1020000,660000,28.1%
                    Q4_2025,1950000,1150000,800000,32.5%
                """.trimIndent().toByteArray(Charsets.UTF_8)
            ),
            SampleFileItem(
                name = "architecture_notes.md",
                format = FileFormat.MD,
                description = "Formatted markdown report with headings, lists, and code",
                bytes = """
                    # Filee Engine Architecture
                    
                    Filee is an **offline-first universal file converter** that operates without remote cloud dependency.
                    
                    ## Key Innovations
                    - **Smart Route Planner**: Computes multi-step conversions up to 3 hops.
                    - **Donut Radial Selector**: Interactive category arc menu.
                    - **Zero Cloud Leakage**: All bytes stay strictly on your device.
                    
                    ```kotlin
                    val route = RoutePlanner.findRoute(FileFormat.CSV, FileFormat.PDF)
                    // Computes: CSV ➔ HTML ➔ PDF
                    ```
                    
                    *Enjoy offline productivity!*
                """.trimIndent().toByteArray(Charsets.UTF_8)
            ),
            SampleFileItem(
                name = "app_manifest.json",
                format = FileFormat.JSON,
                description = "Structured JSON tree with nested properties and arrays",
                bytes = """
                    {
                      "appName": "Filee",
                      "version": "1.0.0",
                      "offlineMode": true,
                      "supportedEngines": ["Image", "Document", "Data", "Audio", "Archive"],
                      "settings": {
                        "defaultQuality": 85,
                        "theme": "DarkCyberSlate",
                        "maxRouteHops": 3
                      }
                    }
                """.trimIndent().toByteArray(Charsets.UTF_8)
            ),
            SampleFileItem(
                name = "vector_badge.svg",
                format = FileFormat.SVG,
                description = "Scalable Vector Graphics emblem with circle and badge paths",
                bytes = """
                    <svg xmlns="http://www.w3.org/2000/svg" width="400" height="400" viewBox="0 0 400 400">
                      <rect width="400" height="400" fill="#0F172A" rx="32"/>
                      <circle cx="200" cy="200" r="140" fill="none" stroke="#38BDF8" stroke-width="16"/>
                      <circle cx="200" cy="200" r="90" fill="#34D399"/>
                      <text x="200" y="215" font-size="28" font-family="sans-serif" font-weight="bold" text-anchor="middle" fill="#FFFFFF">FILEE</text>
                    </svg>
                """.trimIndent().toByteArray(Charsets.UTF_8)
            ),
            SampleFileItem(
                name = "speech_transcript.txt",
                format = FileFormat.TXT,
                description = "Plain text quote ready for audio TTS waveform or PDF generation",
                bytes = "Filee converts your files instantly on your device with complete privacy and zero data tracking.".toByteArray(Charsets.UTF_8)
            ),
            SampleFileItem(
                name = "sample_graphic.png",
                format = FileFormat.PNG,
                description = "High resolution colorful sample graphic image",
                bytes = generateSampleImagePng()
            )
        )
    }

    private fun generateSampleImagePng(): ByteArray {
        val width = 480
        val height = 480
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient background
        val bgPaint = Paint().apply { color = Color.parseColor("#0F172A") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Accent shapes
        val cyanPaint = Paint().apply {
            color = Color.parseColor("#38BDF8")
            isAntiAlias = true
        }
        val emeraldPaint = Paint().apply {
            color = Color.parseColor("#34D399")
            isAntiAlias = true
        }
        val indigoPaint = Paint().apply {
            color = Color.parseColor("#818CF8")
            isAntiAlias = true
        }

        canvas.drawCircle(180f, 200f, 100f, indigoPaint)
        canvas.drawCircle(300f, 260f, 90f, cyanPaint)
        canvas.drawCircle(220f, 320f, 70f, emeraldPaint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 34f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("FILEE SAMPLE", width / 2f, 120f, textPaint)

        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        return out.toByteArray()
    }
}
