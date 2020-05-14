package it.polito.mad.mhackeroni.utilities

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.mhackeroni.Item
import it.polito.mad.mhackeroni.Profile
import it.polito.mad.mhackeroni.R

// DAO singleton class
public class FirebaseRepo private constructor() {
    private var db : FirebaseFirestore = FirebaseFirestore.getInstance()

    private object GetInstance {
        val INSTANCE = FirebaseRepo()
    }

    companion object {
        val INSTANCE: FirebaseRepo by lazy { GetInstance.INSTANCE }
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
                        if(profile != null) {
                            retProfile = profile
                        }
                    }
                    else {
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

    fun updateProfile(profile: Profile, userID: String){
        db.collection("users")
            .document(userID)
            .update(hashMapOf(
                "fullName" to profile.fullName,
                "nickname" to profile.nickname,
                "email" to profile.email,
                "location" to profile.location,
                "image" to profile.image,
                "bio" to profile.bio,
                "phoneNumber" to profile.phoneNumber
        ) as Map<String, Any>)
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

    fun getID(context: Context) : String{
        val sharedPref = context
            .getSharedPreferences(context.getString(R.string.shared_pref), Context.MODE_PRIVATE)
        val uid = sharedPref.getString(context.getString(R.string.uid), "")!!

        return uid
    }
    fun getItemRef(id : String) : DocumentReference{
        return db.collection("items").document(id)
    }

    fun getItemsRef(): CollectionReference {
        var collectionReference = db.collection("items")
        return collectionReference
    }

    fun getItemsRef(uid: String) : Query {

        var collectionReference = db.collection("items")
        var selectedCollection = collectionReference.whereEqualTo("user", uid)

       return selectedCollection
    }


    fun getProfileRef(uid: String): DocumentReference{
        return  db.collection("users").document(uid)
    }

    fun updateItem(id : String, item : Item){

        Log.d("MAG2020", "Updating to; ${Item.toJSON(item)}")

        db.collection("items")
            .document(id)
            .update(hashMapOf(
                "category" to item.category,
                "condition" to item.condition,
                "desc" to item.desc,
                "expiryDate" to item.expiryDate,
                "id" to id,
                "image" to item.image,
                "location" to item.location,
                "name" to item.name,
                "price" to item.price,
                "subcategory" to item.subcategory,
                "user" to item.user
            ) as Map<String, Any>).addOnCompleteListener{
                if (it.isSuccessful){
                    val ref = uploadItemImage(Uri.parse(item.image), id)
                    db.collection("items").document(id).update(
                        hashMapOf("image" to ref) as Map<String, Any>
                    )
                } else {
                    Log.d("MAG2020", it.exception.toString())
                }
            }
    }

    fun clearPersistency(){
        db.clearPersistence()
    }
}