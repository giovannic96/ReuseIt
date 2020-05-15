package it.polito.mad.mhackeroni

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.mhackeroni.utilities.FirebaseRepo

class OnSaleListFragmentViewModel : ViewModel() {
    var items : MutableLiveData<List<Item>> = MutableLiveData()
    var uid : String = ""

    fun getItems(): LiveData<List<Item>>{
        val repo = FirebaseRepo.INSTANCE
        repo.getItemsRef(Item.ItemState.AVAILABLE).addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                items.value = mutableListOf()
                return@EventListener
            }

            var itemList : MutableList<Item> = mutableListOf()
            for (doc in value!!) {
                var addressItem = doc.toObject(Item::class.java)
                addressItem.id = doc.id
                if(addressItem.user != uid)
                    itemList.add(addressItem)
            }
            items.value = itemList
        })

        return items
    }
}

