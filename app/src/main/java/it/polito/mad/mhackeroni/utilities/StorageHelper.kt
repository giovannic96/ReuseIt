package it.polito.mad.mhackeroni.utilities

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.beust.klaxon.Klaxon
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import it.polito.mad.mhackeroni.Item
import it.polito.mad.mhackeroni.Profile
import it.polito.mad.mhackeroni.R


class StorageHelper(context: Context?) {

    private val myContext:Context? = context

    fun saveItemList(s: SharedPreferences, l:MutableList<Item>) {
        with (s.edit()) {
            putString(myContext?.getString(R.string.itemList_sharedPref), Klaxon().toJsonString(l))
            apply()
        }
    }

    fun loadItemList(s: SharedPreferences):MutableList<Item> {
        var jSONString: String? = s.getString(myContext?.getString(R.string.itemList_sharedPref), "")
        if(!jSONString.isNullOrEmpty()) {
            if(jSONString[0] != '[')
                jSONString = "[${jSONString}]" //convert to json array
            val items = Klaxon().parseArray<Item>(jSONString)?.toMutableList()
            if (items != null) {
                return items
            }
        }
        return mutableListOf()
    }

    fun editItem(s: SharedPreferences, item: Item){
        var jSONString: String? = s.getString(myContext?.getString(R.string.itemList_sharedPref), "")
        if(!jSONString.isNullOrEmpty()) {
            if(jSONString[0] != '[')
                jSONString = "[${jSONString}]" //convert to json array
            val items = Klaxon().parseArray<Item>(jSONString)?.toMutableList()

            if (items != null) {
                for(i in 0..items.size){
                    if(items[i].id == item.id){
                        items[i] = item
                        Log.d("MAG", Item.toJSON(
                            item
                        ).toString())
                        saveItemList(s, items)
                        return
                    }
                }

                Log.d("MAG","Not found id ${item.id}")
            } else {
                Log.d("MAG","NULL ERROR")
            }
        }
    }

    fun saveProfile(s: SharedPreferences, p: Profile) {
        with (s.edit()) {
            putString(myContext?.getString(R.string.profile_sharedPref), Profile.toJSON(
                p
            ).toString())
            apply()
        }
    }

    fun loadProfile(s: SharedPreferences): Profile? {
        val jSONString : String? = s.getString(myContext?.getString(R.string.profile_sharedPref), "")
        return jSONString?.let {
            Profile.fromStringJSON(
                it
            )
        }
    }

    fun saveProfile(db: FirebaseFirestore, p: Profile) {
        val user: MutableMap<String, Any> = HashMap()
        user["fullName"] = p.fullName
        user["nickname"] = p.nickname
        user["email"] = p.email
        user["bio"] = p.bio
        if(p.image.isNullOrEmpty()) user["image"] = ""
        else user["image"] = p.image!!
        user["location"] = p.location
        user["phoneNumber"] = p.phoneNumber

        // Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener(OnSuccessListener<DocumentReference> {
                Log.d(TAG,"DocumentSnapshot added with ID: " + p.nickname)
            })
            .addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error adding document", e) })
    }

    fun loadProfile(db: FirebaseFirestore, id: String): Profile? {
        var retProfile: Profile? = null

        // get profile data from db
        db.collection("users")
            .document(id)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    Log.d("KKK", document.toString())
                    if (document != null && document.exists()) {
                        val profile = document.toObject(Profile::class.java)
                        Log.d("KKK", profile?.fullName)
                        if(profile != null)
                            retProfile = profile
                        /*
                        if(!document.data.isNullOrEmpty()) {
                            retProfile = Profile(
                                document.data!!["fullName"] as String,
                                document.data!!["nickname"] as String,
                                document.data!!["email"] as String,
                                document.data!!["location"] as String,
                                document.data!!["image"] as String,
                                document.data!!["bio"] as String,
                                document.data!!["phoneNumber"] as String)
                        }*/
                    }
                    else {
                        Log.w("KKK", "No such document. Now the document will be created")
                        createUserDocument(db, id)
                    }
                } else {
                    Log.w("KKK", "Error getting documents.", task.exception)
                }
            }
        return retProfile
    }

    private fun createUserDocument(db: FirebaseFirestore, docName: String) {
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

}