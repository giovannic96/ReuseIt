package it.polito.mad.mhackeroni.utilities

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.iid.FirebaseInstanceId
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
        ) as Map<String, Any>).addOnCompleteListener{
                if(it.isSuccessful){
                    if(!profile.image.isNullOrEmpty()){
                        val ref = uploadProfileImage(Uri.parse(profile.image), userID)
                        db.collection("users").document(userID).update(
                            hashMapOf("image" to ref) as Map<String, Any>
                        )
                    }
                }
            }
    }

    fun insertItem(item: Item) {
        db.collection("items")
            .add(item)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    if(!item.image.isNullOrEmpty()){
                        it.result?.id?.let { it1 ->
                            val refImage = uploadItemImage(Uri.parse(item.image), it1)
                            db.collection("items").document(it.result!!.id).update(
                                hashMapOf("image" to refImage) as Map<String, Any>
                            )
                        }
                    }
                } else {
                    throw it.exception!!
                }
            }
    }

    fun uploadProfileImage(uri : Uri, profileID : String) : String {
        val storage = Firebase.storage
        var storageRef = storage.reference

        var ref = storageRef.child("profiles_images/${profileID}.jpg")
        ref.putFile(uri)

        return ref.name
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

    fun getItemsRef(state : Item.ItemState = Item.ItemState.AVAILABLE): Query {
        var collectionReference = db.collection("items")
        var selectedCollection = collectionReference.whereEqualTo("state", state)

        return selectedCollection
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
                "user" to item.user,
                "state" to item.state
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

    fun checkFavorite(user : String, item : String) : Task<QuerySnapshot> {
       return db.collection("favorites")
            .whereEqualTo("user", user).whereEqualTo("item", item).get()
    }

    fun insertFavorite(user : String, item : Item): Task<DocumentReference> {
        return db.collection("favorites")
            .add(hashMapOf(
                    "user" to user,
                    "item" to item.id,
                    "seller" to item.user
            ))
    }

    fun clearPersistency(){
        db.clearPersistence()
    }
}