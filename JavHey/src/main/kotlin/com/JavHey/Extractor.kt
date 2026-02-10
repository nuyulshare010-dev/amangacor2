package com.JavHey

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.api.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.M3u8Helper.Companion.generateM3u8
import com.lagradost.cloudstream3.network.WebViewResolver
import org.jsoup.nodes.Document
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

// ==========================================
// 1. MANAJER UTAMA (JAVHEY EXTRACTOR)
// ==========================================
object JavHeyExtractor {
    suspend fun invoke(
        document: Document,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val uniqueUrls = mutableSetOf<String>()

        // Decode Base64 Hidden Links
        try {
            val hiddenInput = document.selectFirst("input#links")
            val hiddenLinksEncrypted = hiddenInput?.attr("value")
            if (!hiddenLinksEncrypted.isNullOrEmpty()) {
                val decodedBytes = Base64.getDecoder().decode(hiddenLinksEncrypted)
                val urls = String(decodedBytes).split(",,,")
                urls.forEach { sourceUrl ->
                    val cleanUrl = sourceUrl.trim()
                    if (cleanUrl.startsWith("http")) uniqueUrls.add(cleanUrl)
                }
            }
        } catch (e: Exception) { e.printStackTrace() }

        // Fallback: Tombol Download
        try {
            document.select("div.links-download a").forEach { linkTag ->
                val downloadUrl = linkTag.attr("href").trim()
                if (downloadUrl.startsWith("http")) uniqueUrls.add(downloadUrl)
            }
        } catch (e: Exception) { e.printStackTrace() }

        // Eksekusi Link (Tanpa Duplikat)
        uniqueUrls.forEach { url ->
            loadExtractor(url, subtitleCallback, callback)
        }
    }
}

// ==========================================
// 2. DAPUR VIDHIDE / FILELIONS (Custom)
// ==========================================
class VidHidePro1 : VidHidePro() { override var mainUrl = "https://filelions.live" }
class VidHidePro2 : VidHidePro() { override var mainUrl = "https://filelions.online" }
class VidHidePro3 : VidHidePro() { override var mainUrl = "https://filelions.to" }
class VidHidePro4 : VidHidePro() { override val mainUrl = "https://kinoger.be" }
class VidHidePro5 : VidHidePro() { override val mainUrl = "https://vidhidevip.com" }
class VidHidePro6 : VidHidePro() { override val mainUrl = "https://vidhidepre.com" }
class Smoothpre : VidHidePro() { override var name = "EarnVids"; override var mainUrl = "https://smoothpre.com" }
class Dhtpre : VidHidePro() { override var name = "EarnVids"; override var mainUrl = "https://dhtpre.com" }
class Peytonepre : VidHidePro() { override var name = "EarnVids"; override var mainUrl = "https://peytonepre.com" }

open class VidHidePro : ExtractorApi() {
    override val name = "VidHidePro"
    override val mainUrl = "https://vidhidepro.com"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val headers = mapOf(
            "Sec-Fetch-Dest" to "empty", "Sec-Fetch-Mode" to "cors", "Sec-Fetch-Site" to "cross-site",
            "Origin" to mainUrl, "User-Agent" to USER_AGENT
        )
        val response = app.get(getEmbedUrl(url), referer = referer)
        val script = if (!getPacked(response.text).isNullOrEmpty()) {
            var result = getAndUnpack(response.text)
            if(result.contains("var links")) result = result.substringAfter("var links")
            result
        } else {
            response.document.selectFirst("script:containsData(sources:)")?.data()
        } ?: return

        Regex(":\\s*\"(.*?m3u8.*?)\"").findAll(script).forEach { m3u8Match ->
            generateM3u8(name, fixUrl(m3u8Match.groupValues[1]), referer = "$mainUrl/", headers = headers).forEach(callback)
        }
    }
    private fun getEmbedUrl(url: String): String {
        return when {
            url.contains("/d/") -> url.replace("/d/", "/v/")
            url.contains("/download/") -> url.replace("/download/", "/v/")
            url.contains("/file/") -> url.replace("/file/", "/v/")
            else -> url.replace("/f/", "/v/")
        }
    }
}

// ==========================================
// 3. DAPUR MIXDROP (Custom)
// ==========================================
class MixDropBz : MixDrop(){ override var mainUrl = "https://mixdrop.bz" }
class MixDropAg : MixDrop(){ override var mainUrl = "https://mixdrop.ag" }
class MixDropCh : MixDrop(){ override var mainUrl = "https://mixdrop.ch" }
class MixDropTo : MixDrop(){ override var mainUrl = "https://mixdrop.to" }

open class MixDrop : ExtractorApi() {
    override var name = "MixDrop"
    override var mainUrl = "https://mixdrop.co"
    private val srcRegex = Regex("""wurl.*?=.*?"(.*?)";""")
    override val requiresReferer = false

