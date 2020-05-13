package it.polito.mad.mhackeroni.utilities

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import it.polito.mad.mhackeroni.Item
import it.polito.mad.mhackeroni.Profile
import java.sql.Timestamp

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
            .addOnCompleteListener{
                if (it.isSuccessful) {
                    for (document in it.result!!) {
                        Log.d("MAD2020", document.id + " => " + document.data)

                        items.add(Item(
                            (document.get("id") as Long).toInt(),
                            document.get("name") as String,
                            document.get("price") as Double,
                            document.get("description") as String,
                            document.get("category") as String,
                            document.get("subcategory") as String,
                            (document.get("expire_date") as com.google.firebase.Timestamp).toString(),
                            (document.get("location") as GeoPoint).toString(),
                            document.get("condition") as String,
                            null))
                    }
                } else {
                  throw it.exception!!
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
        val profileDetails = hashMapOf (
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
}