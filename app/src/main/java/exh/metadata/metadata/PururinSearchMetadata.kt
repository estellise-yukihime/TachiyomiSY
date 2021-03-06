package exh.metadata.metadata

import android.content.Context
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.source.model.SManga
import exh.metadata.metadata.base.RaisedSearchMetadata
import kotlinx.serialization.Serializable

@Serializable
class PururinSearchMetadata : RaisedSearchMetadata() {
    var prId: Int? = null

    var prShortLink: String? = null

    var title by titleDelegate(TITLE_TYPE_TITLE)
    var altTitle by titleDelegate(TITLE_TYPE_ALT_TITLE)

    var thumbnailUrl: String? = null

    var uploaderDisp: String? = null

    var pages: Int? = null

    var fileSize: String? = null

    var ratingCount: Int? = null
    var averageRating: Double? = null

    override fun copyTo(manga: SManga) {
        prId?.let { prId ->
            prShortLink?.let { prShortLink ->
                manga.url = "/gallery/$prId/$prShortLink"
            }
        }

        (title ?: altTitle)?.let {
            manga.title = it
        }

        thumbnailUrl?.let {
            manga.thumbnail_url = it
        }

        manga.artist = tags.ofNamespace(TAG_NAMESPACE_ARTIST).joinToString { it.name }

        manga.genre = tagsToGenreString()

        /*val titleDesc = StringBuilder()
        title?.let { titleDesc += "English Title: $it\n" }
        altTitle?.let { titleDesc += "Japanese Title: $it\n" }

        val detailsDesc = StringBuilder()
        (uploaderDisp ?: uploader)?.let { detailsDesc += "Uploader: $it\n" }
        pages?.let { detailsDesc += "Length: $it pages\n" }
        fileSize?.let { detailsDesc += "Size: $it\n" }
        ratingCount?.let { detailsDesc += "Rating: $averageRating ($ratingCount)\n" }

        val tagsDesc = tagsToDescription()*/

        manga.description = "meta" /*listOf(titleDesc.toString(), detailsDesc.toString(), tagsDesc.toString())
            .filter(String::isNotBlank)
            .joinToString(separator = "\n")*/
    }

    override fun getExtraInfoPairs(context: Context): List<Pair<String, String>> {
        val pairs = mutableListOf<Pair<String, String>>()
        prId?.let { pairs += context.getString(R.string.id) to it.toString() }
        title?.let { pairs += context.getString(R.string.title) to it }
        altTitle?.let { pairs += context.getString(R.string.alt_title) to it }
        thumbnailUrl?.let { pairs += context.getString(R.string.thumbnail_url) to it }
        uploaderDisp?.let { pairs += context.getString(R.string.uploader_capital) to it }
        uploader?.let { pairs += context.getString(R.string.uploader) to it }
        pages?.let { pairs += context.getString(R.string.page_count) to it.toString() }
        fileSize?.let { pairs += context.getString(R.string.gallery_size) to it }
        ratingCount?.let { pairs += context.getString(R.string.total_ratings) to it.toString() }
        averageRating?.let { pairs += context.getString(R.string.average_rating) to it.toString() }
        return pairs
    }

    companion object {
        private const val TITLE_TYPE_TITLE = 0
        private const val TITLE_TYPE_ALT_TITLE = 1

        const val TAG_TYPE_DEFAULT = 0

        private const val TAG_NAMESPACE_ARTIST = "artist"
        const val TAG_NAMESPACE_CATEGORY = "category"

        val BASE_URL = "https://pururin.io"
    }
}
