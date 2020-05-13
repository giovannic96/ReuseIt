package it.polito.mad.mhackeroni.utilities

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.mhackeroni.Item
import it.polito.mad.mhackeroni.Profile

// DAO singleton class
public class DAO private constructor() {
    private var db : FirebaseFirestore = FirebaseFirestore.getInstance()

    private object GetInstance {
        val INSTANCE = DAO()
    }

    companion object {
        val instance: DAO by lazy { GetInstance.INSTANCE }
    }

    // TODO: Using annotation for casting
    fun getItems() : MutableList<Item> {
        var items: MutableList<Item> = mutableListOf()

        db.collection("items")
            .get()
            .addOnSuccessListener{
                for (document in it.documents) {
                    val item = document.toObject(Item::class.java)
                    if (item != null) {
                        items.add(item)
                    }
                }
            }
            return items
    }

    fun getProfileById(id: String): Profile {
        var retProfile = Profile()

        // get profile data from db
        db.collection("users")
            .document(id)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val profile = document.toObject(Profile::class.java)
                        if(profile != null)
                            retProfile = profile
                    }
                    else {
                        Log.w("KKK", "No such document. Now the document will be created")
                        createUserDocument(id)
                    }
                } else {
                    throw task.exception!!
                }
            }
        return retProfile
    }

    private fun createUserDocument(docName: String) {
        val profileDetails = hashMapOf(
            "fullName" to "",
            "nickname" to "",
            "email" to "",
            "location" to "",
            "image" to "",
            "bio" to "",
            "phoneNumber" to ""
        )
        db.collection("users").document(docName)
            .set(profileDetails)
            .addOnSuccessListener { Log.d("KKK", "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w("KKK", "Error writing document", e) }
    }

    fun insertItem(item: Item) {
        db.collection("items")
            .add(item)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    Log.d("KKK","Add item: ${item.name}")
                } else {
                    throw it.exception!!
                }
            }
    }
}