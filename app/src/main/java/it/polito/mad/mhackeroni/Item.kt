package it.polito.mad.mhackeroni

import android.content.Context
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import java.util.*

class Item(var id: Int, var name:String, var price:Double, var desc:String, var category:String, var subcategory: String, var expiryDate:String, var location:String, var condition: String, var image: String?, var user : String = "null") : Serializable {
    companion object Factory {

        fun fromStringJSON(jsonString: String): Item? {
            var jsonObject: JSONObject? = null
            try {
                jsonObject = JSONObject(jsonString)
            } catch (e: JSONException) {
                e.printStackTrace()
            } finally {
                return if (jsonObject != null)
                    fromJSON(jsonObject)
                else
                    null
            }
        }

        private fun fromJSON(jsonObject: JSONObject): Item {
            return Item(
                jsonObject.getInt("id"),
                jsonObject.getString("name"),
                jsonObject.getDouble("price"),
                jsonObject.getString("desc"),
                jsonObject.getString("category"),
                jsonObject.getString("subcategory"),
                jsonObject.getString("expiryDate"),
                jsonObject.getString("location"),
                jsonObject.getString("condition"),
                jsonObject.getString("image")
            )
        }

        fun toJSON(item: Item): JSONObject {
            val obj: JSONObject = JSONObject()
            try {
                obj.put("id", item.id)
                obj.put("name", item.name)
                obj.put("price", item.price)
                obj.put("desc", item.desc)
                obj.put("category", item.category)
                obj.put("subcategory", item.subcategory)
                obj.put("expiryDate", item.expiryDate)
                obj.put("location", item.location)
                obj.put("condition", item.condition)

                if(item.image.isNullOrEmpty())
                    obj.put("image", "")
                else
                    obj.put("image", item.image)

            } catch (e: JSONException) {
                e.printStackTrace()
            } finally {
                return obj
            }
        }
    }
}