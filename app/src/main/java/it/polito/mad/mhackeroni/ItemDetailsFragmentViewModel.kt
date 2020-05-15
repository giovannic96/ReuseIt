package it.polito.mad.mhackeroni

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import kotlinx.coroutines.*

class ItemDetailsFragmentViewModel : ViewModel() {
    var itemId : String = ""
    private var profile : MutableLiveData<Profile> = MutableLiveData()
    private var item : MutableLiveData<Item> = MutableLiveData()

    fun getItem(): LiveData<Item>{
        val repo = FirebaseRepo.INSTANCE
        repo.getItemRef(itemId).addSnapshotListener{snapshot, e ->
            if(e != null){
                return@addSnapshotListener
            }

            if(snapshot != null && snapshot.exists()){
                item.value = snapshot.toObject(Item::class.java)
                item.value?.id  = snapshot.id
                runBlocking {
                    launch { item.value?.user?.let { loadProfile(it) } }
                }
            } else {
                item.value = Item()
            }
        }


        return item

    }

    fun getProfile(): LiveData<Profile>{
        return profile
    }

    fun loadProfile(profileID : String): LiveData<Profile>{
        val repo = FirebaseRepo.INSTANCE
        repo.getProfileRef(profileID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                profile.value = snapshot.toObject(Profile::class.java)
            } else {
                profile.value = Profile()
            }
        }

        return profile
    }

}

