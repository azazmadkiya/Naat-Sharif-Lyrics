package com.example.model

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

fun Naat.toNaatItem(): NaatItem {
    val hLyrics = when {
        this.hindiLyrics.isNotBlank() -> this.hindiLyrics
        this.lyrics.isNotBlank() -> this.lyrics
        else -> ""
    }
    val gLyrics = when {
        this.gujaratiLyrics.isNotBlank() -> this.gujaratiLyrics
        else -> ""
    }
    val genericLyrics = when {
        this.lyrics.isNotBlank() -> this.lyrics
        this.hindiLyrics.isNotBlank() -> this.hindiLyrics
        this.gujaratiLyrics.isNotBlank() -> this.gujaratiLyrics
        else -> ""
    }
    return NaatItem(
        id = this.id,
        title = this.title,
        category = this.category,
        reciter = if (this.reciter.isBlank()) "Traditional" else this.reciter,
        lyrics = genericLyrics,
        hindiLyrics = hLyrics,
        gujaratiLyrics = gLyrics,
        arabicLyrics = this.arabicLyrics,
        isFavorite = this.isFavorite,
        addedBy = this.addedBy
    )
}

fun NaatItem.toNaat(): Naat {
    return Naat(
        id = this.id,
        title = this.title,
        category = this.category,
        reciter = this.reciter,
        lyrics = this.lyrics,
        hindiLyrics = this.hindiLyrics,
        gujaratiLyrics = this.gujaratiLyrics,
        arabicLyrics = this.arabicLyrics,
        isFavorite = this.isFavorite,
        addedBy = this.addedBy,
        timestamp = ""
    )
}
