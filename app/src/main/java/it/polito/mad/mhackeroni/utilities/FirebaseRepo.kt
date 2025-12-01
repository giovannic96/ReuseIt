package it.polito.mad.mhackeroni.utilities

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.firestore.*
import com.google.firebase.firestore.Query
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.model.Profile
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


// DAO singleton class
 class FirebaseRepo private constructor() {
    private var db : FirebaseFirestore = FirebaseFirestore.getInstance()

    var isLogged = false

    private object GetInstance {
        val INSTANCE = FirebaseRepo()
    }

    companion object {
        val INSTANCE: FirebaseRepo by lazy { GetInstance.INSTANCE }
    }

    fun setProfile(profile: Profile, userID : String){
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                if (token != null) {
                    db.collection("users").document(userID).get().addOnCompleteListener {
                        if(it.isSuccessful) {
                            if(!it.result?.exists()!!) {
                                db.collection("users").document(userID).set(profile).addOnCompleteListener {
                                    if(it.isSuccessful) {
                                        db.collection("users").document(userID).update(
                                            hashMapOf("token" to token) as Map<String, Any>
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            })
    }

    fun updateProfile(profile: Profile, userID: String): Task<Void> {
        return db.collection("users")
            .document(userID)
            .update(hashMapOf(
                "fullName" to profile.fullName,
                "nickname" to profile.nickname,
                "email" to profile.email,
                "location" to profile.location,
                "image" to profile.image,
                "bio" to profile.bio,
                "phoneNumber" to profile.phoneNumber,
                "totRating" to profile.totRating,
                "numRating" to profile.numRating,
                "feedbacks" to profile.feedbacks,
                "lat" to profile.lat,
                "lng" to profile.lng
        ) as Map<String, Any>).addOnCompleteListener{
                if(it.isSuccessful){
                    if(!profile.image.isNullOrEmpty()){
                        val uploadImage = uploadProfileImage(Uri.parse(profile.image), userID)
                        val imageRef = uploadImage.second
                        uploadImage.first.addOnCompleteListener {
                            db.collection("users").document(userID).update(
                                hashMapOf("image" to imageRef) as Map<String, Any>
                            )
                        }

                    }
                }
            }
    }

    fun updateToken(uid : String){
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val token = task.result?.token
                    db.collection("users")
                        .document(uid)
                        .update(hashMapOf("token" to token) as Map<String, Any>)
                }
            }
    }

    fun updateUserToken(uid : String, token : String): Task<Void> {
        return db.collection("users")
            .document(uid)
            .update(
                hashMapOf(
                    "token" to token) as Map<String, Any>)
    }

    fun updateRating(uid: String, totRating: Double, numRating: Double): Task<Void>{
        return db.collection("users")
            .document(uid)
            .update(
                hashMapOf(
                    "totRating" to totRating,
                    "numRating" to numRating
                ) as Map<String, Any>
            )
    }

    fun updateFeedback(uid: String, feedback: String): Task<Void>{
        return db.collection("users")
            .document(uid)
            .update("feedbacks", FieldValue.arrayUnion(feedback))
    }

    fun insertItem(item: Item): Task<DocumentReference> {

        Log.d("MAD2020", "Item image: ${item.image}")
        val imagePath = item.image

        val localizedItem = Item.localize(item, true)
        localizedItem.image = ""

       return db.collection("items")
            .add(localizedItem)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    if(!imagePath.isNullOrEmpty()){
                        it.result?.id?.let { id ->
                            val uploadTask = uploadItemImage(Uri.parse(imagePath), id)
                            val getUriTask = uploadTask.second
                            uploadTask.first.addOnCompleteListener {
                                if(it.isSuccessful){
                                    getUriTask.downloadUrl.addOnCompleteListener { task ->
                                        if(task.isSuccessful){
                                            db.collection("items").document(id).update(hashMapOf("image" to task.result.toString()) as Map<String, Any>)
                                            Log.d("MAD2020", "Upload successfull")
                                        }
                                    }
                                } else {
                                    Log.d("MAD2020", "Exception: ${it.exception}")
                                }
                            }
                        }
                    }
                } else {
                    throw it.exception!!
                }
            }
    }

    private fun uploadProfileImage(uri : Uri, profileID : String) : Pair<UploadTask, String> {
        val storage = Firebase.storage
        var storageRef = storage.reference

        var ref = storageRef.child("profiles_images/${profileID}.jpg")
        return Pair(ref.putFile(uri), ref.name)
    }

    private fun uploadItemImage(uri : Uri, documentId : String) : Pair<UploadTask, StorageReference> {
        val storage = Firebase.storage
        var storageRef = storage.reference

        var ref = storageRef.child("items_images/${documentId}.jpg")

        Log.d("MAD2020", "uploading: ${uri}")

        return Pair(ref.putFile(uri), ref)
    }

    fun getUserItem(userID : String) : List<Item>{
        TODO("Not finished")
    }

    fun getID(context: Context) : String{
        val sharedPref = context.getSharedPreferences(context.getString(R.string.shared_pref), Context.MODE_PRIVATE)
        return sharedPref.getString(context.getString(R.string.uid), "")!!
    }

    fun getItemRef(id : String) : DocumentReference{
        return db.collection("items").document(id)
    }

    fun getItemsRef(state : Item.ItemState = Item.ItemState.AVAILABLE): Query {
        val collectionReference = db.collection("items")
       return collectionReference.whereEqualTo("state", state)
    }

    fun getBoughtItem(uid:String): Query {
        return db.collection("items")
            .whereEqualTo("buyer", uid)
    }

    fun getItemsRef(uid: String) : Query {
        val collectionReference = db.collection("items")
        return collectionReference.whereEqualTo("user", uid)
    }

    fun getFavorites(uid : String) : Query {
        return db.collection("favorites")
            .whereEqualTo("user", uid)
    }

    fun getProfileRef(uid: String): DocumentReference {
        return  db.collection("users").document(uid)
    }

    fun getBuyerByNickname(nickname : String) : Task<QuerySnapshot> {
        return db.collection("users")
            .whereEqualTo("nickname", nickname).get()
    }

    fun updateItem(id : String, item : Item, uploadImage : Boolean = true): Task<Void> {
        var imageLink : String? = ""

        if(!uploadImage) {
            imageLink = item.image
        }

        val localizedItem = Item.localize(item, true)

       return db.collection("items")
            .document(id)
            .update(hashMapOf(
                "category" to localizedItem.category,
                "condition" to localizedItem.condition,
                "desc" to item.desc,
                "expiryDate" to item.expiryDate,
                "id" to id,
                "image" to imageLink,
                "location" to item.location,
                "name" to item.name,
                "price" to item.price,
                "subcategory" to localizedItem.subcategory,
                "user" to item.user,
                "state" to item.state,
                "buyer" to item.buyer,
                "hasFeedback" to item.hasFeedback,
                "lat" to item.lat,
                "lng" to item.lng
            ) as Map<String, Any>).addOnCompleteListener{
                if (it.isSuccessful && uploadImage){
                    Log.d("XXX", "Im uploading ${uploadImage}")
                    val uploadTask = uploadItemImage(Uri.parse(item.image), id)
                    val getUriTask = uploadTask.second
                    uploadTask.first.addOnCompleteListener {
                        if(it.isSuccessful){
                            getUriTask.downloadUrl.addOnCompleteListener { task ->
                                if(task.isSuccessful){
                                    db.collection("items").document(id).update(hashMapOf("image" to task.result.toString()) as Map<String, Any>)
                                }
                            }
                        }
                    }
                }
            }
    }

    fun updateItemSold(itemId : String, buyerId : String): Task<Void> {
        return db.collection("items")
            .document(itemId)
            .update("buyer", buyerId).addOnCompleteListener {
                if (it.isSuccessful) {
                    db.collection("items")
                        .document(itemId)
                        .update("state", "SOLD")
                }
            }
    }

    fun checkFavorite(user : String, item : String) : Task<QuerySnapshot> {
       return db.collection("favorites")
            .whereEqualTo("user", user)
            .whereEqualTo("item", item).get()
    }

    fun checkNickname(nickname : String) : Task<QuerySnapshot> {
        return db.collection("users")
            .whereEqualTo("nickname", nickname).get()
    }

    fun insertFavorite(user : String, item : Item): Task<DocumentReference> {
        return db.collection("favorites")
            .add(hashMapOf(
                    "user" to user,
                    "item" to item.id,
                    "seller" to item.user,
                    "itemState" to item.state
            ))
    }

    fun insertFeedback(item: Item, hasFeedback: Boolean): Task<Void> {
        return db.collection("items")
            .document(item.id)
            .update(
                hashMapOf(
                    "hasFeedback" to hasFeedback
                ) as Map<String, Any>)
    }

    fun removeFavorite(docId: String): Task<Void> {
        return db.collection("favorites")
            .document(docId)
            .delete()
    }

    fun getFavDocId(user: String, item: String): Task<QuerySnapshot> {
        return db.collection("favorites")
            .whereEqualTo("item", item)
            .whereEqualTo("user", user)
            .get()
    }

    fun getInterestedProfile(item : String) : LiveData<List<Profile>>{
        val profiles : MutableLiveData<List<Profile>> = MutableLiveData()
        val profileList : MutableList<Profile> = mutableListOf()

        db.collection("favorites")
            .whereEqualTo("item", item)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    if(!it.result?.isEmpty!!){
                        it.result?.forEach {snap ->
                            val user : String = snap["user"] as String
                            db.collection("users").document(user).get().addOnCompleteListener {
                                if(it.isSuccessful){
                                    if(it.result?.exists()!!){
                                        it.result!!.toObject(Profile::class.java)?.let { it1 ->
                                            it1.id = user
                                            profileList.add(it1)
                                        }
                                        profiles.value = listOf()
                                        profiles.value = profileList
                                    }
                                }
                            }
                        }
                    }
                }
            }
        return profiles
    }

    fun logout(context: Context): Task<Void> {
        val sharedPref = context.getSharedPreferences(context.getString(R.string.shared_pref), Context.MODE_PRIVATE)

        return db.collection("users")
            .document(getID(context))
            .update(hashMapOf(
                "token" to ""
            ) as HashMap<String, Any>)
    }

    fun getInterestedItems(user : String) : LiveData<List<Item>>{
        val items : MutableLiveData<List<Item>> = MutableLiveData()
        val itemList : MutableList<Item> = mutableListOf()

        db.collection("favorites")
            .whereEqualTo("user", user)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    if(!it.result?.isEmpty!!) {
                        it.result?.forEach { snap ->
                            val item : String = snap["item"] as String
                            db.collection("items").document(item).get().addOnCompleteListener {
                                if(it.isSuccessful) {
                                    if(it.result?.exists()!!) {
                                        it.result!!.toObject(Item::class.java)?.let { it1 ->
                                            it1.id = item
                                            itemList.add(it1)
                                        }
                                        items.value = listOf()
                                        items.value = itemList
                                    }
                                }
                            }
                        }
                    }
                }
            }
        return items
    }

    fun getBuyedItems(user : String) : LiveData<List<Item>>{
        val items : MutableLiveData<List<Item>> = MutableLiveData()
        val itemList : MutableList<Item> = mutableListOf()

        db.collection("items")
            .whereEqualTo("buyer", user)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    if(!it.result?.isEmpty!!) {
                        it.result?.forEach { snap ->
                            val item : String = snap["item"] as String
                            db.collection("items").document(item).get().addOnCompleteListener {
                                if(it.isSuccessful) {
                                    if(it.result?.exists()!!) {
                                        it.result!!.toObject(Item::class.java)?.let { it1 ->
                                            it1.id = item
                                            itemList.add(it1)
                                        }
                                        items.value = listOf()
                                        items.value = itemList
                                    }
                                }
                            }
                        }
                    }
                }
            }
        return items
    }

    // just for debug
    fun clearPersistency(){
        db.clearPersistence()
    }

    fun blockItem(itemID : String){
        db.collection("items")
            .document(itemID)
            .update(hashMapOf("state" to Item.ItemState.BLOCKED) as Map<String, Any>)
    }
}