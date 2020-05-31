package it.polito.mad.mhackeroni.model

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable


class Profile(
    var fullName:String, var nickname:String,
    var email:String, var location:String, var image: String?, var bio:String, var phoneNumber:String, var totRating: Double, var numRating: Double, var id:String = "", var lat: Double? = null, var lng: Double? = null) : Serializable {

    constructor() : this("", "", "", "", "", "", "", 0.0, 0.0)

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
            return Profile(
                jsonObject.getString("fullname"),
                jsonObject.getString("nickname"),
                jsonObject.getString("email"),
                jsonObject.getString("location"),
                jsonObject.getString("image"),
                jsonObject.getString("bio"),
                jsonObject.getString("phoneNumber"),
                jsonObject.getDouble("totRating"),
                jsonObject.getDouble("numRating")
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