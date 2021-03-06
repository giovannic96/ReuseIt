package it.polito.mad.mhackeroni.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import java.text.SimpleDateFormat
import java.util.*

class OnSaleListFragmentViewModel : ViewModel() {
    private var items : MutableLiveData<List<Item>> = MutableLiveData()
    var uid : String = ""



    fun getItems(): LiveData<List<Item>>{
        val format = SimpleDateFormat("yyyy-MM-dd")
        format.timeZone = TimeZone.getTimeZone("Etc/UTC")
        val today = format.format(Date().time)

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
                if(queryItems.user != uid && queryItems.expiryDate >= today) {
                    itemList.add(Item.localize(queryItems))
                }
            }
            items.value = itemList
        })
        return items
    }
}

