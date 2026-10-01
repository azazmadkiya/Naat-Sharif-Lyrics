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
  var name: String = "",
  var order: Int = 0
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

data class AppUserItem(
  var id: String = "",
  var email: String = "",
  var name: String = "",
  var role: String = "ADMIN", // "ADMIN", "ADD_NAAT", "VIEWER"
  var allowedCategories: List<String> = emptyList(), // e.g. ["hamd", "naat"]
  var createdAt: Long = System.currentTimeMillis(),
  var isActive: Boolean = true
) {
  fun isSuperAdmin(): Boolean = email.lowercase().trim() == "azazmadkiya@gmail.com"
  fun isAdminRole(): Boolean = isSuperAdmin() || role.equals("ADMIN", ignoreCase = true)
  fun isAddNaatRole(): Boolean = role.equals("ADD_NAAT", ignoreCase = true)
  fun isViewerRole(): Boolean = role.equals("VIEWER", ignoreCase = true)

  fun getRoleDisplayName(): String = when {
    isSuperAdmin() -> "Super Admin (Full)"
    isAdminRole() -> "Admin (Full Rights)"
    isAddNaatRole() -> "Editor (Add Naats)"
    isViewerRole() -> "Viewer (Only View)"
    else -> role
  }
}
