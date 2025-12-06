package org.gotson.komga.infrastructure.metadata.promediathekInfoJson

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.BCP47TagValidator
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.MetadataPatchTarget
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.domain.model.Sidecar
import org.gotson.komga.infrastructure.metadata.SeriesMetadataProvider
import org.gotson.komga.infrastructure.metadata.promediathekInfoJson.dto.SeriesJson
import org.gotson.komga.infrastructure.sidecar.SidecarSeriesConsumer
import org.springframework.stereotype.Service
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

private val logger = KotlinLogging.logger {}
private const val SERIES_JSON = "info.json"

@Service
class TitleJsonMetadataProvider(
  private val mapper: ObjectMapper,
) : SeriesMetadataProvider,
  SidecarSeriesConsumer {
  override fun getSeriesMetadata(series: Series): SeriesMetadataPatch? {
    val seriesPath = series.path
    if (seriesPath.listDirectoryEntries().none { it.name == SERIES_JSON }) {
      logger.debug { "Book does not belong to any series: $series" }
      return null
    }

    logger.debug { "Get metadata from $SERIES_JSON for series: $seriesPath" }
    val metadata = mapper.readValue(seriesPath.resolve(SERIES_JSON).toFile(), SeriesJson::class.java)

    return SeriesMetadataPatch(
      title = metadata.title,
      readingDirection = null,
      publisher = null,
      summary = metadata.description,
      language = BCP47TagValidator.normalize(metadata.language),
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
      MetadataPatchTarget.SERIES -> library.importPromediathekTitleInfoJson
      else -> false
    }

  override fun getSidecarSeriesType(): Sidecar.Type = Sidecar.Type.METADATA

  override fun getSidecarSeriesFilenames(): List<String> = listOf(SERIES_JSON)
}
