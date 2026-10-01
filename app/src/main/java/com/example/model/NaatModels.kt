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
  var id: String = "",
  var title: String = "",
  var subtitle: String = "",
  var iconName: String = "",
  var count: Int = 0,
  var name: String = ""
) {
  fun getDisplayTitle(): String {
    if (title.isNotBlank()) return title
    if (name.isNotBlank()) return name
    return when (id.lowercase().trim()) {
      "hamd" -> "हम्द शरीफ"
      "naat" -> "नात शरीफ"
      "manqabat" -> "मनक़बत शरीफ"
      "panjtan" -> "पंजतन पाक"
      "gouse", "ghous", "gouse_azam" -> "गौसे आज़म"
      "gareeb", "garib_nawaz", "gareeb_nawaz" -> "गरीब नवाज़"
      "tazeem" -> "तज़ीम कलाम"
      "raza", "kalam_e_raza", "kalame_raza" -> "कलामे रज़ा"
      else -> id.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
  }

  fun getDisplaySubtitle(): String {
    if (subtitle.isNotBlank()) return subtitle
    return when (id.lowercase().trim()) {
      "hamd" -> "Hamd Sharif"
      "naat" -> "Naat Sharif"
      "manqabat" -> "Manqabat Sharif"
      "panjtan" -> "Panjtan Pak"
      "gouse", "ghous", "gouse_azam" -> "Gouse Azam"
      "gareeb", "garib_nawaz", "gareeb_nawaz" -> "Gareeb Nawaz"
      "tazeem" -> "Tazeem Kalam"
      "raza", "kalam_e_raza", "kalame_raza" -> "Kalame Raza"
      else -> ""
    }
  }
}
