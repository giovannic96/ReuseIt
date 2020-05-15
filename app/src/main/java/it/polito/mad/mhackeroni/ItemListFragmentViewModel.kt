package it.polito.mad.mhackeroni

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import it.polito.mad.mhackeroni.utilities.FirebaseRepo

class ItemListFragmentViewModel : ViewModel() {
    private var items : MutableLiveData<List<Item>> = MutableLiveData()
    var uid : String = ""

    fun getItems(): LiveData<List<Item>>{
        val repo = FirebaseRepo.INSTANCE
        repo.getItemsRef(uid).addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                items.value = mutableListOf()
                return@EventListener
            }

            var itemList : MutableList<Item> = mutableListOf()
            for (doc in value!!) {
                var queryItems = doc.toObject(Item::class.java)
                queryItems.id = doc.id
                itemList.add(queryItems)
            }
            items.value = itemList
        })

        return items
    }
}

