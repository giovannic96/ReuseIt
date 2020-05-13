package it.polito.mad.mhackeroni.utilities

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import it.polito.mad.mhackeroni.Item
import java.sql.Timestamp

// DAO singleton class
public class DAO private constructor() {
    private var db : FirebaseFirestore = FirebaseFirestore.getInstance()

    private object GetInstance {
        val INSTANCE = DAO()
    }

    companion object {
        val instance: DAO by lazy { GetInstance.INSTANCE }
    }

    // TODO: Using annotation for casting
    public fun getItems() : MutableList<Item> {
        var items: MutableList<Item> = mutableListOf()

        db.collection("items")
            .get()
            .addOnSuccessListener{

                for (document in it.documents) {
                    val item = document.toObject(Item::class.java)

                    if (item != null) {
                        items.add(item)
                    }
                }
            }

            return items
    }

    fun insertItem(item: Item){
        db.collection("items")
            .add(item)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    Log.d("MAG","Add item: ${item.name}")
                } else {
                    throw it.exception!!
                }
            }
    }
}