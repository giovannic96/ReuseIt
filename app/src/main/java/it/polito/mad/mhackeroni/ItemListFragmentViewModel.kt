package it.polito.mad.mhackeroni

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.mhackeroni.utilities.DAO
import kotlinx.coroutines.*

class ItemListFragmentViewModel : ViewModel() {

    val userId : String = ""

    private val items : MutableLiveData<List<Item>> by lazy {
        MutableLiveData<List<Item>>().also {
            loadItems()
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            getDataFromDAO(DataTypes.ITEMS)
        }
    }
    fun getItems() : LiveData<List<Item>> {
        return items
    }

    private suspend fun getDataFromDAO(type:DataTypes)  = withContext(Dispatchers.IO) {
        val dao : DAO = DAO.instance

        val data = async { dao.getUserItem(userId) }
        try {
            items.postValue(data.await())
        } catch (e : Exception)  {
            items.postValue(listOf())
        }
    }
}

