package com.NaatSharif.Lyrics.azaz.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class Naat(
    // Document ID Firestore document body inda exclude maadalaagide
    @get:Exclude
    @set:Exclude
    var id: String = "",

    val title: String = "",
    val category: String = "naat",
    val reciter: String = "",
    val lyrics: String = "",
    val hindiLyrics: String = "",
    val gujaratiLyrics: String = "",
    val arabicLyrics: String = "",

    @get:PropertyName("isFavorite")
    @set:PropertyName("isFavorite")
    var isFavorite: Boolean = false,

    val addedBy: String = "",

    @ServerTimestamp
    val timestamp: Timestamp? = null
)
