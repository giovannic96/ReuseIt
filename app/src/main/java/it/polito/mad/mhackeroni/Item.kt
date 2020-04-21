package it.polito.mad.mhackeroni

import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

class Item(val name:String, val price:Double) : Serializable {

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
                jsonObject.getString("name"),
                jsonObject.getDouble("price")
            )
        }

        fun toJSON(item: Item): JSONObject {
            val obj: JSONObject = JSONObject()
            try {
                obj.put("name", item.name)
                obj.put("price", item.price)
            } catch (e: JSONException) {
                e.printStackTrace()
            } finally {
                return obj
            }
        }
    }
}