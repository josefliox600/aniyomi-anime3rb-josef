package eu.kanade.tachiyomi.animeextension.ar.anime3rb

import eu.kanade.tachiyomi.animesource.AnimeHttpSource
import okhttp3.Request
import org.jsoup.Jsoup

class Anime3rb : AnimeHttpSource() {
    override val name = "Anime3rb"
    override val baseUrl = "https://anime3rb.com"
    override val lang = "ar"
    override val supportsLatest = true

    override fun popularAnimeRequest(page: Int): Request {
        return GET("$baseUrl/anime-list/page/$page/")
    }

    // More functions to be implemented
}