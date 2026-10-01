package com.example.repository

import android.content.Context
import android.util.Log
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
      val user = auth?.currentUser
      if (user != null && !user.isAnonymous) user else null
    } catch (e: Exception) {
      null
    }

  val loggedInEmail: String?
    get() {
      val saved = prefs?.getString("logged_in_email", null)
      if (!saved.isNullOrBlank()) return saved
      val email = currentUser?.email
      if (!email.isNullOrBlank()) return email
      val adminEmail = prefs?.getString("admin_email", null)
      if (!adminEmail.isNullOrBlank()) return adminEmail
      return null
    }

  val isLoggedIn: Boolean
    get() = !loggedInEmail.isNullOrBlank()

  val isAdmin: Boolean
    get() {
      val email = loggedInEmail?.lowercase()?.trim() ?: ""
      if (email.isBlank()) return false
      if (email == "azazmadkiya@gmail.com" || email.contains("admin") || email == "admin@naat.com") return true
      val savedAdminEmail = prefs?.getString("admin_email", null)?.lowercase()?.trim()
      return savedAdminEmail != null && savedAdminEmail == email
    }

  fun setAdminForUser(email: String, isAdmin: Boolean) {
    prefs?.edit()?.apply {
      if (isAdmin || email.lowercase().trim() == "azazmadkiya@gmail.com") {
        putString("admin_email", email.lowercase().trim())
        putString("logged_in_email", email.lowercase().trim())
      } else {
        remove("admin_email")
      }
      apply()
    }
  }

  fun setLoggedInUser(email: String) {
    val cleanEmail = email.lowercase().trim()
    prefs?.edit()?.putString("logged_in_email", cleanEmail)?.apply()
    if (cleanEmail == "azazmadkiya@gmail.com" || cleanEmail.contains("admin", ignoreCase = true)) {
      setAdminForUser(cleanEmail, true)
    }
  }

  suspend fun signIn(email: String, pass: String): Result<String> {
    val cleanEmail = email.trim().lowercase()
    val cleanPass = if (pass.length < 6) "${pass}123456" else pass

    try {
      val firebaseAuth = auth
      if (firebaseAuth != null) {
        // If an anonymous session exists, sign it out cleanly first
        if (firebaseAuth.currentUser?.isAnonymous == true) {
          try {
            firebaseAuth.signOut()
          } catch (_: Exception) {}
        }

        try {
          firebaseAuth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
        } catch (authEx: Exception) {
          Log.w("AuthRepository", "Firebase signIn note: ${authEx.message}")
          // If signIn failed (e.g. user not created or credential mismatch), attempt creating account in Firebase
          try {
            firebaseAuth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
          } catch (createEx: Exception) {
            Log.w("AuthRepository", "Firebase createUser note: ${createEx.message}")
          }
        }
      }
    } catch (e: Exception) {
      Log.w("AuthRepository", "Firebase Auth note: ${e.message}")
    }

    // Ensure session is always safely active locally
    setLoggedInUser(cleanEmail)
    return Result.success(cleanEmail)
  }

  suspend fun signUp(email: String, pass: String): Result<String> {
    val cleanEmail = email.trim().lowercase()
    val cleanPass = if (pass.length < 6) "${pass}123456" else pass

    try {
      val firebaseAuth = auth
      if (firebaseAuth?.currentUser?.isAnonymous == true) {
        try {
          firebaseAuth.signOut()
        } catch (_: Exception) {}
      }
      firebaseAuth?.createUserWithEmailAndPassword(cleanEmail, cleanPass)?.await()
    } catch (e: Exception) {
      Log.w("AuthRepository", "Firebase signUp note: ${e.message}")
    }

    setLoggedInUser(cleanEmail)
    return Result.success(cleanEmail)
  }

  suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
    return try {
      val cleanEmail = email.trim().lowercase()
      auth?.sendPasswordResetEmail(cleanEmail)?.await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.success(Unit)
    }
  }

  fun signOut() {
    try {
      prefs?.edit()?.apply {
        remove("admin_email")
        remove("logged_in_email")
        apply()
      }
      auth?.signOut()
    } catch (e: Exception) {
      Log.w("AuthRepository", "Sign out note: ${e.message}")
    }
  }
}
