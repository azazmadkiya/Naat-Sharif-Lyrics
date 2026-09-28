package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

class NaatApplication : Application() {
  companion object {
    lateinit var instance: NaatApplication
      private set
  }

  override fun onCreate() {
    super.onCreate()
    instance = this
    try {
      FirebaseApp.initializeApp(this)
      val db = FirebaseFirestore.getInstance()
      val settings = FirebaseFirestoreSettings.Builder()
        .setLocalCacheSettings(
          PersistentCacheSettings.newBuilder()
            .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        )
        .build()
      db.firestoreSettings = settings
    } catch (e: Exception) {
      // Handle initialization gracefully if already initialized or missing config
    }
  }
}
