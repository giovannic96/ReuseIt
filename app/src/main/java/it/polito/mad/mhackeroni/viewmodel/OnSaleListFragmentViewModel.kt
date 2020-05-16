package it.polito.mad.mhackeroni.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import kotlinx.coroutines.runBlocking

class OnSaleListFragmentViewModel : ViewModel() {
    private var items : MutableLiveData<List<Item>> = MutableLiveData()

    var uid : String = ""

    fun getItems(): LiveData<List<Item>>{
        val repo = FirebaseRepo.INSTANCE
        repo.getItemsRef(Item.ItemState.AVAILABLE).addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                items.value = mutableListOf()
                return@EventListener
            }

            val itemList : MutableList<Item> = mutableListOf()
            for (doc in value!!) {
                val queryItems = doc.toObject(Item::class.java)
                queryItems.id = doc.id
                if(queryItems.user != uid) {
                    itemList.add(queryItems)
                }
            }
            items.value = itemList
        })
        return items
    }
}

