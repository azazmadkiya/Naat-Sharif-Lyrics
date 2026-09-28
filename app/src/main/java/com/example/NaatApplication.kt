package com.example

import android.app.Application
import com.google.firebase.FirebaseApp

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
    } catch (e: Exception) {
      // Handle initialization gracefully if already initialized or missing config
    }
  }
}
