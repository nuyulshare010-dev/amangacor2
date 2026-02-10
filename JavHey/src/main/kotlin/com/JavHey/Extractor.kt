package com.JavHey

import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.INFER_TYPE
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.M3u8Helper
import com.lagradost.cloudstream3.utils.newExtractorLink
import java.net.URI

// --- DAFTAR SERVER ---
class Hglink : JavHeyDood("https://hglink.to", "Hglink")
class Haxloppd : JavHeyDood("https://haxloppd.com", "Haxloppd")
class Minochinos : JavHeyDood("https://minochinos.com", "Minochinos")
class GoTv : JavHeyDood("https://go-tv.lol", "GoTv")

// --- LOGIKA UTAMA ---
open class JavHeyDood(override var mainUrl: String, override var name: String) : ExtractorApi() {
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (com.lagradost.cloudstream3.SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        // 1. Jangan ubah /v/ jadi /e/ (Minochinos butuh /v/)
        val targetUrl = url 
        
        try {
            // 2. Ambil halaman HTML
            // Timeout dipanjangkan sedikit (15s) untuk jaga-jaga
            val responseReq = app.get(targetUrl, referer = "https://javhey.com/", timeout = 15)
            val response = responseReq.text
            val currentHost = "https://" + URI(responseReq.url).host

            // 3. Cari endpoint pass_md5
            // Kita pakai regex yang lebih fleksibel untuk menangkap variasi Doodstream
            var md5Match = Regex("""/pass_md5/[^']*""").find(response)?.value

            // JIKA MD5 TIDAK KETEMU (Kasus Haxloppd/GoTv yang dipacking)
            if (md5Match == null) {
                // Cari pola "makePlay('...')" yang biasa dipakai di script packing
                val makePlayMatch = Regex("""makePlay\('([^']*)'\)""").find(response)
                if (makePlayMatch != null) {
                     val playPath = makePlayMatch.groupValues[1]
                     md5Match = "/pass_md5/$playPath" // Rekonstruksi manual path-nya
                }
            }

            if (md5Match != null) {
                // Gunakan host asli untuk request token
                val trueUrl = "$currentHost$md5Match"
                
                // 4. Request Token (Wajib Referer Asli)
                val tokenResponse = app.get(trueUrl, referer = targetUrl).text

                // 5. Buat Link Video
                val randomString = generateRandomString()
                val videoUrl = "$tokenResponse$randomString?token=${md5Match.substringAfterLast("/")}&expiry=${System.currentTimeMillis()}"

                // 6. Kirim Link
                M3u8Helper.generateM3u8(
                    name,
                    videoUrl,
                    targetUrl,
                    headers = mapOf("Origin" to currentHost, "Referer" to targetUrl)
                ).forEach(callback)
            } else {
                // Fallback terakhir: Cari redirect window.location
                val redirectMatch = Regex("""window\.location\.replace\('([^']*)'\)""").find(response)
                if (redirectMatch != null) {
                    callback.invoke(
                        newExtractorLink(
                            source = name,
                            name = name,
                            url = redirectMatch.groupValues[1],
                            type = INFER_TYPE
                        ) {
                            this.referer = targetUrl
                            this.quality = Qualities.Unknown.value
                        }
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateRandomString(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..10)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
