package it.polito.mad.mhackeroni.model

import android.util.Log
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType


class Profile(
    var fullName:String, var nickname:String,
    var email:String, var location:String, var image: String?, var bio:String, var phoneNumber:String, var totRating: Double, var numRating: Double, var feedbacks: ArrayList<String>, var id:String = "", var lat: Double? = null, var lng: Double? = null) : Serializable {

    constructor() : this("", "", "", "", "", "", "", 0.0, 0.0, ArrayList<String>())

    companion object Factory {

        fun fromStringJSON(jsonString:String) : Profile? {
            var jsonObject:JSONObject? = null
            try {
                jsonObject = JSONObject(jsonString)
            } catch (e:JSONException){
                e.printStackTrace()
            } finally {
                return if(jsonObject != null)
                    fromJSON(
                        jsonObject
                    )
                else
                    null
            }
        }

        private fun fromJSON(jsonObject : JSONObject ) : Profile {
            val arrJson = jsonObject.getJSONArray("feedbacks")
            val feedbacks = ArrayList<String>()

            if(arrJson.length() != 0) {
                for (i in 0 until arrJson.length())
                    feedbacks.add(arrJson.getString(i))
            }

            return Profile(
                jsonObject.getString("fullname"),
                jsonObject.getString("nickname"),
                jsonObject.getString("email"),
                jsonObject.getString("location"),
                jsonObject.getString("image"),
                jsonObject.getString("bio"),
                jsonObject.getString("phoneNumber"),
                jsonObject.getDouble("totRating"),
                jsonObject.getDouble("numRating"),
                feedbacks
                )
        }

        fun toJSON(profile: Profile) : JSONObject{
            val obj:JSONObject = JSONObject()

            try {
                obj.put("fullname", profile.fullName)
                obj.put("nickname", profile.nickname)
                obj.put("email", profile.email)
                obj.put("location", profile.location)
                obj.put("bio", profile.bio)
                obj.put("phoneNumber", profile.phoneNumber)
                obj.put("totRating", profile.totRating)
                obj.put("numRating", profile.totRating)
                val jsonArray = JSONArray(profile.feedbacks)
                obj.put("feedbacks", jsonArray as Object)

                if(profile.image.isNullOrEmpty())
                    obj.put("image", "")
                else
                    obj.put("image", profile.image)
            } catch(e: JSONException) {
                e.printStackTrace()
            } finally {
                return obj
            }
        }
    }
}