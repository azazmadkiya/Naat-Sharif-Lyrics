package com.example.model

data class NaatItem(
  val id: String = "",
  val title: String = "",
  val category: String = "",
  val reciter: String = "Traditional",
  val lyrics: String = "",
  val hindiLyrics: String = "",
  val gujaratiLyrics: String = "",
  val arabicLyrics: String = "",
  val isFavorite: Boolean = false,
  val addedBy: String = ""
)

data class CategoryItem(
  val id: String = "",
  val title: String = "",
  val subtitle: String = "",
  val iconName: String = "",
  val count: Int = 0
)
