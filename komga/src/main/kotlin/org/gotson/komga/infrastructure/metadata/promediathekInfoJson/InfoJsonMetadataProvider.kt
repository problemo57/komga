package org.gotson.komga.infrastructure.metadata.promediathekInfoJson

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.MetadataPatchTarget
import org.gotson.komga.domain.model.SeriesMetadata.ReadingDirection
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.domain.service.BookAnalyzer
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.gotson.komga.infrastructure.metadata.SeriesMetadataFromBookProvider
import org.gotson.komga.infrastructure.metadata.promediathekInfoJson.dto.BookJson
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}
private const val COMIC_INFO = "info.json"

@Service
class InfoJsonMetadataProvider(
  private val bookAnalyzer: BookAnalyzer,
  private val mapper: ObjectMapper,
) : BookMetadataProvider,
  SeriesMetadataFromBookProvider {
  override val capabilities =
    setOf(
      BookMetadataPatchCapability.TITLE,
      BookMetadataPatchCapability.SUMMARY,
      BookMetadataPatchCapability.NUMBER_SORT,
    )

  override fun getBookMetadataFromBook(book: BookWithMedia): BookMetadataPatch? {
    if (book.media.files.none { it.fileName == COMIC_INFO }) {
      logger.debug { "Book does not contain any $COMIC_INFO file: $book" }
      return null
    }

    logger.debug { "Get metadata from $COMIC_INFO for book: $book" }
    val fileContent = bookAnalyzer.getFileContent(book, COMIC_INFO)
    val metadata = mapper.readValue(fileContent, BookJson::class.java)

    return BookMetadataPatch(
      title = metadata.title,
      numberSort = metadata.sort_number.toFloat(),
    )
  }

  override val supportsAppendVolume = false

  override fun getSeriesMetadataFromBook(
    book: BookWithMedia,
    appendVolumeToTitle: Boolean,
  ): SeriesMetadataPatch? {
    if (book.media.files.none { it.fileName == COMIC_INFO }) {
      logger.debug { "Book does not contain any $COMIC_INFO file: $book" }
      return null
    }

    val fileContent = bookAnalyzer.getFileContent(book, COMIC_INFO)
    val bookMetadata = mapper.readValue(fileContent, BookJson::class.java)

    val bookReadingDirection =
      when (bookMetadata.reader_type) {
        "webtoon" -> ReadingDirection.WEBTOON
        "manga" -> ReadingDirection.RIGHT_TO_LEFT
        "manhwa" -> ReadingDirection.LEFT_TO_RIGHT
        else -> null
      }

    return SeriesMetadataPatch(
      title = null,
      readingDirection = bookReadingDirection,
      publisher = null,
      summary = null,
      language = null,
      totalBookCount = null,
      titleSort = null,
      status = null,
      ageRating = null,
      genres = null,
      collections = emptySet(),
    )
  }

  override fun shouldLibraryHandlePatch(
    library: Library,
    target: MetadataPatchTarget,
  ): Boolean =
    when (target) {
      MetadataPatchTarget.BOOK -> library.importPromediathekInfoJson
      MetadataPatchTarget.SERIES -> library.importPromediathekInfoJson
      else -> false
    }
}
