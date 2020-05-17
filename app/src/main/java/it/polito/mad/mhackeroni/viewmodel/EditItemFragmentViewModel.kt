package it.polito.mad.mhackeroni.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.model.Profile
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import kotlinx.coroutines.*

class EditItemFragmentViewModel : ViewModel() {
    var itemId : String = ""
    private var item : MutableLiveData<Item> = MutableLiveData()
    private var localItem : Item? = null

    fun getItem(): LiveData<Item>{
        val repo = FirebaseRepo.INSTANCE
        repo.getItemRef(itemId).get().addOnCompleteListener{

            if(it.isSuccessful && it.result?.exists()!!){
                item.value = it.result!!.toObject(Item::class.java)
                item.value?.id  = it.result!!.id
            } else {
                item.value = Item()
            }
        }
        return item
    }

    fun getLocalItem() : Item?{
        return localItem
    }

    fun updateLocalItem(newItem : Item){
        localItem = newItem
    }

    fun addItem(context : Context): Task<DocumentReference> {
        val repo = FirebaseRepo.INSTANCE
        localItem?.user = repo.getID(context)

        return repo.insertItem(localItem!!)
    }

    fun updateItem(context : Context): Task<Void> {
        val repo : FirebaseRepo = FirebaseRepo.INSTANCE
        localItem?.user = repo.getID(context)
        return repo.updateItem(itemId, localItem!!)
    }
}

