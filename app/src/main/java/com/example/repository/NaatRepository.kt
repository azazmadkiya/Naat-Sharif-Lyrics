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
        private fun getInitialNaats(): List<NaatItem> = listOf(
            NaatItem(
                id = "naat_1",
                title = "वह नबियों में रहमत लक़ब पाने वाला",
                category = "naat",
                reciter = "मौलाना अल्ताफ़ हुसैन हाली",
                lyrics = "Woh Nabiyon Me Rahmat Laqab Paane Wala\nMuradein Gareebon Ki Bar Laane Wala\nMuseebat Me Gairon Ke Kaam Aane Wala\nWoh Apne Paraaye Ka Gam Khaane Wala\n\nFaqeeron Ka Malja Zaeefon Ka Mawa\nYateemon Ka Waali Gulaamon Ka Maula",
                hindiLyrics = "वह नबियों में रहमत लक़ब पाने वाला\nमुरादें ग़रीबों की बर लाने वाला\nमुसीबत में ग़ैरों के काम आने वाला\nवह अपने पराये का ग़म खाने वाला\n\nफ़क़ीरों का मलजा ज़ईफ़ों का मावा\nयतीमों का वाली ग़ुलामों का मौला\nखताकारों से दरगुज़र करने वाला\nबद अंदेश के दिल में घर करने वाला",
                gujaratiLyrics = "તે નબિયોમાં રહમત લકબ પામનારા\nમુરાદો ગરીબોની બર લાવનારા\nમુસીબતમાં બીજાના કામ આવનારા\nતે પોતાના પરાયાના ગમ ખાનારા\n\nફકીરોનો સહારો કમજોરોનો આશરો\nયતીમોના વાલી ગુલામોના મોલા",
                arabicLyrics = "وَمَا أَرْسَلْنَاكَ إِلَّا رَحْمَةً لِلْعَالَمِينَ",
                isFavorite = true,
                addedBy = "azazmadkiya@gmail.com"
            ),
            NaatItem(
                id = "naat_2",
                title = "फ़ासलों को तकल्लुफ़ है हमसे अगर",
                category = "naat",
                reciter = "क़ारी वहीद ज़फ़र क़ासमी",
                lyrics = "Faaslon Ko Takalluf Hai Humse Agar\nHum Bhi Bebas Nahi Hain Nabi Ki Qasam\nRoz Jaate Hain Hum Bhi Dayaar-e-Nabi\nJab Bhi Yaad Unki Aati Hai Seene Mein Hum\n\nUnki Chaukhat Pe Girna Machalna Mera\nMeri Bigdi Banana Sanwarna Mera",
                hindiLyrics = "फ़ासलों को तकल्लुफ़ है हमसे अगर\nहम भी बेबस नहीं हैं नबी की क़सम\nरोज़ जाते हैं हम भी दयारे नबी\nजब भी याद उनकी आती है सीने में हम\n\nउनकी चौखट पे गिरना मचलना मेरा\nमेरी बिगड़ी बनाना सँवरना मेरा\nहाज़री का शराफ़त से इज़हार है\nदिल मदीने में रहने को तय्यार है",
                gujaratiLyrics = "ફાસલાઓને તકલ્લુફ છે હમસે અગર\nહમ ભી બેબસ નહીં હૈં નબી કી કસમ\nરોજ જાતે હૈં હમ ભી દયારે નબી\nજબ ભી યાદ ઉનકી આતી હૈ સીને મેં હમ\n\nઉનકી ચોખટ પે ગિરના મચલના મેરા\nમેરી બિગડી બનાના સંવરના મેરા",
                arabicLyrics = "",
                isFavorite = true,
                addedBy = "azazmadkiya@gmail.com"
            ),
            NaatItem(
                id = "naat_3",
                title = "भर दो झोली मेरी या मुहम्मद",
                category = "naat",
                reciter = "साबरी ब्रदर्स",
                lyrics = "Bhar Do Jholi Meri Ya Muhammad\nLaut Kar Main Na Jaaunga Khaali\nDum Qadam Se Tumhare Jahan Aabaad Hai\nTum Shafa'at Ke Dulha Ho Sab Yaad Hai\n\nTum Zamaane Ke Mukhtaar Ho Ya Nabi\nSabki Bigdi Banaane Ke Sardaar Ho",
                hindiLyrics = "भर दो झोली मेरी या मुहम्मद\nलौट कर मैं न जाऊंगा खाली\nदम क़दम से तुम्हारे जहां आबाद है\nतुम शफ़ाअत के दूल्हा हो सब याद है\n\nतुम ज़माने के मुख़्तार हो या नबी\nसबकी बिगड़ी बनाने के सरदार हो\nतेरे दर से कोई खाली लौटा नहीं\nमेरे दाता तेरे जैसा कोई नहीं",
                gujaratiLyrics = "ભર દો ઝોલી મેરી યા મુહમ્મદ\nલૌટ કર મૈં ન જાઉંગા ખાલી\nદમ કદમ સે તુમ્હારે જહાં આબાદ હૈ\nતુમ શફાઅત કે દુલ્હા હો સબ યાદ હૈ",
                arabicLyrics = "",
                isFavorite = false,
                addedBy = "azazmadkiya@gmail.com"
            ),
            NaatItem(
                id = "naat_4",
                title = "कोई तो है जो निज़ामे हस्ती चला रहा है",
                category = "hamd",
                reciter = "मुज़फ़्फ़र वारसी",
                lyrics = "Koi To Hai Jo Nizaam-e-Hasti Chala Raha Hai, Wahi Khuda Hai\nDikhayi Bhi Jo Na De Nazar Bhi Jo Aa Raha Hai, Wahi Khuda Hai\n\nWahi Hai Mashriq Wahi Hai Maghrib Safar Bhi Uska Qayaam Uska\nJahaan Bhar Ki Zabaan Pe Sirf Ek Naam Uska",
                hindiLyrics = "कोई तो है जो निज़ामे हस्ती चला रहा है, वही ख़ुदा है\nदिखाई भी जो न दे नज़र भी जो आ रहा है, वही ख़ुदा है\n\nवही है मशरिक़ वही है मग़रिब सफ़र भी उसका क़याम उसका\nजहान भर की ज़बां पे सिर्फ़ एक नाम उसका\n\nतलाश उसको न कर बुतों में वो दिल के अंदर समा रहा है\nवही ख़ुदा है वही ख़ुदा है",
                gujaratiLyrics = "કોઈ તો છે જે નિઝામે હસ્તી ચલાવી રહ્યો છે, વહી ખુદા છે\nદેખાઈ ભી જો ન દે નઝર ભી જો આ રહા હૈ, વહી ખુદા છે\n\nવહી છે મશરિક વહી છે મગરીબ સફર ભી ઉસકા કયામ ઉસકા",
                arabicLyrics = "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ مُحَمَّدٌ رَّسُولُ ٱللَّٰهِ",
                isFavorite = true,
                addedBy = "azazmadkiya@gmail.com"
            ),
            NaatItem(
                id = "naat_5",
                title = "ख्वाजा का दीवाना",
                category = "gareeb",
                reciter = "कलाम",
                lyrics = "Khwaja Ka Deewana Ban Kar Dekh Le\nSaare Gham Dhul Jaayenge Ajmer Aa Kar Dekh Le\nSultaan-ul-Hind Khwaja Moinuddin Hasan\nDar Pe Unke Jo Jhuka Wo Baadshah Ban Gaya",
                hindiLyrics = "ख्वाजा का दीवाना बन कर देख ले\nसारे ग़म धुल जाएंगे अजमेर आ कर देख ले\nसुलतानुल हिन्द ख्वाजा मोईनुद्दीन हसन\nदर पे उनके जो झुका वो बादशाह बन गया\n\nहिन्द के राजा मेरे ख्वाजा पिया\nकर दो करम मुझ पे ख्वाजा पिया",
                gujaratiLyrics = "ખ્વાજા કા દીવાના બન કર દેખ લે\nસારે ગમ ધુલ જાએંગે અજમેર આ કર દેખ લે\nસુલતાનુલ હિન્દ ખ્વાજા મોઈનુદ્દીન હસન\nદર પે ઉનકે જો ઝુક્યા વો બાદશાહ બન ગયા",
                arabicLyrics = "",
                isFavorite = false,
                addedBy = "azazmadkiya@gmail.com"
            )
        )

        private val cachedNaats = mutableListOf<NaatItem>().apply {
            addAll(getInitialNaats())
        }
        private val cachedCategories = mutableListOf<CategoryItem>()
    }

    // Admin panel se naat save hote hi ye automatic real-time flow emit karega
    fun getNaatsRealtime(): Flow<List<Naat>> = callbackFlow {
        // Always emit current cached/initial list first
        trySend(cachedNaats.map { it.toNaat() })

        val collection = naatsCollection
        if (collection == null) {
            awaitClose { }
            return@callbackFlow
        }

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Log warning but DO NOT call close(error), so flow remains active
                Log.w("NaatRepository", "Firestore realtime note (rules or offline): ${error.message}")
                trySend(cachedNaats.map { it.toNaat() })
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val naatList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Naat::class.java)?.apply { this.id = doc.id }
                }
                if (naatList.isNotEmpty()) {
                    cachedNaats.clear()
                    cachedNaats.addAll(naatList.map { it.toNaatItem() })
                    trySend(naatList)
                }
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
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Naat::class.java)?.let { naat ->
                            naat.id = doc.id
                            naat.toNaatItem()
                        } ?: doc.toObject(NaatItem::class.java)?.copy(id = doc.id)
                    }
                    if (list.isNotEmpty()) {
                        cachedNaats.clear()
                        cachedNaats.addAll(list)
                        onUpdate(cachedNaats)
                    }
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
        onUpdate(cachedCategories)
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
            Log.w("NaatRepository", "Categories snapshot notice: ${e.message}")
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
            Log.w("NaatRepository", "Failed to fetch categories from cloud: ${e.message}")
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
                if (list.isNotEmpty()) {
                    cachedNaats.clear()
                    cachedNaats.addAll(list)
                    return cachedNaats
                }
            }
        } catch (e: Exception) {
            Log.w("NaatRepository", "Failed to fetch naats from cloud: ${e.message}")
        }
        if (cachedNaats.isEmpty()) {
            cachedNaats.addAll(getInitialNaats())
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
