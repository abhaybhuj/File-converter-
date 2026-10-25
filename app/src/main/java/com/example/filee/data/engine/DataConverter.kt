package com.example.filee.data.engine

import com.example.filee.data.model.ConversionOptions
import com.example.filee.data.model.FileFormat
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.zip.CRC32

object DataConverter {

    fun convertData(
        inputBytes: ByteArray,
        inputFormat: FileFormat,
        targetFormat: FileFormat,
        options: ConversionOptions
    ): ByteArray {
        // Hashing & Hex checks
        if (targetFormat == FileFormat.SHA256) {
            return computeHash(inputBytes, "SHA-256").toByteArray(Charsets.UTF_8)
        }
        if (targetFormat == FileFormat.MD5) {
            return computeHash(inputBytes, "MD5").toByteArray(Charsets.UTF_8)
        }
        if (targetFormat == FileFormat.HEX) {
            return formatHexDump(inputBytes).toByteArray(Charsets.UTF_8)
        }
        if (targetFormat == FileFormat.BASE64) {
            return android.util.Base64.encode(inputBytes, android.util.Base64.DEFAULT)
        }
        if (inputFormat == FileFormat.BASE64 && targetFormat == FileFormat.TXT) {
            val text = String(inputBytes, Charsets.UTF_8).trim()
            val decoded = android.util.Base64.decode(text, android.util.Base64.DEFAULT)
            return decoded
        }

        val text = String(inputBytes, Charsets.UTF_8)

        return when {
            inputFormat == FileFormat.CSV && targetFormat == FileFormat.JSON -> {
                convertCsvToJson(text, options.csvDelimiter, options.prettyPrintJson).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.CSV && targetFormat == FileFormat.TSV -> {
                convertCsvToTsv(text, options.csvDelimiter).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.TSV && targetFormat == FileFormat.CSV -> {
                convertTsvToCsv(text).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.TSV && targetFormat == FileFormat.JSON -> {
                convertCsvToJson(text, '\t', options.prettyPrintJson).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.JSON && targetFormat == FileFormat.CSV -> {
                convertJsonToCsv(text, options.csvDelimiter).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.JSON && targetFormat == FileFormat.TSV -> {
                convertJsonToCsv(text, '\t').toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.JSON && targetFormat == FileFormat.XML -> {
                convertJsonToXml(text).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.JSON && targetFormat == FileFormat.YAML -> {
                convertJsonToYaml(text).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.XML && targetFormat == FileFormat.JSON -> {
                convertXmlToJson(text, options.prettyPrintJson).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.XML && targetFormat == FileFormat.YAML -> {
                val json = convertXmlToJson(text, false)
                convertJsonToYaml(json).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.YAML && targetFormat == FileFormat.JSON -> {
                convertYamlToJson(text, options.prettyPrintJson).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.YAML && targetFormat == FileFormat.XML -> {
                val json = convertYamlToJson(text, false)
                convertJsonToXml(json).toByteArray(Charsets.UTF_8)
            }
            inputFormat.category == com.example.filee.data.model.FormatCategory.DATA && targetFormat == FileFormat.HTML -> {
                convertTabularOrJsonToHtmlTable(text, inputFormat).toByteArray(Charsets.UTF_8)
            }
            inputFormat.category == com.example.filee.data.model.FormatCategory.DATA && targetFormat == FileFormat.TXT -> {
                inputBytes // Already text representation
            }
            else -> throw UnsupportedOperationException("Unsupported data conversion from ${inputFormat.displayName} to ${targetFormat.displayName}")
        }
    }

    private fun computeHash(bytes: ByteArray, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        val hash = digest.digest(bytes)
        val crc = CRC32().apply { update(bytes) }.value
        return buildString {
            append("--- $algorithm Checksum ---\n")
            append("Digest (Hex): ")
            for (b in hash) {
                append(String.format("%02x", b))
            }
            append("\n\nCRC32 Checksum: ${String.format("%08X", crc)}\n")
            append("Payload Size: ${bytes.size} bytes\n")
        }
    }

    private fun formatHexDump(bytes: ByteArray): String {
        val sb = StringBuilder()
        val limit = bytes.size.coerceAtMost(8192) // Format up to 8KB
        for (i in 0 until limit step 16) {
            sb.append(String.format("%08X  ", i))
            val lineBytes = bytes.sliceArray(i until (i + 16).coerceAtMost(limit))
            for (j in 0 until 16) {
                if (j < lineBytes.size) {
                    sb.append(String.format("%02X ", lineBytes[j]))
                } else {
                    sb.append("   ")
                }
                if (j == 7) sb.append(" ")
            }
            sb.append(" |")
            for (b in lineBytes) {
                val c = b.toInt().toChar()
                if (c in ' '..'~') sb.append(c) else sb.append('.')
            }
            sb.append("|\n")
        }
        if (bytes.size > limit) {
            sb.append("\n... [${bytes.size - limit} more bytes truncated in preview dump]")
        }
        return sb.toString()
    }

    fun convertCsvToJson(csv: String, delimiter: Char, prettyPrint: Boolean): String {
        val lines = csv.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return "[]"

        val headers = parseCsvLine(lines[0], delimiter)
        val array = JSONArray()

        for (i in 1 until lines.size) {
            val values = parseCsvLine(lines[i], delimiter)
            val obj = JSONObject()
            for (j in headers.indices) {
                val key = headers[j]
                val value = if (j < values.size) values[j] else ""
                // Auto-type inference for numbers and booleans
                when {
                    value.equals("true", ignoreCase = true) -> obj.put(key, true)
                    value.equals("false", ignoreCase = true) -> obj.put(key, false)
                    value.toIntOrNull() != null -> obj.put(key, value.toInt())
                    value.toDoubleOrNull() != null -> obj.put(key, value.toDouble())
                    else -> obj.put(key, value)
                }
            }
            array.put(obj)
        }

        return if (prettyPrint) array.toString(2) else array.toString()
    }

    fun convertJsonToCsv(json: String, delimiter: Char): String {
        val trimmed = json.trim()
        val array = if (trimmed.startsWith("[")) {
            JSONArray(trimmed)
        } else if (trimmed.startsWith("{")) {
            JSONArray().put(JSONObject(trimmed))
        } else {
            return ""
        }

        if (array.length() == 0) return ""

        val headers = linkedSetOf<String>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val keys = obj.keys()
            while (keys.hasNext()) {
                headers.add(keys.next())
            }
        }

        val sb = StringBuilder()
        sb.append(headers.joinToString(delimiter.toString()) { escapeCsv(it, delimiter) }).append("\n")

        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i)
            if (obj != null) {
                val row = headers.map { key ->
                    escapeCsv(obj.optString(key, ""), delimiter)
                }
                sb.append(row.joinToString(delimiter.toString())).append("\n")
            }
        }

        return sb.toString()
    }

    fun convertCsvToTsv(csv: String, currentDelimiter: Char): String {
        val lines = csv.lines()
        return lines.joinToString("\n") { line ->
            parseCsvLine(line, currentDelimiter).joinToString("\t") { it.replace("\t", " ") }
        }
    }

    fun convertTsvToCsv(tsv: String): String {
        val lines = tsv.lines()
        return lines.joinToString("\n") { line ->
            line.split("\t").joinToString(",") { escapeCsv(it, ',') }
        }
    }

    fun convertJsonToXml(json: String): String {
        val trimmed = json.trim()
        val sb = StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>\n")
        if (trimmed.startsWith("[")) {
            val array = JSONArray(trimmed)
            for (i in 0 until array.length()) {
                sb.append("  <item>\n")
                val item = array.get(i)
                if (item is JSONObject) {
                    appendJsonObjectToXml(item, sb, 4)
                } else {
                    sb.append("    <value>${escapeXml(item.toString())}</value>\n")
                }
                sb.append("  </item>\n")
            }
        } else if (trimmed.startsWith("{")) {
            val obj = JSONObject(trimmed)
            appendJsonObjectToXml(obj, sb, 2)
        }
        sb.append("</root>")
        return sb.toString()
    }

    private fun appendJsonObjectToXml(obj: JSONObject, sb: StringBuilder, indent: Int) {
        val pad = " ".repeat(indent)
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val cleanKey = key.replace(Regex("""[^a-zA-Z0-9_]"""), "_")
            val value = obj.get(key)
            if (value is JSONObject) {
                sb.append("$pad<$cleanKey>\n")
                appendJsonObjectToXml(value, sb, indent + 2)
                sb.append("$pad</$cleanKey>\n")
            } else if (value is JSONArray) {
                sb.append("$pad<$cleanKey>\n")
                for (i in 0 until value.length()) {
                    val elem = value.get(i)
                    if (elem is JSONObject) {
                        sb.append("$pad  <item>\n")
                        appendJsonObjectToXml(elem, sb, indent + 4)
                        sb.append("$pad  </item>\n")
                    } else {
                        sb.append("$pad  <item>${escapeXml(elem.toString())}</item>\n")
                    }
                }
                sb.append("$pad</$cleanKey>\n")
            } else {
                sb.append("$pad<$cleanKey>${escapeXml(value.toString())}</$cleanKey>\n")
            }
        }
    }

    fun convertXmlToJson(xml: String, prettyPrint: Boolean): String {
        // Robust structural XML parser to JSON
        val rootObj = JSONObject()
        val tagRegex = Regex("""<([a-zA-Z0-9_]+)>(.*?)</\1>""")
        val matches = tagRegex.findAll(xml)
        for (m in matches) {
            val tag = m.groupValues[1]
            val content = m.groupValues[2].trim()
            rootObj.put(tag, content)
        }
        return if (prettyPrint) rootObj.toString(2) else rootObj.toString()
    }

    fun convertJsonToYaml(json: String): String {
        val trimmed = json.trim()
        val sb = StringBuilder()
        if (trimmed.startsWith("[")) {
            val array = JSONArray(trimmed)
            for (i in 0 until array.length()) {
                val item = array.get(i)
                if (item is JSONObject) {
                    sb.append("-\n")
                    appendJsonObjectToYaml(item, sb, 2)
                } else {
                    sb.append("- $item\n")
                }
            }
        } else if (trimmed.startsWith("{")) {
            val obj = JSONObject(trimmed)
            appendJsonObjectToYaml(obj, sb, 0)
        }
        return sb.toString()
    }

    private fun appendJsonObjectToYaml(obj: JSONObject, sb: StringBuilder, indent: Int) {
        val pad = " ".repeat(indent)
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = obj.get(key)
            if (value is JSONObject) {
                sb.append("$pad$key:\n")
                appendJsonObjectToYaml(value, sb, indent + 2)
            } else if (value is JSONArray) {
                sb.append("$pad$key:\n")
                for (i in 0 until value.length()) {
                    val elem = value.get(i)
                    if (elem is JSONObject) {
                        sb.append("$pad  -\n")
                        appendJsonObjectToYaml(elem, sb, indent + 4)
                    } else {
                        sb.append("$pad  - $elem\n")
                    }
                }
            } else {
                val vStr = value.toString()
                if (vStr.contains("\n") || vStr.contains(":") || vStr.contains("#")) {
                    sb.append("$pad$key: \"${vStr.replace("\"", "\\\"")}\"\n")
                } else {
                    sb.append("$pad$key: $vStr\n")
                }
            }
        }
    }

    fun convertYamlToJson(yaml: String, prettyPrint: Boolean): String {
        val obj = JSONObject()
        val lines = yaml.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue
            val colonIdx = trimmed.indexOf(':')
            if (colonIdx > 0) {
                val key = trimmed.substring(0, colonIdx).trim()
                val value = trimmed.substring(colonIdx + 1).trim().removeSurrounding("\"")
                obj.put(key, value)
            }
        }
        return if (prettyPrint) obj.toString(2) else obj.toString()
    }

    fun convertTabularOrJsonToHtmlTable(text: String, format: FileFormat): String {
        val sb = StringBuilder("<!DOCTYPE html><html><head><meta charset=\"utf-8\">")
        sb.append("<style>table{border-collapse:collapse;width:100%;font-family:sans-serif}th,td{border:1px solid #ddd;padding:8px;text-align:left}th{background:#38BDF8;color:white}tr:nth-child(even){background:#f9f9f9}</style></head><body>\n")
        sb.append("<h2>Converted Data Table (${format.displayName})</h2>\n")

        val csvContent = if (format == FileFormat.JSON) {
            convertJsonToCsv(text, ',')
        } else {
            text
        }

        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isNotEmpty()) {
            sb.append("<table>\n<thead><tr>")
            val headers = parseCsvLine(lines[0], ',')
            for (h in headers) sb.append("<th>${escapeXml(h)}</th>")
            sb.append("</tr></thead>\n<tbody>\n")

            for (i in 1 until lines.size) {
                sb.append("<tr>")
                val vals = parseCsvLine(lines[i], ',')
                for (v in vals) sb.append("<td>${escapeXml(v)}</td>")
                sb.append("</tr>\n")
            }
            sb.append("</tbody></table>\n")
        }
        sb.append("</body></html>")
        return sb.toString()
    }

    private fun parseCsvLine(line: String, delimiter: Char): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == delimiter && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb.clear()
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    private fun escapeCsv(value: String, delimiter: Char): String {
        return if (value.contains(delimiter) || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
