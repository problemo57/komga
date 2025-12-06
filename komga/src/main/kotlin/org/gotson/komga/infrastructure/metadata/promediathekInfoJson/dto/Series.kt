package org.gotson.komga.infrastructure.metadata.promediathekInfoJson.dto

data class BookJson(
  val provider: String,
  val provider_id: String,
  val title: String,
  val sort_number: Int,
  val reader_type: String,
)

data class SeriesJson(
  val provider: String,
  val provider_id: String,
  val title: String,
  val description: String,
  val language: String,
)
