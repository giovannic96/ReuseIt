package it.polito.mad.mhackeroni

import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

class Item(val name:String, val price:Double, val desc:String, val category:String, val expiryDate:String, val location:String, var condition: String, var image: String?) : Serializable, Comparable<Item> {

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
                jsonObject.getDouble("price"),
                jsonObject.getString("desc"),
                jsonObject.getString("category"),
                jsonObject.getString("expiryDate"), //TODO CHANGE INTO DATE
                jsonObject.getString("location"),
                jsonObject.getString("condition"),
                jsonObject.getString("image")
            )
        }

        fun toJSON(item: Item): JSONObject {
            val obj: JSONObject = JSONObject()
            try {
                obj.put("name", item.name)
                obj.put("price", item.price)
                obj.put("desc", item.desc)
                obj.put("category", item.category)
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

    override fun compareTo(other: Item): Int = this.expiryDate.compareTo(other.expiryDate)
    override fun toString(): String = "$expiryDate"

}