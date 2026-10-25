package com.example.filee.data.engine

import com.example.filee.data.model.ConversionRoute
import com.example.filee.data.model.FileFormat
import java.util.LinkedList
import java.util.Queue

object RoutePlanner {

    // Graph of direct conversion edges: From -> Set of To formats
    private val directEdges: Map<FileFormat, Set<FileFormat>> by lazy {
        val edges = mutableMapOf<FileFormat, MutableSet<FileFormat>>()

        fun addEdge(from: FileFormat, to: FileFormat) {
            edges.getOrPut(from) { mutableSetOf() }.add(to)
        }

        fun addBiEdge(a: FileFormat, b: FileFormat) {
            addEdge(a, b)
            addEdge(b, a)
        }

        // Image intra-conversions
        val standardImages = listOf(FileFormat.PNG, FileFormat.JPEG, FileFormat.WEBP, FileFormat.BMP, FileFormat.ICO)
        for (a in standardImages) {
            for (b in standardImages) {
                if (a != b) addEdge(a, b)
            }
            // All standard images can be compiled into PDF
            addEdge(a, FileFormat.PDF)
        }

        // SVG conversions
        addEdge(FileFormat.SVG, FileFormat.PNG)
        addEdge(FileFormat.SVG, FileFormat.JPEG)
        addEdge(FileFormat.SVG, FileFormat.WEBP)
        addEdge(FileFormat.SVG, FileFormat.PDF)
        addEdge(FileFormat.SVG, FileFormat.HTML)
        addEdge(FileFormat.SVG, FileFormat.TXT)

        // GIF conversions
        addEdge(FileFormat.GIF, FileFormat.PNG)
        addEdge(FileFormat.GIF, FileFormat.JPEG)
        addEdge(FileFormat.GIF, FileFormat.WEBP)

        // PDF conversions
        addEdge(FileFormat.PDF, FileFormat.PNG)
        addEdge(FileFormat.PDF, FileFormat.JPEG)
        addEdge(FileFormat.PDF, FileFormat.WEBP)
        addEdge(FileFormat.PDF, FileFormat.TXT)
        addEdge(FileFormat.PDF, FileFormat.ZIP)

        // Markdown conversions
        addEdge(FileFormat.MD, FileFormat.HTML)
        addEdge(FileFormat.MD, FileFormat.TXT)
        addEdge(FileFormat.MD, FileFormat.PDF)
        addEdge(FileFormat.MD, FileFormat.RTF)
        addEdge(FileFormat.MD, FileFormat.JSON)

        // HTML conversions
        addEdge(FileFormat.HTML, FileFormat.TXT)
        addEdge(FileFormat.HTML, FileFormat.MD)
        addEdge(FileFormat.HTML, FileFormat.PDF)
        addEdge(FileFormat.HTML, FileFormat.CSV)
        addEdge(FileFormat.HTML, FileFormat.JSON)

        // Plain Text conversions
        addEdge(FileFormat.TXT, FileFormat.MD)
        addEdge(FileFormat.TXT, FileFormat.HTML)
        addEdge(FileFormat.TXT, FileFormat.PDF)
        addEdge(FileFormat.TXT, FileFormat.WAV) // Text-to-speech
        addEdge(FileFormat.TXT, FileFormat.GZ)
        addEdge(FileFormat.TXT, FileFormat.ZIP)

        // RTF conversions
        addEdge(FileFormat.RTF, FileFormat.TXT)
        addEdge(FileFormat.RTF, FileFormat.HTML)
        addEdge(FileFormat.RTF, FileFormat.PDF)

        // Data / Structured tabular & tree formats
        addBiEdge(FileFormat.CSV, FileFormat.JSON)
        addBiEdge(FileFormat.CSV, FileFormat.TSV)
        addBiEdge(FileFormat.JSON, FileFormat.XML)
        addBiEdge(FileFormat.JSON, FileFormat.YAML)
        addBiEdge(FileFormat.JSON, FileFormat.TSV)

        addEdge(FileFormat.CSV, FileFormat.HTML)
        addEdge(FileFormat.CSV, FileFormat.TXT)
        addEdge(FileFormat.CSV, FileFormat.PDF)

        addEdge(FileFormat.TSV, FileFormat.TXT)
        addEdge(FileFormat.TSV, FileFormat.HTML)

        addEdge(FileFormat.JSON, FileFormat.TXT)
        addEdge(FileFormat.JSON, FileFormat.HTML)
        addEdge(FileFormat.JSON, FileFormat.PDF)

        addEdge(FileFormat.XML, FileFormat.TXT)
        addEdge(FileFormat.XML, FileFormat.HTML)

        addEdge(FileFormat.YAML, FileFormat.TXT)

        // Audio conversions
        addEdge(FileFormat.WAV, FileFormat.MP3)
        addEdge(FileFormat.WAV, FileFormat.AAC)
        addEdge(FileFormat.WAV, FileFormat.TXT) // Audio metadata & analysis
        addEdge(FileFormat.MP3, FileFormat.WAV)
        addEdge(FileFormat.MP3, FileFormat.TXT)
        addEdge(FileFormat.AAC, FileFormat.WAV)
        addEdge(FileFormat.AAC, FileFormat.TXT)

        // Archives
        addEdge(FileFormat.ZIP, FileFormat.TXT) // Manifest inspection
        addEdge(FileFormat.GZ, FileFormat.TXT)

        // Crypto & Encoding universal edges: Every format can be hashed or packed to zip/base64/hex
        for (fmt in FileFormat.entries) {
            addEdge(fmt, FileFormat.SHA256)
            addEdge(fmt, FileFormat.MD5)
            addEdge(fmt, FileFormat.BASE64)
            addEdge(fmt, FileFormat.HEX)
            if (fmt != FileFormat.ZIP) addEdge(fmt, FileFormat.ZIP)
        }

        // Base64 & Hex decoders back to text or binary
        addEdge(FileFormat.BASE64, FileFormat.TXT)
        addEdge(FileFormat.BASE64, FileFormat.HEX)
        addEdge(FileFormat.BASE64, FileFormat.PNG)
        addEdge(FileFormat.HEX, FileFormat.TXT)
        addEdge(FileFormat.HEX, FileFormat.BASE64)

        edges
    }

