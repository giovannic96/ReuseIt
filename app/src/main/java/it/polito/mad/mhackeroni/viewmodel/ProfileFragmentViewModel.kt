package it.polito.mad.mhackeroni.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.mhackeroni.model.Profile
import it.polito.mad.mhackeroni.utilities.FirebaseRepo

class ProfileFragmentViewModel : ViewModel() {
    var uid : String = ""
    private var profile : MutableLiveData<Profile> = MutableLiveData()

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

