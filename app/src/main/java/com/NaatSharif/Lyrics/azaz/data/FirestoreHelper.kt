package com.NaatSharif.Lyrics.azaz.data

import com.NaatSharif.Lyrics.azaz.model.Naat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirestoreHelper {

    private val db = FirebaseFirestore.getInstance()
    private val naatsCollection = db.collection("naats")

    // Ella Naat galannu fetch maaduva function
    fun getAllNaats(
        onSuccess: (List<Naat>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        naatsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val naatList = result.documents.mapNotNull { document ->
                    document.toObject(Naat::class.java)?.apply {
                        id = document.id
                    }
                }
                onSuccess(naatList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Hosa Naat add maaduva function
    fun addNaat(
        naat: Naat,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        naatsCollection
            .add(naat)
            .addOnSuccessListener { documentReference ->
                onSuccess(documentReference.id)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
