package com.example.repository

import android.content.Context
import android.util.Log
import com.example.NaatApplication
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FavoritesRepository {
    private val firestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val prefs by lazy {
        try {
            NaatApplication.instance.getSharedPreferences("naat_favorites_prefs", Context.MODE_PRIVATE)
        } catch (e: Exception) {
            null
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private var snapshotListener: ListenerRegistration? = null
    private var currentActiveUserKey: String = ""

    private val _favoriteIds = MutableStateFlow<Set<String>>(loadLocalFavorites())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private fun loadLocalFavorites(): Set<String> {
        return prefs?.getStringSet("fav_ids", emptySet())?.toSet() ?: emptySet()
    }

    private fun saveLocalFavorites(set: Set<String>) {
        prefs?.edit()?.putStringSet("fav_ids", set)?.apply()
    }

    private fun getDeviceId(): String {
        var id = prefs?.getString("device_id", null)
        if (id.isNullOrBlank()) {
            id = UUID.randomUUID().toString()
            prefs?.edit()?.putString("device_id", id)?.apply()
        }
        return id
    }

    private fun getUserKey(userEmail: String?): String {
        val clean = userEmail?.trim()?.lowercase()
        return if (!clean.isNullOrBlank()) {
            clean.replace(".", "_")
        } else {
            "device_${getDeviceId()}"
        }
    }

    /**
     * Syncs favorites with Firestore in real-time for the given user,
     * merging local favorites with cloud favorites across devices.
     */
    fun syncUserFavorites(userEmail: String?) {
        val userKey = getUserKey(userEmail)
        if (currentActiveUserKey == userKey && snapshotListener != null) {
            return
        }

        snapshotListener?.remove()
        snapshotListener = null
        currentActiveUserKey = userKey

        val db = firestore
        if (db == null) {
            Log.w("FavoritesRepository", "Firestore not available, using offline favorites")
            return
        }

        val docRef = db.collection("user_favorites").document(userKey)

        scope.launch {
            try {
                // 1. Initial Fetch & Merge
                val snapshot = docRef.get().await()
                val cloudFavorites = if (snapshot.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    (snapshot.get("favoriteIds") as? List<String>)?.toSet() ?: emptySet()
                } else {
                    emptySet()
                }

                val localFavorites = loadLocalFavorites()
                val merged = (localFavorites + cloudFavorites).toSet()

                // Save merged set locally
                saveLocalFavorites(merged)
                _favoriteIds.value = merged

                // Persist merged set back to Firestore
                val data = mapOf(
                    "userId" to userKey,
                    "favoriteIds" to merged.toList(),
                    "updatedAt" to System.currentTimeMillis()
                )
                docRef.set(data, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.w("FavoritesRepository", "Initial favorites sync notice: ${e.message}")
            }

            // 2. Real-time Snapshot Listener for live multi-device sync
            try {
                snapshotListener = docRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FavoritesRepository", "Realtime favorites listen notice: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        @Suppress("UNCHECKED_CAST")
                        val cloudList = (snapshot.get("favoriteIds") as? List<String>)?.toSet() ?: emptySet()
                        saveLocalFavorites(cloudList)
                        _favoriteIds.value = cloudList
                    }
                }
            } catch (e: Exception) {
                Log.w("FavoritesRepository", "Snapshot listener attach note: ${e.message}")
            }
        }
    }

    fun isFavorite(id: String): Boolean {
        if (id.isBlank()) return false
        return _favoriteIds.value.contains(id)
    }

    /**
     * Toggles a favorite status locally and asynchronously syncs to Firestore.
     */
    fun toggleFavorite(id: String, userEmail: String?): Boolean {
        if (id.isBlank()) return false
        val current = _favoriteIds.value.toMutableSet()
        val newState = if (current.contains(id)) {
            current.remove(id)
            false
        } else {
            current.add(id)
            true
        }

        // Instant local update
        saveLocalFavorites(current)
        _favoriteIds.value = current

        // Cloud sync to Firestore
        val userKey = if (currentActiveUserKey.isNotBlank()) currentActiveUserKey else getUserKey(userEmail)
        val db = firestore
        if (db != null) {
            val docRef = db.collection("user_favorites").document(userKey)
            scope.launch {
                try {
                    if (newState) {
                        docRef.set(
                            mapOf(
                                "userId" to userKey,
                                "favoriteIds" to FieldValue.arrayUnion(id),
                                "updatedAt" to System.currentTimeMillis()
                            ),
                            SetOptions.merge()
                        ).await()
                    } else {
                        docRef.update(
                            "favoriteIds", FieldValue.arrayRemove(id),
                            "updatedAt", System.currentTimeMillis()
                        ).await()
                    }
                } catch (e: Exception) {
                    Log.w("FavoritesRepository", "Firestore toggle sync notice: ${e.message}")
                    // Fallback to complete set overwrite
                    try {
                        docRef.set(
                            mapOf(
                                "userId" to userKey,
                                "favoriteIds" to current.toList(),
                                "updatedAt" to System.currentTimeMillis()
                            ),
                            SetOptions.merge()
                        ).await()
                    } catch (_: Exception) {}
                }
            }
        }

        return newState
    }
}
