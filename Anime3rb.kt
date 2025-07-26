package eu.kanade.tachiyomi.animeextension.ar.anime3rb

import eu.kanade.tachiyomi.animesource.model.*
import eu.kanade.tachiyomi.animesource.AnimeHttpSource
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.TimeUnit

class Anime3rb : AnimeHttpSource() {
    override val name = "Anime3rb"
    override val baseUrl = "https://anime3rb.com"
    override val lang = "ar"
    override val supportsLatest = true

    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun popularAnimeRequest(page: Int): Request {
        return GET("$baseUrl/category/anime/page/$page")
    }

    override fun popularAnimeParse(response: okhttp3.Response): AnimesPage {
        val document: Document = Jsoup.parse(response.body!!.string())
        val animeList = document.select(".AnimeCard a").map {
            SAnime.create().apply {
                title = it.attr("title")
                thumbnail_url = it.selectFirst("img")?.attr("src")
                setUrlWithoutDomain(it.attr("href"))
            }
        }
        return AnimesPage(animeList, hasNextPage = true)
    }

    override fun episodeListParse(response: okhttp3.Response): List<SEpisode> {
        val document = Jsoup.parse(response.body!!.string())
        return document.select(".episodes-list a").map {
            SEpisode.create().apply {
                name = it.text()
                setUrlWithoutDomain(it.attr("href"))
            }
        }
    }

    override fun videoListParse(response: okhttp3.Response): List<Video> {
        // هذا الجزء يتطلب فحص الروابط الداخلية لفيديوهات anime3rb
        return emptyList()
    }

    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request {
        return GET("$baseUrl/?s=$query")
    }

    override fun searchAnimeParse(response: okhttp3.Response): AnimesPage {
        return popularAnimeParse(response)
    }

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$baseUrl/page/$page")
    }

    override fun latestUpdatesParse(response: okhttp3.Response): AnimesPage {
        return popularAnimeParse(response)
    }

    override fun animeDetailsParse(response: okhttp3.Response): SAnime {
        val document = Jsoup.parse(response.body!!.string())
        return SAnime.create().apply {
            title = document.selectFirst(".anime-title h1")?.text() ?: ""
            description = document.selectFirst(".anime-description")?.text()
            thumbnail_url = document.selectFirst(".anime-cover img")?.attr("src")
        }
    }

    override fun videoUrlParse(response: okhttp3.Response): Video? = null

    override fun getFilterList(): AnimeFilterList = AnimeFilterList()

    override fun headersBuilder(): Headers {
        return super.headersBuilder()
            .add("Referer", baseUrl)
    }
}
