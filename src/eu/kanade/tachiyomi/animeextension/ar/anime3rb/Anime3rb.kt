package eu.kanade.tachiyomi.animeextension.ar.anime3rb

import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.AnimesPage
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.animesource.AnimeHttpSource
import okhttp3.Request
import okhttp3.Response

class Anime3rb : AnimeHttpSource() {
    override val name = "Anime3rb"
    override val baseUrl = "https://anime3rb.com"
    override val lang = "ar"
    override val supportsLatest = true

    override fun popularAnimeRequest(page: Int): Request {
        return GET("$baseUrl/anime-list/page/$page/", headers)
    }

    override fun popularAnimeParse(response: Response): AnimesPage {
        val document = response.asJsoup()
        val animeList = document.select("div.anime-card-container").map {
            val link = it.selectFirst("a")!!
            val title = it.selectFirst(".anime-card-title")?.text().orEmpty()
            val url = link.attr("href")
            val thumbnail = link.selectFirst("img")?.attr("src").orEmpty()

            SAnime.create().apply {
                this.title = title
                this.setUrlWithoutDomain(url)
                this.thumbnail_url = thumbnail
            }
        }
        return AnimesPage(animeList, hasNextPage = true)
    }

    override fun episodeListRequest(anime: SAnime): Request {
        return GET(baseUrl + anime.url, headers)
    }

    override fun episodeListParse(response: Response): List<SEpisode> {
        val document = response.asJsoup()
        return document.select("ul.episodes-list li").map {
            val ep = SEpisode.create()
            ep.name = it.text()
            ep.episode_number = it.text().replace(Regex("\D"), "").toFloatOrNull() ?: 0F
            ep.setUrlWithoutDomain(it.selectFirst("a")!!.attr("href"))
            ep
        }
    }

    override fun videoListRequest(episode: SEpisode): Request {
        return GET(baseUrl + episode.url, headers)
    }

    override fun videoListParse(response: Response): List<Video> {
        val document = response.asJsoup()
        val iframe = document.selectFirst("iframe")
        val embedUrl = iframe?.attr("src") ?: return emptyList()
        return listOf(Video(embedUrl, "مشغل", embedUrl))
    }

    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request {
        return GET("$baseUrl/?s=$query", headers)
    }

    override fun searchAnimeParse(response: Response): AnimesPage {
        return popularAnimeParse(response)
    }
}