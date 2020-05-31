package it.polito.mad.mhackeroni.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.model.Profile
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import kotlinx.coroutines.*

class ItemDetailsFragmentViewModel : ViewModel() {
    var itemId : String = ""
    var owner : String = ""
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
                    launch { item.value?.user?.let { loadProfile(it)
                    owner = it} }
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

    fun getInterestedUsers(item : String): LiveData<List<Profile>> {
        val repo = FirebaseRepo.INSTANCE
        return repo.getInterestedProfile(item)
    }

    fun getLoggedProfile(context : Context): DocumentReference {
        val repo = FirebaseRepo.INSTANCE
        val id = repo.getID(context)
        return repo.getProfileRef(id)
    }

    fun updateItemSold(nicknameBuyer: String): Task<QuerySnapshot> {
        val repo : FirebaseRepo = FirebaseRepo.INSTANCE

        // first get buyer id from its nickname, then update item
        return repo.getBuyerByNickname(nicknameBuyer).addOnCompleteListener {
            if (it.isSuccessful) {
                if (!it.result?.isEmpty!!) {
                    var idDoc: String = ""
                    for (document in it.result!!) {
                        idDoc = document.id
                        break
                    }
                    repo.updateItemSold(itemId, idDoc)
                }
            }
        }
    }
}

