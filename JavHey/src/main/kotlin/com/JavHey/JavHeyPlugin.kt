package com.JavHey

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class JavHeyPlugin: Plugin() {
    override fun load(context: Context) {
        // 1. Register Provider Utama
        registerMainAPI(JavHey())

        // 2. Register SEMUA Extractor Server yang kita buat tadi
        registerExtractorAPI(Hglink())
        registerExtractorAPI(Haxloppd())
        registerExtractorAPI(Minochinos()) // Baru
        registerExtractorAPI(Bysebuho())   // Baru
        registerExtractorAPI(GoTv())       // Baru
    }
}
