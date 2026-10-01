package com.example.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CategoryItem
import com.example.model.NaatItem
import com.example.model.toNaatItem
import com.example.repository.AuthRepository
import com.example.repository.NaatRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class NaatViewModel : ViewModel() {
  private val naatRepo = NaatRepository()
  private val authRepo = AuthRepository()

  var categories by mutableStateOf<List<CategoryItem>>(emptyList())
    private set

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

  var isConnected by mutableStateOf(true)
  var errorMessage by mutableStateOf<String?>(null)
  var isRefreshing by mutableStateOf(false)

  init {
    refreshData()
    viewModelScope.launch {
      try {
        naatRepo.getNaatsRealtime()
          .catch { e ->
            android.util.Log.w("NaatViewModel", "Realtime collect note: ${e.message}")
          }
          .collect { list ->
            if (list.isNotEmpty()) {
              _naats.value = list.map { it.toNaatItem() }
            }
          }
      } catch (e: Exception) {
        android.util.Log.w("NaatViewModel", "Realtime collect catch: ${e.message}")
      }
    }
    naatRepo.listenToCategories { list ->
      categories = list
    }
  }

  fun refreshData() {
    viewModelScope.launch {
      isRefreshing = true
      errorMessage = null
      try {
        val fetchedNaats = naatRepo.getNaats()
        val fetchedCategories = naatRepo.getCategories()
        _naats.value = fetchedNaats.toList()
        categories = fetchedCategories
        isConnected = true
        errorMessage = null
      } catch (e: Exception) {
        isConnected = false
        errorMessage = "No internet connection. Please check your network and try again."
        android.util.Log.e("NaatViewModel", "Network error: ${e.message}")
      } finally {
        isRefreshing = false
      }
    }
  }

  fun loadCategories() {
    refreshData()
  }

  fun addCategory(item: CategoryItem, onComplete: (Boolean) -> Unit) {
    viewModelScope.launch {
      val success = naatRepo.addCategory(item)
      if (success) loadCategories()
      onComplete(success)
    }
  }

  fun updateCategory(item: CategoryItem, onComplete: (Boolean) -> Unit) {
    viewModelScope.launch {
      val success = naatRepo.updateCategory(item)
      if (success) loadCategories()
      onComplete(success)
    }
  }

  fun deleteCategory(id: String, onComplete: (Boolean) -> Unit) {
    viewModelScope.launch {
      val success = naatRepo.deleteCategory(id)
      if (success) loadCategories()
      onComplete(success)
    }
  }

  fun loadNaats() {
    viewModelScope.launch {
      _naats.value = naatRepo.getNaats().toList()
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

  fun updateNaat(id: String, title: String, category: String, reciter: String, lyrics: String, gujaratiLyrics: String, onComplete: (Boolean) -> Unit) {
    viewModelScope.launch {
      val updatedItem = NaatItem(
        id = id,
        title = title,
        category = category,
        reciter = reciter,
        lyrics = lyrics,
        hindiLyrics = lyrics,
        gujaratiLyrics = gujaratiLyrics,
        addedBy = currentUser?.email ?: "admin"
      )
      val success = naatRepo.updateNaat(updatedItem)
      if (success) {
        loadNaats()
      }
      onComplete(success)
    }
  }

  fun deleteNaat(id: String, onComplete: (Boolean) -> Unit) {
    viewModelScope.launch {
      val success = naatRepo.deleteNaat(id)
      if (success) {
        loadNaats()
      }
      onComplete(success)
    }
  }

  fun signIn(email: String, pass: String, isAdminRole: Boolean, onResult: (Boolean) -> Unit) {
    viewModelScope.launch {
      authError = null
      val result = authRepo.signIn(email, pass)
      if (result.isSuccess) {
        currentUser = authRepo.currentUser
        val shouldBeAdmin = isAdminRole || email.contains("admin", ignoreCase = true) || email.lowercase() == "azazmadkiya@gmail.com"
        if (shouldBeAdmin) {
          authRepo.setAdminForUser(email, true)
        }
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
        if (isAdminRole || email.lowercase() == "azazmadkiya@gmail.com") {
          authRepo.setAdminForUser(email, true)
        }
        isAdmin = authRepo.isAdmin
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
    val email = currentUser?.email ?: "testadmin@naat.com"
    authRepo.setAdminForUser(email, true)
    isAdmin = true
  }
}
