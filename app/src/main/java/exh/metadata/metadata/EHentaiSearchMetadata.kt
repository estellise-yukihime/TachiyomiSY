package exh.metadata.metadata

import android.content.Context
import androidx.core.net.toUri
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.source.model.SManga
import exh.metadata.MetadataUtil
import exh.metadata.metadata.base.RaisedSearchMetadata
import kotlinx.serialization.Serializable
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.Date

@Serializable
class EHentaiSearchMetadata : RaisedSearchMetadata() {
    var gId: String?
        get() = indexedExtra
        set(value) { indexedExtra = value }

    var gToken: String? = null
    var exh: Boolean? = null
    var thumbnailUrl: String? = null

    var title by titleDelegate(TITLE_TYPE_TITLE)
    var altTitle by titleDelegate(TITLE_TYPE_ALT_TITLE)

    var genre: String? = null

    var datePosted: Long? = null
    var parent: String? = null
    var visible: String? = null // Not a boolean
    var language: String? = null
    var translated: Boolean? = null
    var size: Long? = null
    var length: Int? = null
    var favorites: Int? = null
    var ratingCount: Int? = null
    var averageRating: Double? = null

    var aged: Boolean = false
    var lastUpdateCheck: Long = 0

    override fun copyTo(manga: SManga) {
        gId?.let { gId ->
            gToken?.let { gToken ->
                manga.url = idAndTokenToUrl(gId, gToken)
            }
        }
        thumbnailUrl?.let { manga.thumbnail_url = it }

        // No title bug?
        val titleObj = if (Injekt.get<PreferencesHelper>().useJapaneseTitle().get()) {
            altTitle ?: title
        } else {
            title
        }
        titleObj?.let { manga.title = it }

        // Set artist (if we can find one)
        tags.filter { it.namespace == EH_ARTIST_NAMESPACE }.let {
            if (it.isNotEmpty()) manga.artist = it.joinToString(transform = { it.name })
        }

        // Copy tags -> genres
        manga.genre = tagsToGenreString()

        // Try to automatically identify if it is ongoing, we try not to be too lenient here to avoid making mistakes
        // We default to completed
        manga.status = SManga.COMPLETED
        title?.let { t ->
            MetadataUtil.ONGOING_SUFFIX.find {
                t.endsWith(it, ignoreCase = true)
            }?.let {
                manga.status = SManga.ONGOING
            }
        }

        // Build a nice looking description out of what we know
        /* val titleDesc = StringBuilder()
        title?.let { titleDesc += "Title: $it\n" }
        altTitle?.let { titleDesc += "Alternate Title: $it\n" }

        val detailsDesc = StringBuilder()
        genre?.let { detailsDesc += "Genre: $it\n" }
        uploader?.let { detailsDesc += "Uploader: $it\n" }
        datePosted?.let { detailsDesc += "Posted: ${EX_DATE_FORMAT.format(Date(it))}\n" }
        visible?.let { detailsDesc += "Visible: $it\n" }
        language?.let {
            detailsDesc += "Language: $it"
            if (translated == true) detailsDesc += " TR"
            detailsDesc += "\n"
        }
        size?.let { detailsDesc += "File size: ${humanReadableByteCount(it, true)}\n" }
        length?.let { detailsDesc += "Length: $it pages\n" }
        favorites?.let { detailsDesc += "Favorited: $it times\n" }
        averageRating?.let {
            detailsDesc += "Rating: $it"
            ratingCount?.let { detailsDesc += " ($it)" }
            detailsDesc += "\n"
        }

        val tagsDesc = tagsToDescription()*/

        manga.description = "meta" /*listOf(titleDesc.toString(), detailsDesc.toString(), tagsDesc.toString())
            .filter(String::isNotBlank)
            .joinToString(separator = "\n")*/
    }

    override fun getExtraInfoPairs(context: Context): List<Pair<String, String>> {
        val pairs = mutableListOf<Pair<String, String>>()

        gId?.let { pairs += context.getString(R.string.id) to it }
        gToken?.let { pairs += context.getString(R.string.token) to it }
        exh?.let { pairs += context.getString(R.string.is_exhentai_gallery) to context.getString(if (it) android.R.string.yes else android.R.string.no) }
        thumbnailUrl?.let { pairs += context.getString(R.string.thumbnail_url) to it }
        title?.let { pairs += context.getString(R.string.title) to it }
        altTitle?.let { pairs += context.getString(R.string.alt_title) to it }
        genre?.let { pairs += context.getString(R.string.genre) to it }
        datePosted?.let { pairs += context.getString(R.string.date_posted) to MetadataUtil.EX_DATE_FORMAT.format(Date(it)) }
        parent?.let { pairs += context.getString(R.string.parent) to it }
        visible?.let { pairs += context.getString(R.string.visible) to it }
        language?.let { pairs += context.getString(R.string.language) to it }
        translated?.let { pairs += "Translated" to context.getString(if (it) android.R.string.yes else android.R.string.no) }
        size?.let { pairs += context.getString(R.string.gallery_size) to MetadataUtil.humanReadableByteCount(it, true) }
        length?.let { pairs += context.getString(R.string.page_count) to it.toString() }
        favorites?.let { pairs += context.getString(R.string.total_favorites) to it.toString() }
        ratingCount?.let { pairs += context.getString(R.string.total_ratings) to it.toString() }
        averageRating?.let { pairs += context.getString(R.string.average_rating) to it.toString() }
        aged.let { pairs += context.getString(R.string.aged) to context.getString(if (it) android.R.string.yes else android.R.string.no) }
        lastUpdateCheck.let { pairs += context.getString(R.string.last_update_check) to MetadataUtil.EX_DATE_FORMAT.format(Date(it)) }

        return pairs
    }

    companion object {
        private const val TITLE_TYPE_TITLE = 0
        private const val TITLE_TYPE_ALT_TITLE = 1

        const val TAG_TYPE_NORMAL = 0
        const val TAG_TYPE_LIGHT = 1
        const val TAG_TYPE_WEAK = 2

        const val EH_GENRE_NAMESPACE = "genre"
        private const val EH_ARTIST_NAMESPACE = "artist"

        private fun splitGalleryUrl(url: String) =
            url.let {
                // Only parse URL if is full URL
                val pathSegments = if (it.startsWith("http")) {
                    it.toUri().pathSegments
                } else {
                    it.split('/')
                }
                pathSegments.filterNot(String::isNullOrBlank)
            }

        fun galleryId(url: String): String = splitGalleryUrl(url)[1]

        fun galleryToken(url: String): String =
            splitGalleryUrl(url)[2]

        fun normalizeUrl(url: String) =
            idAndTokenToUrl(galleryId(url), galleryToken(url))

        fun idAndTokenToUrl(id: String, token: String) =
            "/g/$id/$token/?nw=always"
    }
}