    override fun getExtractorUrl(id: String): String = "$mainUrl/e/$id"

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        with(app.get(url.replaceFirst("/f/", "/e/"))) {
            getAndUnpack(this.text).let { unpackedText ->
                srcRegex.find(unpackedText)?.groupValues?.get(1)?.let { link ->
                    return listOf(newExtractorLink(name, name, httpsify(link)) {
                        this.referer = url
                        this.quality = Qualities.Unknown.value
                    })
                }
            }
        }
        return null
    }
}

// ==========================================
// 4. DAPUR STREAMWISH (Custom)
// ==========================================
class Mwish : StreamWishExtractor() { override val name = "Mwish"; override val mainUrl = "https://mwish.pro" }
class Dwish : StreamWishExtractor() { override val name = "Dwish"; override val mainUrl = "https://dwish.pro" }
class Ewish : StreamWishExtractor() { override val name = "Embedwish"; override val mainUrl = "https://embedwish.com" }
class WishembedPro : StreamWishExtractor() { override val name = "Wishembed"; override val mainUrl = "https://wishembed.pro" }
class Kswplayer : StreamWishExtractor() { override val name = "Kswplayer"; override val mainUrl = "https://kswplayer.info" }
class Wishfast : StreamWishExtractor() { override val name = "Wishfast"; override val mainUrl = "https://wishfast.top" }
class Streamwish2 : StreamWishExtractor() { override val mainUrl = "https://streamwish.site" }
class SfastwishCom : StreamWishExtractor() { override val name = "Sfastwish"; override val mainUrl = "https://sfastwish.com" }
class Strwish : StreamWishExtractor() { override val name = "Strwish"; override val mainUrl = "https://strwish.xyz" }
class Strwish2 : StreamWishExtractor() { override val name = "Strwish"; override val mainUrl = "https://strwish.com" }
class FlaswishCom : StreamWishExtractor() { override val name = "Flaswish"; override val mainUrl = "https://flaswish.com" }
class Awish : StreamWishExtractor() { override val name = "Awish"; override val mainUrl = "https://awish.pro" }
class Obeywish : StreamWishExtractor() { override val name = "Obeywish"; override val mainUrl = "https://obeywish.com" }
class Jodwish : StreamWishExtractor() { override val name = "Jodwish"; override val mainUrl = "https://jodwish.com" }
class Swhoi : StreamWishExtractor() { override val name = "Swhoi"; override val mainUrl = "https://swhoi.com" }
class Multimovies : StreamWishExtractor() { override val name = "Multimovies"; override val mainUrl = "https://multimovies.cloud" }
class UqloadsXyz : StreamWishExtractor() { override val name = "Uqloads"; override val mainUrl = "https://uqloads.xyz" }
class Doodporn : StreamWishExtractor() { override val name = "Doodporn"; override val mainUrl = "https://doodporn.xyz" }
class CdnwishCom : StreamWishExtractor() { override val name = "Cdnwish"; override val mainUrl = "https://cdnwish.com" }
class Asnwish : StreamWishExtractor() { override val name = "Asnwish"; override val mainUrl = "https://asnwish.com" }
class Nekowish : StreamWishExtractor() { override val name = "Nekowish"; override val mainUrl = "https://nekowish.my.id" }
class Nekostream : StreamWishExtractor() { override val name = "Nekostream"; override val mainUrl = "https://neko-stream.click" }
class Swdyu : StreamWishExtractor() { override val name = "Swdyu"; override val mainUrl = "https://swdyu.com" }
class Wishonly : StreamWishExtractor() { override val name = "Wishonly"; override val mainUrl = "https://wishonly.site" }
class Playerwish : StreamWishExtractor() { override val name = "Playerwish"; override val mainUrl = "https://playerwish.com" }
class StreamHLS : StreamWishExtractor() { override val name = "StreamHLS"; override val mainUrl = "https://streamhls.to" }
class HlsWish : StreamWishExtractor() { override val name = "HlsWish"; override val mainUrl = "https://hlswish.com" }