    /**
     * Find all compatible target formats for a given input format,
     * reachable within max 3 hops (as specified in KnifeLemon/Filee).
     */
    fun getAvailableTargetFormats(source: FileFormat): List<FileFormat> {
        val targets = mutableSetOf<FileFormat>()
        val queue: Queue<Pair<FileFormat, Int>> = LinkedList()
        val visited = mutableSetOf(source)

        queue.add(Pair(source, 0))

        while (queue.isNotEmpty()) {
            val (current, depth) = queue.poll()
            if (depth >= 3) continue

            val neighbors = directEdges[current] ?: emptySet()
            for (next in neighbors) {
                if (next !in visited) {
                    visited.add(next)
                    targets.add(next)
                    queue.add(Pair(next, depth + 1))
                }
            }
        }

        return targets.sortedBy { it.displayName }
    }

    /**
     * BFS Pathfinding: finds the shortest and optimal route between source and target format.
     */
    fun findRoute(source: FileFormat, target: FileFormat): ConversionRoute? {
        if (source == target) return null

        val queue: Queue<List<FileFormat>> = LinkedList()
        val visited = mutableSetOf<FileFormat>()

        queue.add(listOf(source))
        visited.add(source)

        while (queue.isNotEmpty()) {
            val path = queue.poll()
            val current = path.last()

            if (current == target) {
                return buildRouteFromPath(path)
            }

            if (path.size > 3) continue // Filee constraint: up to 3 hops (4 nodes max)

            val neighbors = directEdges[current] ?: emptySet()
            for (next in neighbors) {
                if (next !in visited) {
                    visited.add(next)
                    queue.add(path + next)
                }
            }
        }

        return null
    }

    private fun buildRouteFromPath(path: List<FileFormat>): ConversionRoute {
        val steps = mutableListOf<String>()
        for (i in 0 until path.size - 1) {
            val from = path[i]
            val to = path[i + 1]
            steps.add("Convert ${from.displayName} to ${to.displayName}")
        }

        val fidelity = when {
            path.size == 2 && path[0].category == path[1].category -> "Lossless / Native Fidelity"
            path.size == 2 -> "High Fidelity Direct Route"
            path.size == 3 -> "Smart Multi-Hop Route (2 steps)"
            else -> "Complex Multi-Step Route (${path.size - 1} steps)"
        }

        return ConversionRoute(
            inputFormat = path.first(),
            outputFormat = path.last(),
            hops = path,
            stepDescriptions = steps,
            estimatedFidelity = fidelity
        )
    }

    /**
     * Returns all supported direct pairs for visualization or route exploration.
     */
    fun getAllDirectPairs(): List<Pair<FileFormat, FileFormat>> {
        val pairs = mutableListOf<Pair<FileFormat, FileFormat>>()
        for ((from, tos) in directEdges) {
            for (to in tos) {
                pairs.add(Pair(from, to))
            }
        }
        return pairs
    }
}
