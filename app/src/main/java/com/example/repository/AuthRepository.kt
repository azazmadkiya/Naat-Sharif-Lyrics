package com.example.repository

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

  val currentUser: FirebaseUser?
    get() = try {
      auth?.currentUser
    } catch (e: Exception) {
      null
    }

  val isAdmin: Boolean
    get() = currentUser?.email?.lowercase()?.contains("admin") == true || currentUser?.email == "admin@naat.com"

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

  fun signOut() {
    try {
      auth?.signOut()
    } catch (e: Exception) {
      // Ignore
    }
  }
}
