package it.polito.mad.mhackeroni

import android.R
import android.widget.TextView
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import java.util.*


class Profile(var fullName:String, var nickname:String,
              var email:String, var location:String, var image: String?, var bio:String, var phoneNumber:String) : Serializable {

    companion object Factory {

        fun fromStringJSON(jsonString:String) : Profile? {
            var jsonObject:JSONObject? = null
            try {
                jsonObject = JSONObject(jsonString)
            } catch (e:JSONException){
                e.printStackTrace()
            } finally {
                return if(jsonObject != null)
                    fromJSON(jsonObject)
                else
                    null
            }
        }

        private fun fromJSON(jsonObject : JSONObject ) : Profile {
            return Profile(jsonObject.getString("fullname"),
                jsonObject.getString("nickname"),
                jsonObject.getString("email"),
                jsonObject.getString("location"),
                jsonObject.getString("image"),
                jsonObject.getString("bio"),
                jsonObject.getString("phoneNumber")
            )
        }

        fun toJSON(profile:Profile) : JSONObject{
            val obj:JSONObject = JSONObject()
            try {
                obj.put("fullname", profile.fullName)
                obj.put("nickname", profile.nickname)
                obj.put("email", profile.email)
                obj.put("location", profile.location)
                obj.put("bio", profile.bio)
                obj.put("phoneNumber", profile.phoneNumber)

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