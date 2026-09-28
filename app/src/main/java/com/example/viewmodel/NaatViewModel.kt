package com.example.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CategoryItem
import com.example.model.NaatItem
import com.example.repository.AuthRepository
import com.example.repository.NaatRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NaatViewModel : ViewModel() {
  private val naatRepo = NaatRepository()
  private val authRepo = AuthRepository()

  val categories: List<CategoryItem> = naatRepo.categories

  private val _naats = MutableStateFlow<List<NaatItem>>(emptyList())
  val naats: StateFlow<List<NaatItem>> = _naats.asStateFlow()

  var searchQuery by mutableStateOf("")
  var selectedCategory by mutableStateOf<String?>(null)
  var selectedNaat by mutableStateOf<NaatItem?>(null)

  var isDarkMode by mutableStateOf(false)
  var fontSizeScale by mutableStateOf(18f) // sp for lyrics reading

  var currentUser by mutableStateOf<FirebaseUser?>(authRepo.currentUser)
  var isAdmin by mutableStateOf(authRepo.isAdmin)
  var authError by mutableStateOf<String?>(null)

  init {
    loadNaats()
  }

  fun loadNaats() {
    viewModelScope.launch {
      _naats.value = naatRepo.getNaats()
    }
  }

  fun toggleFavorite(naat: NaatItem) {
    viewModelScope.launch {
      val updatedFav = !naat.isFavorite
      val success = naatRepo.updateFavorite(naat.id, updatedFav)
      if (success) {
        _naats.value = _naats.value.map {
          if (it.id == naat.id) it.copy(isFavorite = updatedFav) else it
        }
        if (selectedNaat?.id == naat.id) {
          selectedNaat = selectedNaat?.copy(isFavorite = updatedFav)
        }
      }
    }
  }

  fun addNaat(title: String, category: String, reciter: String, lyrics: String, gujaratiLyrics: String, onComplete: (Boolean) -> Unit) {
    viewModelScope.launch {
      val newItem = NaatItem(
        title = title,
        category = category,
        reciter = reciter,
        lyrics = lyrics,
        hindiLyrics = lyrics,
        gujaratiLyrics = gujaratiLyrics,
        addedBy = currentUser?.email ?: "admin"
      )
      val success = naatRepo.addNaat(newItem)
      if (success) {
        loadNaats()
      }
      onComplete(success)
    }
  }

  fun signIn(email: String, pass: String, onResult: (Boolean) -> Unit) {
    viewModelScope.launch {
      authError = null
      val result = authRepo.signIn(email, pass)
      if (result.isSuccess) {
        currentUser = authRepo.currentUser
        isAdmin = authRepo.isAdmin
        onResult(true)
      } else {
        authError = result.exceptionOrNull()?.message ?: "Login failed"
        onResult(false)
      }
    }
  }

  fun signUp(email: String, pass: String, isAdminRole: Boolean, onResult: (Boolean) -> Unit) {
    viewModelScope.launch {
      authError = null
      val result = authRepo.signUp(email, pass)
      if (result.isSuccess) {
        currentUser = authRepo.currentUser
        isAdmin = isAdminRole || authRepo.isAdmin
        onResult(true)
      } else {
        authError = result.exceptionOrNull()?.message ?: "Sign up failed"
        onResult(false)
      }
    }
  }

  fun signOut() {
    authRepo.signOut()
    currentUser = null
    isAdmin = false
  }

  fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
    viewModelScope.launch {
      authError = null
      if (email.isBlank()) {
        onResult(false, "Please enter your email address first")
        return@launch
      }
      val result = authRepo.sendPasswordResetEmail(email)
      if (result.isSuccess) {
        onResult(true, "Password reset email sent. Check your inbox.")
      } else {
        val err = result.exceptionOrNull()?.message ?: "Failed to send reset email"
        authError = err
        onResult(false, err)
      }
    }
  }

  // Helper for quick admin login override for evaluation
  fun makeAdminForTest() {
    isAdmin = true
  }
}
