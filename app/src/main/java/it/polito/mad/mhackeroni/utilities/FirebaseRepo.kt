package it.polito.mad.mhackeroni.utilities

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.model.Profile

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
        // Log.d("MAG2020", "Testing: ${userID}")
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                if (token != null) {
                    db.collection("users").document(userID).get().addOnCompleteListener {
                        if(it.isSuccessful){
                            if(!it.result?.exists()!!){
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

    fun insertItem(item: Item): Task<DocumentReference> {
       return db.collection("items")
            .add(item)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    if(!item.image.isNullOrEmpty()){
                        it.result?.id?.let { id ->
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

    fun uploadItemImage(uri : Uri, documentId : String) : Pair<UploadTask, StorageReference> {
        val storage = Firebase.storage
        var storageRef = storage.reference

        var ref = storageRef.child("items_images/${documentId}.jpg")

        return Pair(ref.putFile(uri), ref)
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

    fun updateItem(id : String, item : Item): Task<Void> {
       return db.collection("items")
            .document(id)
            .update(hashMapOf(
                "category" to item.category,
                "condition" to item.condition,
                "desc" to item.desc,
                "expiryDate" to item.expiryDate,
                "id" to id,
                "image" to null,
                "location" to item.location,
                "name" to item.name,
                "price" to item.price,
                "subcategory" to item.subcategory,
                "user" to item.user,
                "state" to item.state
            ) as Map<String, Any>).addOnCompleteListener{
                if (it.isSuccessful){
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

    // just for debug
    fun clearPersistency(){
        db.clearPersistence()
    }
}