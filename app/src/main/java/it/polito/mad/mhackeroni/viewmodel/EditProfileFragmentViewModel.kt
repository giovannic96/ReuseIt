package it.polito.mad.mhackeroni.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import it.polito.mad.mhackeroni.model.Profile
import it.polito.mad.mhackeroni.utilities.FirebaseRepo

class EditProfileFragmentViewModel : ViewModel() {
    var uid : String = ""
    private var profile : MutableLiveData<Profile> = MutableLiveData()
    private var localProfile : Profile? = null

    fun getProfile(): LiveData<Profile>{
        val repo = FirebaseRepo.INSTANCE

        repo.getProfileRef(uid).get().addOnCompleteListener {task ->
            profile.value = Profile()

            if(task.isSuccessful){
                if(task.result?.exists()!!){
                    profile.value = task?.result?.toObject(Profile::class.java)
                }
            }

        }

        if(localProfile != null){
            profile.value = localProfile
        }

        return profile
    }

    fun updateProfile(newProfile : Profile){
        localProfile = newProfile
    }

    fun getLocalProfile() : Profile?{
        return localProfile
    }

    fun updateDB(): Task<Void> {
        val repo : FirebaseRepo = FirebaseRepo.INSTANCE
        val newProfile = localProfile!!

        return repo.updateProfile(newProfile,uid)


    }
}