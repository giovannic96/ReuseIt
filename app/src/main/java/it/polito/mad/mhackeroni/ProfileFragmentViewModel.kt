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

class ProfileFragmentViewModel : ViewModel() {
    var uid : String = ""
    var profile : MutableLiveData<Profile> = MutableLiveData()

    fun getProfile(): LiveData<Profile>{
        val repo = FirebaseRepo.INSTANCE
        repo.getProfileRef(uid).addSnapshotListener { snapshot, e ->
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

