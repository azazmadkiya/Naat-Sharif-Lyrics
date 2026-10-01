package com.example.repository

import android.util.Log
import com.example.model.CategoryItem
import com.example.model.Naat
import com.example.model.NaatItem
import com.example.model.toNaat
import com.example.model.toNaatItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NaatRepository {
    private val firestore = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }
    private val naatsCollection = firestore?.collection("naats")

    private val defaultCategories = listOf(
        CategoryItem("hamd", "हम्द शरीफ", "Hamd Sharif", "ic_hamd", 15),
        CategoryItem("naat", "नात शरीफ", "Naat Sharif", "ic_naat", 45),
        CategoryItem("manqabat", "मनक़बत शरीफ", "Manqabat Sharif", "ic_manqabat", 25),
        CategoryItem("panjtan", "पंजतन पाक", "Panjtan Pak", "ic_panjtan", 10),
        CategoryItem("gouse", "गौसे आज़म", "Gouse Azam", "ic_gouse", 12),
        CategoryItem("gareeb", "गरीब नवाज़", "Gareeb Nawaz", "ic_gareeb", 14),
        CategoryItem("tazeem", "तज़ीम कलाम", "Tazeem Kalam", "ic_tazeem", 8),
        CategoryItem("raza", "कलामे रज़ा", "Kalame Raza", "ic_raza", 30)
    )

    companion object {
        private val cachedNaats = mutableListOf<NaatItem>()
        private val cachedCategories = mutableListOf<CategoryItem>()
    }

    // Admin panel se naat save hote hi ye automatic real-time flow emit karega
    fun getNaatsRealtime(): Flow<List<Naat>> = callbackFlow {
        val collection = naatsCollection
        if (collection == null) {
            trySend(cachedNaats.map { it.toNaat() })
            awaitClose { }
            return@callbackFlow
        }
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val naatList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Naat::class.java)?.apply { this.id = doc.id }
                }
                cachedNaats.clear()
                cachedNaats.addAll(naatList.map { it.toNaatItem() })
                trySend(naatList)
            }
        }
        awaitClose { listener.remove() }
    }

    fun listenToNaats(onUpdate: (List<NaatItem>) -> Unit) {
        try {
            naatsCollection?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d("NaatRepository", "Naats listen failed: ${error.message}")
                    onUpdate(cachedNaats)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Naat::class.java)?.let { naat ->
                            naat.id = doc.id
                            naat.toNaatItem()
                        } ?: doc.toObject(NaatItem::class.java)?.copy(id = doc.id)
                    }
                    cachedNaats.clear()
                    cachedNaats.addAll(list)
                    onUpdate(cachedNaats)
                }
            }
        } catch (e: Exception) {
            Log.d("NaatRepository", "Naats snapshot error: ${e.message}")
            onUpdate(cachedNaats)
        }
    }

    fun listenToCategories(onUpdate: (List<CategoryItem>) -> Unit) {
        try {
            firestore?.collection("categories")?.addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(CategoryItem::class.java)?.copy(id = doc.id)
                    }
                    cachedCategories.clear()
                    if (list.isNotEmpty()) {
                        cachedCategories.addAll(list)
                    } else {
                        cachedCategories.addAll(defaultCategories)
                    }
                    onUpdate(cachedCategories)
                }
            }
        } catch (e: Exception) {
            Log.d("NaatRepository", "Categories snapshot error: ${e.message}")
        }
    }

    suspend fun getCategories(): List<CategoryItem> {
        try {
            val db = firestore
            if (db != null) {
                val snapshot = db.collection("categories").get().await()
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(CategoryItem::class.java)?.copy(id = doc.id)
                }
                cachedCategories.clear()
                if (list.isNotEmpty()) {
                    cachedCategories.addAll(list)
                } else {
                    cachedCategories.addAll(defaultCategories)
                    for (cat in defaultCategories) {
                        try {
                            db.collection("categories").document(cat.id).set(cat).await()
                        } catch (_: Exception) {}
                    }
                }
                return cachedCategories
            }
        } catch (e: Exception) {
            Log.d("NaatRepository", "Failed to fetch categories: ${e.message}")
        }
        if (cachedCategories.isEmpty()) {
            cachedCategories.addAll(defaultCategories)
        }
        return cachedCategories
    }

    suspend fun addCategory(item: CategoryItem): Boolean {
        val newItem = if (item.id.isBlank()) item.copy(id = item.title.lowercase().replace(" ", "_")) else item
        if (!cachedCategories.any { it.id == newItem.id }) {
            cachedCategories.add(newItem)
        }
        try {
            firestore?.collection("categories")?.document(newItem.id)?.set(newItem)?.await()
        } catch (e: Exception) {
            Log.d("NaatRepository", "Category added error: ${e.message}")
        }
        return true
    }

    suspend fun updateCategory(item: CategoryItem): Boolean {
        val index = cachedCategories.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            cachedCategories[index] = item
        } else {
            cachedCategories.add(item)
        }
        try {
            if (item.id.isNotEmpty()) {
                firestore?.collection("categories")?.document(item.id)?.set(item)?.await()
            }
        } catch (e: Exception) {
            Log.d("NaatRepository", "Category updated error: ${e.message}")
        }
        return true
    }

    suspend fun deleteCategory(id: String): Boolean {
        cachedCategories.removeAll { it.id == id }
        try {
            if (id.isNotEmpty()) {
                firestore?.collection("categories")?.document(id)?.delete()?.await()
            }
        } catch (e: Exception) {
            Log.d("NaatRepository", "Category deleted error: ${e.message}")
        }
        return true
    }

    suspend fun getNaats(): List<NaatItem> {
        try {
            val db = firestore
            if (db != null) {
                val snapshot = db.collection("naats").get().await()
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Naat::class.java)?.let { naat ->
                        naat.id = doc.id
                        naat.toNaatItem()
                    } ?: doc.toObject(NaatItem::class.java)?.copy(id = doc.id)
                }
                cachedNaats.clear()
                cachedNaats.addAll(list)
                return cachedNaats
            }
        } catch (e: Exception) {
            Log.d("NaatRepository", "Failed to fetch naats: ${e.message}")
        }
        return cachedNaats
    }

    suspend fun addNaat(item: Naat): Boolean = addNaat(item.toNaatItem())

    suspend fun addNaat(item: NaatItem): Boolean {
        val newItem = if (item.id.isBlank()) item.copy(id = System.currentTimeMillis().toString()) else item
        if (!cachedNaats.any { it.id == newItem.id }) {
            cachedNaats.add(0, newItem)
        } else {
            val index = cachedNaats.indexOfFirst { it.id == newItem.id }
            cachedNaats[index] = newItem
        }
        try {
            firestore?.collection("naats")?.document(newItem.id)?.set(newItem.toNaat())?.await()
        } catch (e: Exception) {
            Log.d("NaatRepository", "Add naat error: ${e.message}")
        }
        return true
    }

    suspend fun updateNaat(item: Naat): Boolean = updateNaat(item.toNaatItem())

    suspend fun updateNaat(item: NaatItem): Boolean {
        val index = cachedNaats.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            cachedNaats[index] = item
        } else {
            cachedNaats.add(0, item)
        }
        try {
            if (item.id.isNotEmpty()) {
                firestore?.collection("naats")?.document(item.id)?.set(item.toNaat())?.await()
            }
        } catch (e: Exception) {
            Log.d("NaatRepository", "Update naat error: ${e.message}")
        }
        return true
    }

    suspend fun deleteNaat(id: String): Boolean {
        cachedNaats.removeAll { it.id == id }
        try {
            if (id.isNotEmpty()) {
                firestore?.collection("naats")?.document(id)?.delete()?.await()
            }
        } catch (e: Exception) {
            Log.d("NaatRepository", "Delete naat error: ${e.message}")
        }
        return true
    }

    suspend fun updateFavorite(id: String, isFavorite: Boolean): Boolean {
        val index = cachedNaats.indexOfFirst { it.id == id }
        if (index >= 0) {
            cachedNaats[index] = cachedNaats[index].copy(isFavorite = isFavorite)
        }
        try {
            firestore?.collection("naats")?.document(id)?.update("isFavorite", isFavorite)?.await()
        } catch (e: Exception) {
            Log.d("NaatRepository", "Favorite update error: ${e.message}")
        }
        return true
    }
}
