package com.moviebox

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.Jsoup

class MovieBoxExtractor : ExtractorApi() {
    override val name = "MovieBox"
    override val mainUrl = "https://moviebox.ph"
    override val requiresReferer = false

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val document = app.get(url, referer = referer).document
        
        // Method 1: Direct video sources
        document.select("video source, source").forEach { source ->
            val videoUrl = source.attr("src")
            if (videoUrl.isNotEmpty()) {
                callback.invoke(
                    ExtractorLink(
                        name,
                        name,
                        videoUrl,
                        referer ?: mainUrl,
                        getQualityFromName(source.attr("label") ?: source.attr("data-quality") ?: ""),
                        videoUrl.contains(".m3u8")
                    )
                )
            }
        }
        
        // Method 2: iframe embeds
        document.select("iframe").forEach { iframe ->
            val iframeSrc = iframe.attr("src")
            if (iframeSrc.isNotEmpty()) {
                loadExtractor(iframeSrc, subtitleCallback, callback)
            }
        }
        
        // Method 3: JavaScript embedded links
        document.select("script").forEach { script ->
            val content = script.html()
            
            // Find m3u8 playlist URLs
            Regex("""["'](https?://[^"']+\.m3u8[^"']*)["']""").findAll(content).forEach { match ->
                val videoUrl = match.groupValues[1]
                callback.invoke(
                    ExtractorLink(
                        name,
                        "$name HLS",
                        videoUrl,
                        referer ?: mainUrl,
                        Qualities.Unknown.value,
                        true
                    )
                )
            }
            
            // Find MP4 URLs
            Regex("""["'](https?://[^"']+\.mp4[^"']*)["']""").findAll(content).forEach { match ->
                val videoUrl = match.groupValues[1]
                val quality = Regex("""(\d{3,4})p?""").find(videoUrl)?.groupValues?.get(1)?.toIntOrNull()
                callback.invoke(
                    ExtractorLink(
                        name,
                        "$name MP4",
                        videoUrl,
                        referer ?: mainUrl,
                        quality ?: Qualities.Unknown.value,
                        false
                    )
                )
            }
        }
        
        // Method 4: Parse JSON data
        val jsonRegex = Regex("""sources?\s*[:=]\s*(\[.*?\]|\{.*?\})""", RegexOption.DOT_MATCHES_ALL)
        document.select("script").forEach { script ->
            val content = script.html()
            jsonRegex.findAll(content).forEach { match ->
                try {
                    val jsonStr = match.groupValues[1]
                    // This would need proper JSON parsing
                    // For now, extract URLs from the JSON string
                    Regex("""["']?(?:file|url)["']?\s*[:=]\s*["'](https?://[^"']+)["']""").findAll(jsonStr).forEach { urlMatch ->
                        val videoUrl = urlMatch.groupValues[1]
                        callback.invoke(
                            ExtractorLink(
                                name,
                                name,
                                videoUrl,
                                referer ?: mainUrl,
                                Qualities.Unknown.value,
                                videoUrl.contains(".m3u8")
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Ignore parsing errors
                }
            }
        }
    }

    private fun getQualityFromName(qualityName: String): Int {
        return when {
            qualityName.contains("2160") || qualityName.contains("4K") -> Qualities.P2160.value
            qualityName.contains("1440") -> Qualities.P1440.value
            qualityName.contains("1080") -> Qualities.P1080.value
            qualityName.contains("720") -> Qualities.P720.value
            qualityName.contains("480") -> Qualities.P480.value
            qualityName.contains("360") -> Qualities.P360.value
            else -> Qualities.Unknown.value
        }
    }
}
