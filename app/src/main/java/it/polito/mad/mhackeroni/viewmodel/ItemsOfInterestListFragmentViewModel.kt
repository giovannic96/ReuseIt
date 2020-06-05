package it.polito.mad.mhackeroni.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.utilities.FirebaseRepo

class ItemsOfInterestListFragmentViewModel : ViewModel() {
    private var itemIds : MutableLiveData<List<String>> = MutableLiveData()
    private var items : MutableLiveData<List<Item>> = MutableLiveData()
    var uid : String = ""

    fun getItemsByIds(ids: List<String>) : LiveData<List<Item>> {
        val repo = FirebaseRepo.INSTANCE
        val itemList: MutableList<Item> = mutableListOf()
        val strings: ArrayList<String> = ArrayList()

        for(id in ids) {
            if (!strings.contains(id)) {
                strings.add(id);
            }
        }

        for(id in strings) {
            repo.getItemRef(id)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        items.value = mutableListOf()
                        return@addSnapshotListener
                    }
                    if(snapshot != null && snapshot.exists()) {
                        val queryItem = snapshot.toObject(Item::class.java)
                        queryItem?.id = snapshot.id
                        if (queryItem != null) {

                            // if item already exist in our list -> update it
                            if(items.value?.find { item -> item.id == queryItem.id } != null) {
                                items.value?.map { item ->
                                    if (item.id == queryItem.id) {
                                        item.state = queryItem.state
                                        item.name = queryItem.name
                                        item.price = queryItem.price
                                        item.category = queryItem.category
                                        item.subcategory = queryItem.subcategory
                                        item.image = queryItem.image
                                        item.location = queryItem.location
                                        item.desc = queryItem.desc
                                        item.expiryDate = queryItem.expiryDate
                                        item.user = queryItem.user
                                        item.condition = queryItem.condition
                                    }
                                }
                            } else { // is a new item -> add it to our list
                                itemList.add(queryItem)
                            }
                        }
                        items.value = itemList
                    }
                }
        }
        return items
    }

    fun getItemIds(): LiveData<List<String>>{
        val repo = FirebaseRepo.INSTANCE
        val itemsId: MutableList<String> = mutableListOf()

        repo.getFavorites(uid).addSnapshotListener { snapshot, e ->
            if (e != null) {
                itemIds.value = mutableListOf()
                return@addSnapshotListener
            }

            if(snapshot != null && !snapshot.isEmpty) {
                for (doc in snapshot.documents) {
                    val id = doc.data?.get("item") as String
                    itemsId.add(id)
                }
            }
            itemIds.value = itemsId
        }
        return itemIds
    }
}

