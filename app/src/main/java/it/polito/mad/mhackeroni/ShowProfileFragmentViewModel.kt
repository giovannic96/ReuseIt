package it.polito.mad.mhackeroni

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.mhackeroni.utilities.DAO
import kotlinx.coroutines.*

class ShowProfileFragmentViewModel : ViewModel() {

    var uid: String = "" //id of the logged user

    private val profile : MutableLiveData<Profile> by lazy {
        MutableLiveData<Profile>().also {
            loadProfile()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            getDataFromDAO(DataTypes.PROFILE)
        }
    }

    fun getProfile() :LiveData<Profile> {
        return profile
    }

    private suspend fun getDataFromDAO(type:DataTypes)  = withContext(Dispatchers.IO) {
        val dao : DAO = DAO.instance

        val data = async { dao.getProfileById(uid) }

        try {
            profile.postValue(data.await())
        } catch (e : Exception)  {
            profile.postValue(Profile())
        }
    }
}

