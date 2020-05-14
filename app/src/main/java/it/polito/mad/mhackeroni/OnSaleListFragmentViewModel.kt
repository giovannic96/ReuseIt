package it.polito.mad.mhackeroni

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.mhackeroni.utilities.DAO
import kotlinx.coroutines.*

enum class DataTypes {
    PROFILE, ITEMS
}

class OnSaleListFragmentViewModel : ViewModel() {

    // TODO: Remove profile reference
    var uid: String = "" //id of the logged user

    private val items : MutableLiveData<List<Item>> by lazy {
        MutableLiveData<List<Item>>().also {
            loadItems()
        }
    }

    private val profile : MutableLiveData<Profile> by lazy {
        MutableLiveData<Profile>().also {
            loadProfile()
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            getDataFromDAO(DataTypes.ITEMS)
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            getDataFromDAO(DataTypes.PROFILE)
        }
    }

    fun getItems() : LiveData<List<Item>> {
        return items
    }

    fun getProfile() :LiveData<Profile> {
        return profile
    }

    private suspend fun getDataFromDAO(type:DataTypes)  = withContext(Dispatchers.IO) {
        val dao : DAO = DAO.instance

        if(type == DataTypes.ITEMS) {
            val data = async { dao.getItems() }
            try {
                items.postValue(data.await())
            } catch (e : Exception)  {
                items.postValue(listOf())
            }
        }
        else {
            val data = async { dao.getProfileById(uid) }
            try {
                profile.postValue(data.await())
            } catch (e : Exception)  {
                profile.postValue(Profile())
            }
        }
    }
}

