package com.example.naatsharif.model

import com.google.firebase.firestore.PropertyName

data class Naat(
    var id: String = "",
    var title: String = "",
    var category: String = "naat",
    var reciter: String = "",
    var lyrics: String = "",
    var hindiLyrics: String = "",
    var gujaratiLyrics: String = "",
    var arabicLyrics: String = "",
    @get:PropertyName("isFavorite")
    @set:PropertyName("isFavorite")
    var isFavorite: Boolean = false,
    var addedBy: String = "",
    var timestamp: String = ""
)
