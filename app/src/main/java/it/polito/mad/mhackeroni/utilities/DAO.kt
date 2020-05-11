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
            .addOnCompleteListener{
                if (it.isSuccessful) {
                    for (document in it.result!!) {
                        Log.d("MAD2020", document.id + " => " + document.data)

                        items.add(Item(
                            (document.get("id") as Long).toInt(),
                            document.get("name") as String,
                            document.get("price") as Double,
                            document.get("description") as String,
                            document.get("category") as String,
                            document.get("subcategory") as String,
                            (document.get("expire_date") as com.google.firebase.Timestamp).toString(),
                            (document.get("location") as GeoPoint).toString(),
                            document.get("condition") as String,
                            null))

                    }
                } else {
                  throw it.exception!!
                }
            }

            return items
    }
}