open class StreamWishExtractor : ExtractorApi() {
    override val name = "Streamwish"
    override val mainUrl = "https://streamwish.to"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val headers = mapOf(
            "Accept" to "*/*", "Connection" to "keep-alive", "Sec-Fetch-Dest" to "empty",
            "Sec-Fetch-Mode" to "cors", "Sec-Fetch-Site" to "cross-site", "Referer" to "$mainUrl/",
            "Origin" to "$mainUrl/", "User-Agent" to USER_AGENT
        )
        val pageResponse = app.get(resolveEmbedUrl(url), referer = referer)
        val playerScriptData = when {
            !getPacked(pageResponse.text).isNullOrEmpty() -> getAndUnpack(pageResponse.text)
            pageResponse.document.select("script").any { it.html().contains("jwplayer(\"vplayer\").setup(") } ->
                pageResponse.document.select("script").firstOrNull { it.html().contains("jwplayer(\"vplayer\").setup(") }?.html()
            else -> pageResponse.document.selectFirst("script:containsData(sources:)")?.data()
        }
        val directStreamUrl = playerScriptData?.let { Regex("""file:\s*"(.*?m3u8.*?)"""").find(it)?.groupValues?.getOrNull(1) }

        if (!directStreamUrl.isNullOrEmpty()) {
            generateM3u8(name, directStreamUrl, mainUrl, headers = headers).forEach(callback)
        } else {
            val webViewM3u8Resolver = WebViewResolver(
                interceptUrl = Regex("""txt|m3u8"""), additionalUrls = listOf(Regex("""txt|m3u8""")),
                useOkhttp = false, timeout = 15_000L
            )
            val interceptedStreamUrl = app.get(url, referer = referer, interceptor = webViewM3u8Resolver).url
            if (interceptedStreamUrl.isNotEmpty()) {
                generateM3u8(name, interceptedStreamUrl, mainUrl, headers = headers).forEach(callback)
            }
        }
    }
    private fun resolveEmbedUrl(inputUrl: String): String {
        return if (inputUrl.contains("/f/")) "$mainUrl/${inputUrl.substringAfter("/f/")}"
        else if (inputUrl.contains("/e/")) "$mainUrl/${inputUrl.substringAfter("/e/")}"
        else inputUrl
    }
}

// ==========================================
// 5. DAPUR BYSE / BYSEBUHO (Custom)
// ==========================================
@Prerelease class Bysezejataos : ByseSX() { override var name = "Bysezejataos"; override var mainUrl = "https://bysezejataos.com" }
@Prerelease class ByseBuho : ByseSX() { override var name = "ByseBuho"; override var mainUrl = "https://bysebuho.com" }
@Prerelease class ByseVepoin : ByseSX() { override var name = "ByseVepoin"; override var mainUrl = "https://bysevepoin.com" }

@Prerelease
open class ByseSX : ExtractorApi() {
    override var name = "Byse"
    override var mainUrl = "https://byse.sx"
    override val requiresReferer = true

    private fun b64UrlDecode(s: String): ByteArray {
        val fixed = s.replace('-', '+').replace('_', '/')
        val pad = (4 - fixed.length % 4) % 4
        return base64DecodeArray(fixed + "=".repeat(pad))
    }
    private fun getBaseUrl(url: String) = URI(url).let { "${it.scheme}://${it.host}" }
    private fun getCodeFromUrl(url: String) = URI(url).path?.trimEnd('/')?.substringAfterLast('/') ?: ""

    private suspend fun getDetails(mainUrl: String): DetailsRoot? {
        val base = getBaseUrl(mainUrl); val code = getCodeFromUrl(mainUrl)
        return app.get("$base/api/videos/$code/embed/details").parsedSafe<DetailsRoot>()
    }
    private suspend fun getPlayback(mainUrl: String): PlaybackRoot? {
        val details = getDetails(mainUrl) ?: return null
        val embedFrameUrl = details.embedFrameUrl
        val embedBase = getBaseUrl(embedFrameUrl); val code = getCodeFromUrl(embedFrameUrl)
        val headers = mapOf("accept" to "*/*", "referer" to embedFrameUrl, "x-embed-parent" to mainUrl)
        return app.get("$embedBase/api/videos/$code/embed/playback", headers = headers).parsedSafe<PlaybackRoot>()
    }
    private fun decryptPlayback(playback: Playback): String? {
        val keyBytes = b64UrlDecode(playback.keyParts[0]) + b64UrlDecode(playback.keyParts[1])
        val ivBytes = b64UrlDecode(playback.iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(keyBytes, "AES"), GCMParameterSpec(128, ivBytes))
        val jsonStr = String(cipher.doFinal(b64UrlDecode(playback.payload)), StandardCharsets.UTF_8).removePrefix("\uFEFF")
        return tryParseJson<PlaybackDecrypt>(jsonStr)?.sources?.firstOrNull()?.url
    }
    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val playbackRoot = getPlayback(url) ?: return
        val streamUrl = decryptPlayback(playbackRoot.playback) ?: return
        generateM3u8(name, streamUrl, mainUrl, headers = mapOf("Referer" to getBaseUrl(url))).forEach(callback)
    }
}
// Data classes for Byse
data class DetailsRoot(val id: Long, val code: String, val title: String, @JsonProperty("poster_url") val posterUrl: String, val description: String, @JsonProperty("created_at") val createdAt: String, @JsonProperty("owner_private") val ownerPrivate: Boolean, @JsonProperty("embed_frame_url") val embedFrameUrl: String)
data class PlaybackRoot(val playback: Playback)
data class Playback(val algorithm: String, val iv: String, val payload: String, @JsonProperty("key_parts") val keyParts: List<String>, @JsonProperty("expires_at") val expiresAt: String, @JsonProperty("decrypt_keys") val decryptKeys: DecryptKeys, val iv2: String, val payload2: String)
data class DecryptKeys(@JsonProperty("edge_1") val edge1: String, @JsonProperty("edge_2") val edge2: String, @JsonProperty("legacy_fallback") val legacyFallback: String)
data class PlaybackDecrypt(val sources: List<PlaybackDecryptSource>)
data class PlaybackDecryptSource(val quality: String, val label: String, @JsonProperty("mime_type") val mimeType: String, val url: String, @JsonProperty("bitrate_kbps") val bitrateKbps: Long, val height: Any?)

