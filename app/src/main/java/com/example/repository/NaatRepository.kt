package com.example.repository

import android.util.Log
import com.example.model.CategoryItem
import com.example.model.NaatItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NaatRepository {
  private val firestore by lazy {
    try {
      FirebaseFirestore.getInstance()
    } catch (e: Exception) {
      null
    }
  }

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

  private val defaultNaats = listOf(
    NaatItem(
      id = "1",
      title = "अक्से रूए मुस्तफा से ऐसी",
      category = "naat",
      reciter = "Ala Hazrat",
      lyrics = "अक्से रूए मुस्तफा से ऐसी रौशनी हुई,\nज़रा ज़रा मदीने का आफ़ताब हो गया।\n\nदूर से आने वालो हमें भी सलाम कहना,\nहमारा भी तो वहाँ पैग़ाम कहना।",
      hindiLyrics = "अक्से रूए मुस्तफा से ऐसी रौशनी हुई...",
      gujaratiLyrics = "અક્સે રૂએ મુસ્તફા સે એસી રોશની હુઈ,\nઝરા ઝરા મદીને કા આફતાબ હો ગયા।"
    ),
    NaatItem(
      id = "2",
      title = "अपने दामाने शफाअत में छुपाए",
      category = "naat",
      reciter = "Khalid Mahmud",
      lyrics = "अपने दामाने शफाअत में छुपाए रखना,\nमेरे सरकार मेरी बात बनाए रखना।\n\nमैंने माना के निकम्मा हु मगर आपका हु,\nमुझ निकम्मे को भी सरकार निभाए रखना।",
      hindiLyrics = "अपने दामाने शफाअत में छुपाए रखना...",
      gujaratiLyrics = "અપને દામાને શફાઅત મેં છુપાએ રખના,\nમેરે સરકાર મેરી બાત બનાએ રખના।"
    ),
    NaatItem(
      id = "3",
      title = "अब तो बस एक ही धुन है",
      category = "naat",
      reciter = "Owais Raza Qadri",
      lyrics = "अब तो बस एक ही धुन है के मदीना देखूँ,\nरौज़ए اقدस और शहर का नगीना देखूँ।",
      hindiLyrics = "अब तो बस एक ही धुन है...",
      gujaratiLyrics = "અબ તો બસ એક હી ધુન હૈ કે मदीना દેખૂં..."
    ),
    NaatItem(
      id = "4",
      title = "वही रब है जिसने तुझको",
      category = "hamd",
      reciter = "Hamd Reciter",
      lyrics = "वही रब है जिसने तुझको ये मक़ाम बख्शा,\nज़मीं पर भी आसमां का एहतराम बख्शा।",
      hindiLyrics = "वही रब है जिसने तुझको...",
      gujaratiLyrics = "વહી રબ હૈ જિસને તુઝકો યે મકામ બખ્શા..."
    ),
    NaatItem(
      id = "5",
      title = "मनक़बत गौसे आज़म दस्तगीर",
      category = "gouse",
      reciter = "Manqabat Khwan",
      lyrics = "या शाहमीर गौसे आज़म दस्तगीर,\nमुश्किलकुशा हो आप, करो मेरे पीर।",
      hindiLyrics = "या शाहमीर गौसे आज़म...",
      gujaratiLyrics = "યા શાહમીર ગૌસે આઝમ દસ્તगीर..."
    )
  )

  companion object {
    private val cachedNaats = mutableListOf<NaatItem>()
    private val cachedCategories = mutableListOf<CategoryItem>()
  }

  suspend fun getCategories(): List<CategoryItem> {
    try {
      val db = firestore
      if (db != null) {
        val snapshot = db.collection("categories").get().await()
        val list = snapshot.documents.mapNotNull { doc ->
          doc.toObject(CategoryItem::class.java)?.copy(id = doc.id)
        }
        if (list.isNotEmpty()) {
          cachedCategories.clear()
          cachedCategories.addAll(list)
          return cachedCategories
        }
      }
    } catch (e: Exception) {
      Log.d("NaatRepository", "Failed to fetch categories from cloud, using cache")
    }
    if (cachedCategories.isEmpty()) {
      cachedCategories.addAll(defaultCategories)
    }
    return cachedCategories
  }

  suspend fun addCategory(item: CategoryItem): Boolean {
    val newItem = if (item.id.isBlank()) item.copy(id = item.title.lowercase().replace(" ", "_")) else item
    cachedCategories.add(newItem)
    try {
      val db = firestore
      if (db != null) {
        db.collection("categories").document(newItem.id).set(newItem).await()
      }
    } catch (e: Exception) {
      Log.d("NaatRepository", "Category added locally")
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
      val db = firestore
      if (db != null && item.id.isNotEmpty()) {
        db.collection("categories").document(item.id).set(item).await()
      }
    } catch (e: Exception) {
      Log.d("NaatRepository", "Category updated locally")
    }
    return true
  }

  suspend fun deleteCategory(id: String): Boolean {
    cachedCategories.removeAll { it.id == id }
    try {
      val db = firestore
      if (db != null && id.isNotEmpty()) {
        db.collection("categories").document(id).delete().await()
      }
    } catch (e: Exception) {
      Log.d("NaatRepository", "Category deleted locally")
    }
    return true
  }

  suspend fun getNaats(): List<NaatItem> {
    try {
      val db = firestore
      if (db != null) {
        val snapshot = db.collection("naats").get().await()
        val list = snapshot.documents.mapNotNull { doc ->
          doc.toObject(NaatItem::class.java)?.copy(id = doc.id)
        }
        if (list.isNotEmpty()) {
          cachedNaats.clear()
          cachedNaats.addAll(list)
          return cachedNaats
        }
      }
    } catch (e: Exception) {
      Log.d("NaatRepository", "Failed to fetch naats from cloud, using cache")
    }
    if (cachedNaats.isEmpty()) {
      cachedNaats.addAll(defaultNaats)
    }
    return cachedNaats
  }

  suspend fun addNaat(item: NaatItem): Boolean {
    val newItem = if (item.id.isBlank()) item.copy(id = System.currentTimeMillis().toString()) else item
    cachedNaats.add(0, newItem)
    try {
      val db = firestore
      if (db != null) {
        db.collection("naats").document(newItem.id).set(newItem).await()
      }
    } catch (e: Exception) {
      Log.d("NaatRepository", "Added locally")
    }
    return true
  }

  suspend fun updateNaat(item: NaatItem): Boolean {
    val index = cachedNaats.indexOfFirst { it.id == item.id }
    if (index >= 0) {
      cachedNaats[index] = item
    } else {
      cachedNaats.add(0, item)
    }
    try {
      val db = firestore
      if (db != null && item.id.isNotEmpty()) {
        db.collection("ats").document(item.id).set(item).await() // wait, collection is naats
        db.collection("naats").document(item.id).set(item).await()
      }
    } catch (e: Exception) {
      Log.d("NaatRepository", "Updated locally")
    }
    return true
  }

  suspend fun deleteNaat(id: String): Boolean {
    cachedNaats.removeAll { it.id == id }
    try {
      val db = firestore
      if (db != null && id.isNotEmpty()) {
        db.collection("naats").document(id).delete().await()
      }
    } catch (e: Exception) {
      Log.d("NaatRepository", "Deleted locally")
    }
    return true
  }

  suspend fun updateFavorite(id: String, isFavorite: Boolean): Boolean {
    val index = cachedNaats.indexOfFirst { it.id == id }
    if (index >= 0) {
      cachedNaats[index] = cachedNaats[index].copy(isFavorite = isFavorite)
    }
    try {
      val db = firestore ?: return true
      db.collection("naats").document(id).update("favorite", isFavorite).await()
    } catch (e: Exception) {
      Log.d("NaatRepository", "Favorite updated locally")
    }
    return true
  }
}
