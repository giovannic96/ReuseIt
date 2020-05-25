package it.polito.mad.mhackeroni.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.utilities.FirebaseRepo

class ItemsOfInterestListFragmentViewModel : ViewModel() {
    var uid : String = ""

    fun getInterestedItems(): LiveData<List<Item>> {
        val repo = FirebaseRepo.INSTANCE
        return repo.getInterestedItems(uid)
    }
}

