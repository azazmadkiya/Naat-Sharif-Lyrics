package com.example.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.NaatApplication
import com.example.model.AppUserItem
import com.example.model.CategoryItem
import com.example.model.NaatItem
import com.example.model.toNaatItem
import com.example.repository.AuthRepository
import com.example.repository.FavoritesRepository
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
  private val favoritesRepo = FavoritesRepository()

  var categories by mutableStateOf<List<CategoryItem>>(naatRepo.defaultCategories)
    private set

  var users by mutableStateOf<List<AppUserItem>>(naatRepo.defaultUsers)
    private set

  private val _naats = MutableStateFlow<List<NaatItem>>(emptyList())
  val naats: StateFlow<List<NaatItem>> = _naats.asStateFlow()

  var searchQuery by mutableStateOf("")
  var selectedCategory by mutableStateOf<String?>(null)
  var selectedNaat by mutableStateOf<NaatItem?>(null)

  var isDarkMode by mutableStateOf(false)
  var fontSizeScale by mutableStateOf(18f) // sp for lyrics reading

  var currentUser by mutableStateOf<FirebaseUser?>(authRepo.currentUser)
  var isLoggedIn by mutableStateOf(authRepo.isLoggedIn)
  var loggedInEmail by mutableStateOf(authRepo.loggedInEmail)
  var isAdmin by mutableStateOf(authRepo.isAdmin)
  var authError by mutableStateOf<String?>(null)

  var sessionIdleTimeoutMinutes by mutableStateOf(getStoredIdleTimeoutMinutes())
    private set
  var showSessionExpiredDialog by mutableStateOf(false)
  private var lastInteractionTime = System.currentTimeMillis()

  private fun getStoredIdleTimeoutMinutes(): Int {
    try {
      val prefs = NaatApplication.instance.getSharedPreferences("NaatAdminPrefs", Context.MODE_PRIVATE)
      return prefs.getInt("session_idle_timeout_mins", 15) // default 15 minutes
    } catch (_: Exception) {
      return 15
    }
  }

  fun setSessionIdleTimeout(minutes: Int) {
    sessionIdleTimeoutMinutes = minutes
    try {
      val prefs = NaatApplication.instance.getSharedPreferences("NaatAdminPrefs", Context.MODE_PRIVATE)
      prefs.edit().putInt("session_idle_timeout_mins", minutes).apply()
    } catch (_: Exception) {}
    lastInteractionTime = System.currentTimeMillis()
  }

  fun recordUserInteraction() {
    lastInteractionTime = System.currentTimeMillis()
  }

  var isConnected by mutableStateOf(true)
  var errorMessage by mutableStateOf<String?>(null)
  var isRefreshing by mutableStateOf(false)

  init {
    // Idle session watchdog coroutine
    viewModelScope.launch {
      while (true) {
        kotlinx.coroutines.delay(30_000L) // check every 30 seconds
        val timeoutMins = sessionIdleTimeoutMinutes
        if (timeoutMins > 0 && isLoggedIn) {
          val idleDuration = System.currentTimeMillis() - lastInteractionTime
          val timeoutMs = timeoutMins * 60 * 1000L
          if (idleDuration > timeoutMs) {
            authRepo.signOut()
            isLoggedIn = false
            loggedInEmail = null
            isAdmin = false
            currentUser = null
            showSessionExpiredDialog = true
            lastInteractionTime = System.currentTimeMillis()
          }
        }
      }
    }
    // 1. Sync & listen to user favorites across devices via Firestore
    favoritesRepo.syncUserFavorites(loggedInEmail ?: currentUser?.email)
    viewModelScope.launch {
      favoritesRepo.favoriteIds.collect { favSet ->
        _naats.value = _naats.value.map { it.copy(isFavorite = favSet.contains(it.id)) }
        if (selectedNaat != null) {
          selectedNaat = selectedNaat?.copy(isFavorite = favSet.contains(selectedNaat!!.id))
        }
      }
    }

    refreshData()
    viewModelScope.launch {
      try {
        naatRepo.getNaatsRealtime()
          .catch { e ->
            android.util.Log.w("NaatViewModel", "Realtime collect note: ${e.message}")
          }
          .collect { list ->
            _naats.value = applyFavorites(list.map { it.toNaatItem() })
          }
      } catch (e: Exception) {
        android.util.Log.w("NaatViewModel", "Realtime collect catch: ${e.message}")
      }
    }
    naatRepo.listenToCategories { list ->
      categories = list
    }
    naatRepo.listenToUsers { list ->
      users = list
    }
  }

  fun applyFavorites(items: List<NaatItem>): List<NaatItem> {
    val favSet = favoritesRepo.favoriteIds.value
    return items.map { it.copy(isFavorite = favSet.contains(it.id)) }
  }

  fun refreshData() {
    viewModelScope.launch {
      isRefreshing = true
      errorMessage = null
      try {
        val fetchedNaats = naatRepo.getNaats()
        val fetchedCategories = naatRepo.getCategories()
        val fetchedUsers = naatRepo.getUsers()
        _naats.value = applyFavorites(fetchedNaats.toList())
        categories = fetchedCategories
        users = fetchedUsers
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

  // ==================== User Rights & RBAC Helpers ====================

  fun getCurrentUserItem(): AppUserItem {
    val email = loggedInEmail?.lowercase()?.trim() ?: currentUser?.email?.lowercase()?.trim() ?: ""
    if (email == "azazmadkiya@gmail.com") {
      return AppUserItem(
        id = "admin_owner",
        email = "azazmadkiya@gmail.com",
        name = "Azaz Madkiya (Owner)",
        role = "ADMIN",
        allowedCategories = emptyList(),
        isActive = true
      )
    }
    val matched = users.find { it.email.equals(email, ignoreCase = true) }
    if (matched != null) {
      try {
        val prefs = NaatApplication.instance.getSharedPreferences("NaatAdminPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit()
          .putString("user_role_$email", matched.role)
          .putString("user_name_$email", matched.name)
          .putString("user_cats_$email", matched.allowedCategories.joinToString(","))
          .apply()
      } catch (_: Exception) {}
      return matched
    }

    // Check SharedPreferences cache for assigned rights across logout/login and offline
    try {
      val prefs = NaatApplication.instance.getSharedPreferences("NaatAdminPrefs", android.content.Context.MODE_PRIVATE)
      val cachedRole = prefs.getString("user_role_$email", null)
      if (!cachedRole.isNullOrBlank()) {
        val cachedName = prefs.getString("user_name_$email", email) ?: email
        val cachedCatsStr = prefs.getString("user_cats_$email", "") ?: ""
        val cachedCats = if (cachedCatsStr.isNotBlank()) cachedCatsStr.split(",").map { it.trim() }.filter { it.isNotBlank() } else emptyList()
        return AppUserItem(
          id = "user_cached_$email",
          email = email,
          name = cachedName,
          role = cachedRole,
          allowedCategories = cachedCats,
          isActive = true
        )
      }
    } catch (_: Exception) {}

    // Fallback if marked as admin in local preferences
    return if (isAdmin) {
      AppUserItem(
        id = "admin_local",
        email = email.ifBlank { "admin" },
        name = "Admin User",
        role = "ADMIN",
        allowedCategories = emptyList(),
        isActive = true
      )
    } else {
      AppUserItem(
        id = "viewer_local",
        email = email.ifBlank { "guest" },
        name = "App User",
        role = "VIEWER",
        allowedCategories = emptyList(),
        isActive = true
      )
    }
  }

  fun canManageUsers(): Boolean {
    val user = getCurrentUserItem()
    return user.isSuperAdmin() || user.isAdminRole()
  }

  fun canManageCategories(): Boolean {
    val user = getCurrentUserItem()
    return user.isSuperAdmin() || user.isAdminRole()
  }

  fun canAddNaat(categoryId: String = ""): Boolean {
    val user = getCurrentUserItem()
    if (user.isSuperAdmin() || user.isAdminRole()) return true
    if (user.isOnlyAddRole() || user.isAddNaatRole()) {
      if (categoryId.isBlank()) return true
      return user.allowedCategories.isEmpty() || user.allowedCategories.any { it.equals(categoryId, ignoreCase = true) }
    }
    return false
  }

  fun canEditNaat(categoryId: String = ""): Boolean {
    val user = getCurrentUserItem()
    if (user.isOnlyAddRole()) return false
    if (user.isSuperAdmin() || user.isAdminRole()) return true
    if (user.role.equals("ADD_NAAT", ignoreCase = true)) {
      if (categoryId.isBlank()) return true
      return user.allowedCategories.isEmpty() || user.allowedCategories.any { it.equals(categoryId, ignoreCase = true) }
    }
    return false
  }

  fun canDeleteNaat(): Boolean {
    val user = getCurrentUserItem()
    if (user.isOnlyAddRole()) return false
    return user.isSuperAdmin() || user.isAdminRole()
  }

  fun isViewOnly(): Boolean {
    val user = getCurrentUserItem()
    return user.isViewerRole()
  }

  fun addUser(name: String, email: String, role: String, allowedCategories: List<String>, onComplete: (Boolean) -> Unit) {
    viewModelScope.launch {
      val cleanEmail = email.trim().lowercase()
      try {
        val prefs = NaatApplication.instance.getSharedPreferences("NaatAdminPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit()
          .putString("user_role_$cleanEmail", role)
          .putString("user_name_$cleanEmail", name.trim())
          .putString("user_cats_$cleanEmail", allowedCategories.joinToString(","))
          .apply()
      } catch (_: Exception) {}

      val newUser = AppUserItem(
        id = "user_${System.currentTimeMillis()}",
        name = name.trim(),
        email = cleanEmail,
        role = role,
        allowedCategories = allowedCategories,
        createdAt = System.currentTimeMillis(),
        isActive = true
      )
      val success = naatRepo.addUser(newUser)
      if (success) {
        users = naatRepo.getUsers()
      }
      onComplete(success)
    }
  }

  fun updateUser(id: String, name: String, email: String, role: String, allowedCategories: List<String>, onComplete: (Boolean) -> Unit) {
    viewModelScope.launch {
      val cleanEmail = email.trim().lowercase()
      try {
        val prefs = NaatApplication.instance.getSharedPreferences("NaatAdminPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit()
          .putString("user_role_$cleanEmail", role)
          .putString("user_name_$cleanEmail", name.trim())
          .putString("user_cats_$cleanEmail", allowedCategories.joinToString(","))
          .apply()
      } catch (_: Exception) {}

      val updatedUser = AppUserItem(
        id = id,
        name = name.trim(),
        email = cleanEmail,
        role = role,
        allowedCategories = allowedCategories,
        createdAt = System.currentTimeMillis(),
        isActive = true
      )
      val success = naatRepo.updateUser(updatedUser)
      if (success) {
        users = naatRepo.getUsers()
      }
      onComplete(success)
    }
  }

  fun deleteUser(id: String, onComplete: (Boolean) -> Unit) {
    viewModelScope.launch {
      val success = naatRepo.deleteUser(id)
      if (success) {
        users = naatRepo.getUsers()
      }
      onComplete(success)
    }
  }

  // ==================== Category Operations ====================

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

  fun moveCategoryUp(index: Int) {
    if (index <= 0 || index >= categories.size) return
    val list = categories.toMutableList()
    val item = list.removeAt(index)
    list.add(index - 1, item)
    val reordered = list.mapIndexed { idx, cat -> cat.copy(order = idx) }
    categories = reordered
    viewModelScope.launch {
      naatRepo.saveCategoriesOrder(reordered)
    }
  }

  fun moveCategoryDown(index: Int) {
    if (index < 0 || index >= categories.size - 1) return
    val list = categories.toMutableList()
    val item = list.removeAt(index)
    list.add(index + 1, item)
    val reordered = list.mapIndexed { idx, cat -> cat.copy(order = idx) }
    categories = reordered
    viewModelScope.launch {
      naatRepo.saveCategoriesOrder(reordered)
    }
  }

  // ==================== Naat Operations ====================

  fun loadNaats() {
    viewModelScope.launch {
      _naats.value = applyFavorites(naatRepo.getNaats().toList())
    }
  }

  fun toggleFavorite(naat: NaatItem) {
    val userEmail = loggedInEmail ?: currentUser?.email
    val newFav = favoritesRepo.toggleFavorite(naat.id, userEmail)
    _naats.value = _naats.value.map {
      if (it.id == naat.id) it.copy(isFavorite = newFav) else it
    }
    if (selectedNaat?.id == naat.id) {
      selectedNaat = selectedNaat?.copy(isFavorite = newFav)
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
        addedBy = loggedInEmail ?: currentUser?.email ?: "admin"
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
        addedBy = loggedInEmail ?: currentUser?.email ?: "admin"
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

  // ==================== Authentication ====================

  fun signIn(email: String, pass: String, isAdminRole: Boolean, onResult: (Boolean) -> Unit) {
    viewModelScope.launch {
      authError = null
      val result = authRepo.signIn(email, pass)
      if (result.isSuccess) {
        isLoggedIn = true
        loggedInEmail = authRepo.loggedInEmail
        isAdmin = authRepo.isAdmin
        currentUser = authRepo.currentUser
        favoritesRepo.syncUserFavorites(loggedInEmail ?: currentUser?.email)
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
        isLoggedIn = true
        loggedInEmail = authRepo.loggedInEmail
        isAdmin = authRepo.isAdmin
        currentUser = authRepo.currentUser
        favoritesRepo.syncUserFavorites(loggedInEmail ?: currentUser?.email)
        onResult(true)
      } else {
        authError = result.exceptionOrNull()?.message ?: "Sign up failed"
        onResult(false)
      }
    }
  }

  fun signOut() {
    authRepo.signOut()
    isLoggedIn = false
    loggedInEmail = null
    currentUser = null
    isAdmin = false
    favoritesRepo.syncUserFavorites(null)
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

  fun changeMyPassword(newPass: String, onResult: (Boolean, String?) -> Unit) {
    viewModelScope.launch {
      val result = authRepo.updatePassword(newPass)
      if (result.isSuccess) {
        onResult(true, "Password updated successfully!")
      } else {
        val err = result.exceptionOrNull()?.message ?: "Failed to update password"
        onResult(false, err)
      }
    }
  }

  fun adminResetUserPassword(email: String, onResult: (Boolean, String?) -> Unit) {
    viewModelScope.launch {
      if (email.isBlank()) {
        onResult(false, "Invalid user email")
        return@launch
      }
      val result = authRepo.sendPasswordResetEmail(email)
      if (result.isSuccess) {
        onResult(true, "Password reset instructions sent to $email")
      } else {
        onResult(false, "Failed to send password reset email to $email")
      }
    }
  }

  fun makeAdminForTest() {
    val email = loggedInEmail ?: "azazmadkiya@gmail.com"
    authRepo.setAdminForUser(email, true)
    isLoggedIn = true
    loggedInEmail = email
    isAdmin = true
    favoritesRepo.syncUserFavorites(email)
  }
}
