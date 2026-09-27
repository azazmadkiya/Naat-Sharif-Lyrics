package com.example

import android.app.Application
import com.google.firebase.FirebaseApp

class NaatApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    try {
      FirebaseApp.initializeApp(this)
    } catch (e: Exception) {
      // Handle initialization gracefully if already initialized or missing config
    }
  }
}
