package it.polito.mad.mhackeroni

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.mhackeroni.utilities.DAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class OnSaleListFragmentViewModel : ViewModel() {
   private val items : MutableLiveData<List<Item>> by lazy{
       MutableLiveData<List<Item>>().also {
           loadItems()
       }
   }

    private fun loadItems(){
        viewModelScope.launch {
            getDataFromDAO()
        }
    }

    fun getItems() : LiveData<List<Item>> {
        return items
    }

    private suspend fun getDataFromDAO()  = withContext(Dispatchers.IO){
        val dao : DAO = DAO.instance
        val data = async { dao.getItems()}

        try {
            items.postValue(data.await())
        } catch (e : Exception)  {
            items.postValue(listOf())
        }

    }
}