// ==========================================
// 6. DAPUR DOOD (Custom)
// ==========================================
class D0000d : DoodLaExtractor() { override var mainUrl = "https://d0000d.com" }
class D000dCom : DoodLaExtractor() { override var mainUrl = "https://d000d.com" }
class DoodstreamCom : DoodLaExtractor() { override var mainUrl = "https://doodstream.com" }
class Dooood : DoodLaExtractor() { override var mainUrl = "https://dooood.com" }
class DoodWfExtractor : DoodLaExtractor() { override var mainUrl = "https://dood.wf" }
class DoodCxExtractor : DoodLaExtractor() { override var mainUrl = "https://dood.cx" }
class DoodShExtractor : DoodLaExtractor() { override var mainUrl = "https://dood.sh" }
class DoodWatchExtractor : DoodLaExtractor() { override var mainUrl = "https://dood.watch" }
class DoodPmExtractor : DoodLaExtractor() { override var mainUrl = "https://dood.pm" }
class DoodToExtractor : DoodLaExtractor() { override var mainUrl = "https://dood.to" }
class DoodSoExtractor : DoodLaExtractor() { override var mainUrl = "https://dood.so" }
class DoodWsExtractor : DoodLaExtractor() { override var mainUrl = "https://dood.ws" }
class DoodYtExtractor : DoodLaExtractor() { override var mainUrl = "https://dood.yt" }
class DoodLiExtractor : DoodLaExtractor() { override var mainUrl = "https://dood.li" }
class Ds2play : DoodLaExtractor() { override var mainUrl = "https://ds2play.com" }
class Ds2video : DoodLaExtractor() { override var mainUrl = "https://ds2video.com" }
class MyVidPlay : DoodLaExtractor() { override var mainUrl = "https://myvidplay.com" }

open class DoodLaExtractor : ExtractorApi() {
    override var name = "DoodStream"
    override var mainUrl = "https://dood.la"
    override val requiresReferer = false
    private val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val embedUrl = url.replace("/d/", "/e/")
        val req = app.get(embedUrl)
        val host = URI(req.url).let { "${it.scheme}://${it.host}" }
        val response0 = req.text
        val md5 = host + (Regex("/pass_md5/[^']*").find(response0)?.value ?: return)
        val trueUrl = app.get(md5, referer = req.url).text + buildString { repeat(10) { append(alphabet.random()) } } + "?token=" + md5.substringAfterLast("/")
        val quality = Regex("\\d{3,4}p").find(response0.substringAfter("<title>").substringBefore("</title>"))?.groupValues?.getOrNull(0)
        callback.invoke(newExtractorLink(name, name, trueUrl) { this.referer = "$mainUrl/"; this.quality = getQualityFromName(quality) })
    }
}

// ==========================================
// 7. DAPUR LULUSTREAM (Custom)
// ==========================================
class Lulustream1 : LuluStream() { override val name = "Lulustream"; override val mainUrl = "https://lulustream.com" }
class Lulustream2 : LuluStream() { override val name = "Lulustream"; override val mainUrl = "https://kinoger.pw" }

open class LuluStream : ExtractorApi() {
    override val name = "LuluStream"
    override val mainUrl = "https://luluvdo.com"
    override val requiresReferer = true
    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val filecode = url.substringAfterLast("/")
        val post = app.post("$mainUrl/dl", data = mapOf("op" to "embed", "file_code" to filecode, "auto" to "1", "referer" to (referer ?: ""))).document
        post.selectFirst("script:containsData(vplayer)")?.data()?.let { script ->
            Regex("file:\"(.*)\"").find(script)?.groupValues?.get(1)?.let { link ->
                callback(newExtractorLink(name, name, link) { this.referer = mainUrl; this.quality = Qualities.P1080.value })
            }
        }
    }
}
