package com.example.repository

import android.util.Log
import com.example.model.AppUserItem
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
    private val usersCollection = firestore?.collection("app_users")

    val defaultCategories = listOf(
        CategoryItem("hamd", "हम्द शरीफ", "Hamd Sharif", "ic_hamd", 0, "", 0),
        CategoryItem("naat", "नात शरीफ", "Naat Sharif", "ic_naat", 0, "", 1),
        CategoryItem("manqabat", "मनक़बत शरीफ", "Manqabat Sharif", "ic_manqabat", 0, "", 2),
        CategoryItem("panjtan", "पंजतन पाक", "Panjtan Pak", "ic_panjtan", 0, "", 3),
        CategoryItem("gouse", "गौसे आज़म", "Gouse Azam", "ic_gouse", 0, "", 4),
        CategoryItem("gareeb", "गरीब नवाज़", "Gareeb Nawaz", "ic_gareeb", 0, "", 5),
        CategoryItem("tazeem", "तज़ीम कलाम", "Tazeem Kalam", "ic_tazeem", 0, "", 6),
        CategoryItem("raza", "कलामे रज़ा", "Kalame Raza", "ic_raza", 0, "", 7)
    )

    val defaultUsers = listOf(
        AppUserItem(
            id = "admin_owner",
            email = "azazmadkiya@gmail.com",
            name = "Azaz Madkiya (Owner)",
            role = "ADMIN",
            allowedCategories = emptyList(),
            createdAt = System.currentTimeMillis(),
            isActive = true
        )
    )

    companion object {
        private val cachedNaats = mutableListOf<NaatItem>()
        private val cachedCategories = mutableListOf<CategoryItem>()
        private val cachedUsers = mutableListOf<AppUserItem>()
    }

    private fun mapDocToNaat(doc: com.google.firebase.firestore.DocumentSnapshot): Naat? {
        val naat = doc.toObject(Naat::class.java)?.apply { this.id = doc.id } ?: return null
        val docHindi = doc.getString("hindiLyrics") 
            ?: doc.getString("hindi_lyrics") 
            ?: doc.getString("hindi") 
            ?: ""
        val docGujarati = doc.getString("gujaratiLyrics") 
            ?: doc.getString("gujarati_lyrics") 
            ?: doc.getString("gujarati") 
            ?: ""
        val docLyrics = doc.getString("lyrics") ?: ""

        if (naat.hindiLyrics.isBlank()) {
            naat.hindiLyrics = if (docHindi.isNotBlank()) docHindi else docLyrics
        }
        if (naat.gujaratiLyrics.isBlank()) {
            naat.gujaratiLyrics = docGujarati
        }
        if (naat.lyrics.isBlank()) {
            naat.lyrics = if (docLyrics.isNotBlank()) docLyrics else naat.hindiLyrics
        }
        return naat
    }

    private fun mapDocToCategory(doc: com.google.firebase.firestore.DocumentSnapshot, defaultIndex: Int): CategoryItem {
        val obj = doc.toObject(CategoryItem::class.java)?.apply { this.id = doc.id }
            ?: CategoryItem(id = doc.id)
        val docTitle = doc.getString("title") 
            ?: doc.getString("name") 
            ?: doc.getString("category") 
            ?: doc.getString("categoryTitle") 
            ?: ""
        val docSubtitle = doc.getString("subtitle") 
            ?: doc.getString("subTitle") 
            ?: doc.getString("desc") 
            ?: ""
        val docOrder = doc.getLong("order")?.toInt() ?: doc.getLong("position")?.toInt() ?: defaultIndex

        val finalTitle = if (obj.title.isNotBlank()) obj.title 
            else if (docTitle.isNotBlank()) docTitle 
            else obj.getDisplayTitle()
            
        val finalSubtitle = if (obj.subtitle.isNotBlank()) obj.subtitle 
            else if (docSubtitle.isNotBlank()) docSubtitle 
            else obj.getDisplaySubtitle()

        val finalOrder = if (doc.contains("order")) docOrder else if (obj.order != 0) obj.order else defaultIndex

        return obj.copy(
            id = doc.id,
            title = finalTitle,
            subtitle = finalSubtitle,
            order = finalOrder
        )
    }

    // Admin panel se naat save hote hi ye automatic real-time flow emit karega
    fun getNaatsRealtime(): Flow<List<Naat>> = callbackFlow {
        trySend(cachedNaats.map { it.toNaat() })

        val collection = naatsCollection
        if (collection == null) {
            awaitClose { }
            return@callbackFlow
        }

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("NaatRepository", "Firestore realtime note: ${error.message}")
                trySend(cachedNaats.map { it.toNaat() })
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val naatList = snapshot.documents.mapNotNull { doc ->
                    mapDocToNaat(doc)
                }
                cachedNaats.clear()
                cachedNaats.addAll(naatList.map { it.toNaatItem() })
                trySend(naatList)
            }
        }
        awaitClose { listener.remove() }
    }

    fun listenToNaats(onUpdate: (List<NaatItem>) -> Unit) {
        onUpdate(cachedNaats)
        try {
            naatsCollection?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("NaatRepository", "Naats listen notice: ${error.message}")
                    onUpdate(cachedNaats)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        mapDocToNaat(doc)?.toNaatItem()
                    }
                    cachedNaats.clear()
                    cachedNaats.addAll(list)
                    onUpdate(cachedNaats)
                }
            }
        } catch (e: Exception) {
            Log.w("NaatRepository", "Naats snapshot notice: ${e.message}")
            onUpdate(cachedNaats)
        }
    }

    fun listenToCategories(onUpdate: (List<CategoryItem>) -> Unit) {
        if (cachedCategories.isEmpty()) {
            cachedCategories.addAll(defaultCategories)
        }
        onUpdate(cachedCategories.sortedBy { it.order })
        try {
            firestore?.collection("categories")?.addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapIndexed { index, doc ->
                        mapDocToCategory(doc, index)
                    }.sortedBy { it.order }
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
            Log.w("NaatRepository", "Categories snapshot notice: ${e.message}")
        }
    }

    suspend fun getCategories(): List<CategoryItem> {
        try {
            val db = firestore
            if (db != null) {
                val snapshot = db.collection("categories").get().await()
                val list = snapshot.documents.mapIndexed { index, doc ->
                    mapDocToCategory(doc, index)
                }.sortedBy { it.order }
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
            Log.w("NaatRepository", "Failed to fetch categories from cloud: ${e.message}")
        }
        if (cachedCategories.isEmpty()) {
            cachedCategories.addAll(defaultCategories)
        }
        return cachedCategories.sortedBy { it.order }
    }

    suspend fun saveCategoriesOrder(categories: List<CategoryItem>): Boolean {
        cachedCategories.clear()
        cachedCategories.addAll(categories)
        try {
            val db = firestore ?: return true
            for ((index, cat) in categories.withIndex()) {
                val map = mapOf("order" to index)
                db.collection("categories").document(cat.id).update(map).await()
            }
        } catch (e: Exception) {
            Log.w("NaatRepository", "Reorder categories update notice: ${e.message}")
        }
        return true
    }

    suspend fun addCategory(item: CategoryItem): Boolean {
        val count = cachedCategories.size
        val newItem = if (item.id.isBlank()) item.copy(id = item.title.lowercase().replace(" ", "_"), order = count) else item.copy(order = count)
        if (!cachedCategories.any { it.id == newItem.id }) {
            cachedCategories.add(newItem)
        }
        try {
            firestore?.collection("categories")?.document(newItem.id)?.set(newItem)?.await()
        } catch (e: Exception) {
            Log.w("NaatRepository", "Category added notice: ${e.message}")
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
            Log.w("NaatRepository", "Category updated notice: ${e.message}")
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
            Log.w("NaatRepository", "Category deleted notice: ${e.message}")
        }
        return true
    }

    // ==================== User Management & RBAC Rights ====================

    fun listenToUsers(onUpdate: (List<AppUserItem>) -> Unit) {
        if (cachedUsers.isEmpty()) {
            cachedUsers.addAll(defaultUsers)
        }
        onUpdate(cachedUsers)
        try {
            usersCollection?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("NaatRepository", "Users listen notice: ${error.message}")
                    onUpdate(cachedUsers)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(AppUserItem::class.java)?.apply { this.id = doc.id }
                    }
                    cachedUsers.clear()
                    if (list.isNotEmpty()) {
                        // Ensure owner is always present
                        val hasOwner = list.any { it.email.equals("azazmadkiya@gmail.com", ignoreCase = true) }
                        if (!hasOwner) {
                            cachedUsers.addAll(defaultUsers)
                        }
                        cachedUsers.addAll(list.filterNot { it.email.equals("azazmadkiya@gmail.com", ignoreCase = true) })
                    } else {
                        cachedUsers.addAll(defaultUsers)
                    }
                    onUpdate(cachedUsers)
                }
            }
        } catch (e: Exception) {
            Log.w("NaatRepository", "Users snapshot notice: ${e.message}")
            onUpdate(cachedUsers)
        }
    }

    suspend fun getUsers(): List<AppUserItem> {
        try {
            val db = firestore
            if (db != null) {
                val snapshot = db.collection("app_users").get().await()
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(AppUserItem::class.java)?.apply { this.id = doc.id }
                }
                cachedUsers.clear()
                if (list.isNotEmpty()) {
                    val hasOwner = list.any { it.email.equals("azazmadkiya@gmail.com", ignoreCase = true) }
                    if (!hasOwner) {
                        cachedUsers.addAll(defaultUsers)
                    }
                    cachedUsers.addAll(list.filterNot { it.email.equals("azazmadkiya@gmail.com", ignoreCase = true) })
                } else {
                    cachedUsers.addAll(defaultUsers)
                    try {
                        db.collection("app_users").document("admin_owner").set(defaultUsers.first()).await()
                    } catch (_: Exception) {}
                }
                return cachedUsers
            }
        } catch (e: Exception) {
            Log.w("NaatRepository", "Failed to fetch users from cloud: ${e.message}")
        }
        if (cachedUsers.isEmpty()) {
            cachedUsers.addAll(defaultUsers)
        }
        return cachedUsers
    }

    suspend fun addUser(user: AppUserItem): Boolean {
        val docId = if (user.id.isBlank()) "user_${System.currentTimeMillis()}" else user.id
        val newUser = user.copy(id = docId, email = user.email.trim().lowercase())
        if (!cachedUsers.any { it.email.equals(newUser.email, ignoreCase = true) }) {
            cachedUsers.add(newUser)
        }
        try {
            usersCollection?.document(docId)?.set(newUser)?.await()
        } catch (e: Exception) {
            Log.w("NaatRepository", "Add user notice: ${e.message}")
        }
        return true
    }

    suspend fun updateUser(user: AppUserItem): Boolean {
        val index = cachedUsers.indexOfFirst { it.id == user.id || it.email.equals(user.email, ignoreCase = true) }
        if (index >= 0) {
            cachedUsers[index] = user
        } else {
            cachedUsers.add(user)
        }
        try {
            val docId = if (user.id.isNotBlank()) user.id else "user_${System.currentTimeMillis()}"
            usersCollection?.document(docId)?.set(user)?.await()
        } catch (e: Exception) {
            Log.w("NaatRepository", "Update user notice: ${e.message}")
        }
        return true
    }

    suspend fun deleteUser(id: String): Boolean {
        cachedUsers.removeAll { it.id == id && !it.email.equals("azazmadkiya@gmail.com", ignoreCase = true) }
        try {
            if (id.isNotEmpty() && id != "admin_owner") {
                usersCollection?.document(id)?.delete()?.await()
            }
        } catch (e: Exception) {
            Log.w("NaatRepository", "Delete user notice: ${e.message}")
        }
        return true
    }

    // ==================== Naat CRUD ====================

    suspend fun getNaats(): List<NaatItem> {
        try {
            val db = firestore
            if (db != null) {
                val snapshot = db.collection("naats").get().await()
                val list = snapshot.documents.mapNotNull { doc ->
                    mapDocToNaat(doc)?.toNaatItem()
                }
                cachedNaats.clear()
                cachedNaats.addAll(list)
                return cachedNaats
            }
        } catch (e: Exception) {
            Log.w("NaatRepository", "Failed to fetch naats from cloud: ${e.message}")
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
            Log.w("NaatRepository", "Add naat notice: ${e.message}")
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
            Log.w("NaatRepository", "Update naat notice: ${e.message}")
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
            Log.w("NaatRepository", "Delete naat notice: ${e.message}")
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
            Log.w("NaatRepository", "Favorite update notice: ${e.message}")
        }
        return true
    }
}
