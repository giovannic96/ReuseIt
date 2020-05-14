package it.polito.mad.mhackeroni.utilities

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import it.polito.mad.mhackeroni.Item
import it.polito.mad.mhackeroni.Profile
import java.io.File
import java.net.URI

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
        val documentId : String? = null

        db.collection("items")
            .add(item)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    if(!item.image.isNullOrEmpty()){
                        it.result?.id?.let { it1 ->
                            val ref = uploadItemImage(Uri.parse(item.image), it1)
                            db.collection("items").document(it.result!!.id).update(
                                hashMapOf("image" to ref) as Map<String, Any>
                            )
                        }
                    }
                } else {
                    throw it.exception!!
                }
            }
    }

    fun uploadItemImage(uri : Uri, documentId : String) : String {
        val storage = Firebase.storage
        var storageRef = storage.reference

        var ref = storageRef.child("items_images/${documentId}.jpg")
        ref.putFile(uri)

        return ref.name
    }

    fun getUserItem(userID : String) : List<Item>{
        TODO("Not finished")
    }
}