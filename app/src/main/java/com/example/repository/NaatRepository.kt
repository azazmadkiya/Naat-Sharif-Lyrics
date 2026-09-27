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

  val categories = listOf(
    CategoryItem("hamd", "हम्द शरीफ", "Hamd Sharif", "ic_hamd", 15),
    CategoryItem("naat", "नात शरीफ", "Naat Sharif", "ic_naat", 45),
    CategoryItem("manqabat", "मनक़बत शरीफ", "Manqabat Sharif", "ic_manqabat", 25),
    CategoryItem("panjtan", "पंजतन पाक", "Panjtan Pak", "ic_panjtan", 10),
    CategoryItem("gouse", "गौसे आज़म", "Gouse Azam", "ic_gouse", 12),
    CategoryItem("gareeb", "गरीब नवाज़", "Gareeb Nawaz", "ic_gareeb", 14),
    CategoryItem("tazeem", "तज़ीम कलाम", "Tazeem Kalam", "ic_tazeem", 8),
    CategoryItem("raza", "कलामे रज़ा", "Kalame Raza", "ic_raza", 30)
  )

  val defaultNaats = listOf(
    NaatItem(
      id = "1",
      title = "1. अक्से रूए मुस्तफा से ऐसी",
      category = "naat",
      reciter = "Ala Hazrat",
      lyrics = "अक्से रूए मुस्तफा से ऐसी रौशनी हुई,\nज़रा ज़रा मदीने का आफ़ताब हो गया।\n\nदूर से आने वालो हमें भी सलाम कहना,\nहमारा भी तो वहाँ पैग़ाम कहना।",
      hindiLyrics = "अक्से रूए मुस्तफा से ऐसी रौशनी हुई...",
      gujaratiLyrics = "અક્સે રૂએ મુસ્તફા સે એસી રોશની હુઈ,\nઝરા ઝરા મદીને કા આફતાબ હો ગયા।"
    ),
    NaatItem(
      id = "2",
      title = "2. अपने दामाने शफाअत में छुपाए",
      category = "naat",
      reciter = "Khalid Mahmud",
      lyrics = "अपने दामाने शफाअत में छुपाए रखना,\nमेरे सरकार मेरी बात बनाए रखना।\n\nमैंने माना के निकम्मा हु मगर आपका हु,\nमुझ निकम्मे को भी सरकार निभाए रखना।",
      hindiLyrics = "अपने दामाने शफाअत में छुपाए रखना...",
      gujaratiLyrics = "અપને દામાને શફાઅત મેં છુપાએ રખના,\nમેરે સરકાર મેરી બાત બનાએ રખના।"
    ),
    NaatItem(
      id = "3",
      title = "3. अब तो बस एक ही धुन है",
      category = "naat",
      reciter = "Owais Raza Qadri",
      lyrics = "अब तो बस एक ही धुन है के मदीना देखूँ,\nरौज़ए اقدस और शहर का नगीना देखूँ।",
      hindiLyrics = "अब तो बस एक ही धुन है...",
      gujaratiLyrics = "અબ તો બસ એક હી ધુન હૈ કે મદીના દેખૂં..."
    ),
    NaatItem(
      id = "4",
      title = "4. वही रब है जिसने तुझको",
      category = "hamd",
      reciter = "Hamd Reciter",
      lyrics = "वही रब है जिसने तुझको ये मक़ाम बख्शा,\nज़मीं पर भी आसमां का एहतराम बख्शा।",
      hindiLyrics = "वही रब है जिसने तुझको...",
      gujaratiLyrics = "વહી રબ હૈ જિસને તુઝકો યે મકામ બખ્શા..."
    ),
    NaatItem(
      id = "5",
      title = "5. मनक़बत गौसे आज़म दस्तगीर",
      category = "gouse",
      reciter = "Manqabat Khwan",
      lyrics = "या शाहमीर गौसे आज़म दस्तगीर,\nमुश्किलकुशा हो आप, करो मेरे पीर।",
      hindiLyrics = "या शाहमीर गौसे आज़म...",
      gujaratiLyrics = "યા શાહમીર ગૌસે આઝમ દસ્તગીર..."
    )
  )

  suspend fun getNaats(): List<NaatItem> {
    return try {
      val db = firestore ?: return defaultNaats
      val snapshot = db.collection("naats").get().await()
      val list = snapshot.documents.mapNotNull { doc ->
        doc.toObject(NaatItem::class.java)?.copy(id = doc.id)
      }
      if (list.isEmpty()) {
        for (item in defaultNaats) {
          db.collection("naats").document(item.id).set(item).await()
        }
        defaultNaats
      } else {
        list
      }
    } catch (e: Exception) {
      Log.e("NaatRepository", "Error fetching naats, using default", e)
      defaultNaats
    }
  }

  suspend fun addNaat(item: NaatItem): Boolean {
    return try {
      val db = firestore ?: return false
      val docRef = if (item.id.isNotEmpty()) db.collection("naats").document(item.id) else db.collection("naats").document()
      val newItem = item.copy(id = docRef.id)
      docRef.set(newItem).await()
      true
    } catch (e: Exception) {
      Log.e("NaatRepository", "Error adding naat", e)
      false
    }
  }

  suspend fun updateFavorite(id: String, isFavorite: Boolean): Boolean {
    return try {
      val db = firestore ?: return true
      db.collection("naats").document(id).update("favorite", isFavorite).await()
      true
    } catch (e: Exception) {
      Log.e("NaatRepository", "Error updating favorite", e)
      true
    }
  }
}
