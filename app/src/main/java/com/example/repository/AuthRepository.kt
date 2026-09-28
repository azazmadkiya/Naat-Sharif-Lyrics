package com.example.repository

import android.content.Context
import com.example.NaatApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {
  private val auth by lazy {
    try {
      FirebaseAuth.getInstance()
    } catch (e: Exception) {
      null
    }
  }

  private val prefs by lazy {
    try {
      NaatApplication.instance.getSharedPreferences("NaatAdminPrefs", Context.MODE_PRIVATE)
    } catch (e: Exception) {
      null
    }
  }

  val currentUser: FirebaseUser?
    get() = try {
      auth?.currentUser
    } catch (e: Exception) {
      null
    }

  val isAdmin: Boolean
    get() {
      val user = currentUser ?: return false
      val email = user.email?.lowercase() ?: ""
      if (email.contains("admin") || email == "admin@naat.com") return true
      val savedAdminEmail = prefs?.getString("admin_email", null)
      return savedAdminEmail != null && savedAdminEmail == email
    }

  fun setAdminForUser(email: String, isAdmin: Boolean) {
    prefs?.edit()?.apply {
      if (isAdmin) {
        putString("admin_email", email.lowercase())
      } else {
        remove("admin_email")
      }
      apply()
    }
  }

  suspend fun signIn(email: String, pass: String): Result<FirebaseUser?> {
    return try {
      val firebaseAuth = auth ?: return Result.failure(Exception("Firebase Auth not initialized"))
      val res = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
      Result.success(res.user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signUp(email: String, pass: String): Result<FirebaseUser?> {
    return try {
      val firebaseAuth = auth ?: return Result.failure(Exception("Firebase Auth not initialized"))
      val res = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
      Result.success(res.user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
    return try {
      val firebaseAuth = auth ?: return Result.failure(Exception("Firebase Auth not initialized"))
      firebaseAuth.sendPasswordResetEmail(email).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun signOut() {
    try {
      prefs?.edit()?.remove("admin_email")?.apply()
      auth?.signOut()
    } catch (e: Exception) {
      // Ignore
    }
  }
}
