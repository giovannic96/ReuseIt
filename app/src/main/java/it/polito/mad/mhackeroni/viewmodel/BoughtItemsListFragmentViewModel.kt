package it.polito.mad.mhackeroni.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.utilities.FirebaseRepo

class BoughtItemsListFragmentViewModel : ViewModel() {
    private var items : MutableLiveData<List<Item>> = MutableLiveData()
    var uid : String = ""

    fun getBoughtItems(): LiveData<List<Item>>{
        val repo = FirebaseRepo.INSTANCE
        repo.getBoughtItem(uid).addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                items.value = mutableListOf()
                return@EventListener
            }

            val itemList : MutableList<Item> = mutableListOf()
            for (doc in value!!) {
                val queryItem = doc.toObject(Item::class.java)
                queryItem.id = doc.id
                itemList.add(queryItem)
            }
            items.value = itemList
        })
        return items
    }
}

