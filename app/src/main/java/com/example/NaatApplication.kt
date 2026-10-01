package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
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

      // Auto sign-in anonymously if no user is signed in so reads work under authenticated rules
      try {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
          auth.signInAnonymously()
            .addOnSuccessListener {
              Log.d("NaatApp", "Anonymous auth success: ${it.user?.uid}")
            }
            .addOnFailureListener {
              Log.d("NaatApp", "Anonymous auth notice: ${it.message}")
            }
        }
      } catch (e: Exception) {
        Log.d("NaatApp", "Auth init notice: ${e.message}")
      }
    } catch (e: Exception) {
      Log.d("NaatApp", "Firebase init: ${e.message}")
    }
  }
}
