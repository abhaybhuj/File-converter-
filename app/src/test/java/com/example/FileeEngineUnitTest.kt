package com.example

import com.example.filee.data.engine.ArchiveConverter
import com.example.filee.data.engine.DataConverter
import com.example.filee.data.engine.DocumentConverter
import com.example.filee.data.engine.RoutePlanner
import com.example.filee.data.model.ConversionOptions
import com.example.filee.data.model.FileFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FileeEngineUnitTest {

    @Test
    fun testDirectRoutePlanner() {
        val route = RoutePlanner.findRoute(FileFormat.PNG, FileFormat.JPEG)
        assertNotNull(route)
        assertEquals(true, route!!.isDirect)
        assertEquals(2, route.hops.size)
        assertEquals(FileFormat.PNG, route.hops.first())
        assertEquals(FileFormat.JPEG, route.hops.last())
    }

    @Test
    fun testMultiHopRoutePlanner() {
        // Multi-hop: XML to YAML via JSON
        val route = RoutePlanner.findRoute(FileFormat.XML, FileFormat.YAML)
        assertNotNull(route)
        assertEquals(FileFormat.XML, route!!.inputFormat)
        assertEquals(FileFormat.YAML, route.outputFormat)
        assertTrue(route.hops.size >= 2)
    }

    @Test
    fun testAvailableTargetFormats() {
        val targets = RoutePlanner.getAvailableTargetFormats(FileFormat.CSV)
        assertTrue(targets.contains(FileFormat.JSON))
        assertTrue(targets.contains(FileFormat.PDF))
        assertTrue(targets.contains(FileFormat.ZIP))
        assertTrue(targets.contains(FileFormat.SHA256))
    }

    @Test
    fun testCsvToJsonAndBack() {
        val csv = "Name,Age,Active\nAlice,30,true\nBob,25,false"
        val json = DataConverter.convertCsvToJson(csv, ',', false)
        assertTrue(json.contains("Alice"))
        assertTrue(json.contains("Bob"))
        assertTrue(json.contains("Age"))

        val regeneratedCsv = DataConverter.convertJsonToCsv(json, ',')
        assertTrue(regeneratedCsv.contains("Alice"))
        assertTrue(regeneratedCsv.contains("Bob"))
    }

    @Test
    fun testMarkdownToHtml() {
        val md = "# Heading 1\n\nThis is **bold** text."
        val html = DocumentConverter.convertMarkdownToHtml(md)
        assertTrue(html.contains("<h1>Heading 1</h1>"))
        assertTrue(html.contains("<p>This is **bold** text.</p>"))
    }

    @Test
    fun testMarkdownToPlainText() {
        val md = "## Title\n\n- Item 1\n- Item 2\n\n```code```"
        val plain = DocumentConverter.stripMarkdownToPlainText(md)
        assertTrue(plain.contains("Title"))
        assertTrue(plain.contains("Item 1"))
        assertTrue(!plain.startsWith("##"))
    }

    @Test
    fun testZipArchiveCompressionAndInspection() {
        val payload = "Hello Filee Universal Converter".toByteArray(Charsets.UTF_8)
        val zipBytes = ArchiveConverter.createZipSingleFile(payload, "test.txt")
        assertTrue(zipBytes.isNotEmpty())

        val manifest = ArchiveConverter.inspectZipManifest(zipBytes)
        assertTrue(manifest.contains("test.txt"))
        assertTrue(manifest.contains("entries found"))
    }

    @Test
    fun testHashDigestComputation() {
        val payload = "Filee Security Guarantee".toByteArray(Charsets.UTF_8)
        val hashResult = DataConverter.convertData(
            inputBytes = payload,
            inputFormat = FileFormat.TXT,
            targetFormat = FileFormat.SHA256,
            options = ConversionOptions()
        )
        val hashStr = String(hashResult, Charsets.UTF_8)
        assertTrue(hashStr.contains("SHA-256 Checksum"))
        assertTrue(hashStr.contains("Digest (Hex):"))
    }
}
