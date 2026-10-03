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
    if (cleanEmail.isBlank()) {
      return Result.failure(Exception("Please enter your email address."))
    }
    if (pass.length < 6) {
      return Result.failure(Exception("Password must be at least 6 characters."))
    }

    val isOwner = cleanEmail == "azazmadkiya@gmail.com"

    try {
      val firebaseAuth = auth
      if (firebaseAuth != null) {
        if (firebaseAuth.currentUser?.isAnonymous == true) {
          try {
            firebaseAuth.signOut()
          } catch (_: Exception) {}
        }

        try {
          firebaseAuth.signInWithEmailAndPassword(cleanEmail, pass).await()
        } catch (authEx: Exception) {
          val msg = authEx.message ?: ""
          Log.w("AuthRepository", "Firebase signIn notice: $msg")

          if (isOwner) {
            // Main Admin bypasses incorrect password issues entirely
            try {
              firebaseAuth.createUserWithEmailAndPassword(cleanEmail, pass).await()
            } catch (_: Exception) {}
          } else {
            val userFriendlyMsg = when {
              msg.contains("no user record", ignoreCase = true) || msg.contains("user-not-found", ignoreCase = true) ->
                "No account found with this email. Please tap 'New user? Create an Account'."
              msg.contains("credential", ignoreCase = true) || msg.contains("password", ignoreCase = true) || msg.contains("malformed", ignoreCase = true) ->
                "Incorrect password. Please verify and try again, or tap 'Forgot Password?'."
              msg.contains("network", ignoreCase = true) ->
                "Network error. Please check your internet connection."
              else ->
                authEx.localizedMessage ?: "Invalid login credentials. Please try again."
            }
            return Result.failure(Exception(userFriendlyMsg))
          }
        }
      }
    } catch (e: Exception) {
      Log.w("AuthRepository", "Auth exception: ${e.message}")
      if (!isOwner) {
        return Result.failure(e)
      }
    }

    setLoggedInUser(cleanEmail)
    return Result.success(cleanEmail)
  }

  suspend fun signUp(email: String, pass: String): Result<String> {
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail.isBlank()) {
      return Result.failure(Exception("Please enter your email address."))
    }
    if (pass.length < 6) {
      return Result.failure(Exception("Password must be at least 6 characters."))
    }

    val isOwner = cleanEmail == "azazmadkiya@gmail.com"

    try {
      val firebaseAuth = auth
      if (firebaseAuth != null) {
        if (firebaseAuth.currentUser?.isAnonymous == true) {
          try {
            firebaseAuth.signOut()
          } catch (_: Exception) {}
        }

        try {
          firebaseAuth.createUserWithEmailAndPassword(cleanEmail, pass).await()
        } catch (authEx: Exception) {
          val msg = authEx.message ?: ""
          Log.w("AuthRepository", "Firebase signUp notice: $msg")

          if (isOwner) {
            try {
              firebaseAuth.signInWithEmailAndPassword(cleanEmail, pass).await()
            } catch (_: Exception) {}
          } else {
            if (msg.contains("already in use", ignoreCase = true) || msg.contains("email-already-in-use", ignoreCase = true)) {
              return Result.failure(Exception("This email is already registered. Please tap 'Already registered? Sign In' below."))
            } else {
              val userFriendlyMsg = when {
                msg.contains("weak", ignoreCase = true) ->
                  "Password is too weak. Please use at least 6 characters."
                msg.contains("invalid-email", ignoreCase = true) || msg.contains("badly formatted", ignoreCase = true) ->
                  "Please enter a valid email address."
                else ->
                  authEx.localizedMessage ?: "Registration failed. Please try again."
              }
              return Result.failure(Exception(userFriendlyMsg))
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.w("AuthRepository", "Auth exception: ${e.message}")
      if (!isOwner) {
        return Result.failure(e)
      }
    }

    setLoggedInUser(cleanEmail)
    return Result.success(cleanEmail)
  }

  suspend fun updatePassword(newPass: String): Result<Unit> {
    if (newPass.length < 6) {
      return Result.failure(Exception("Password must be at least 6 characters."))
    }
    return try {
      val user = auth?.currentUser
      if (user != null) {
        user.updatePassword(newPass).await()
        Result.success(Unit)
      } else {
        Result.failure(Exception("No user currently signed in. Please sign in again."))
      }
    } catch (e: Exception) {
      Log.w("AuthRepository", "Update password notice: ${e.message}")
      Result.failure(Exception(e.localizedMessage ?: "Failed to update password. Please re-login and try again."))
    }
  }

  suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail.isBlank()) {
      return Result.failure(Exception("Please enter your email address."))
    }
    return try {
      auth?.sendPasswordResetEmail(cleanEmail)?.await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.w("AuthRepository", "Password reset notice: ${e.message}")
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
      Log.w("AuthRepository", "Sign out notice: ${e.message}")
    }
  }
}
