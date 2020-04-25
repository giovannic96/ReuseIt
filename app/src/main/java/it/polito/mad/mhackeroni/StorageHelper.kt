package it.polito.mad.mhackeroni

import android.content.Context
import android.content.SharedPreferences
import com.beust.klaxon.Klaxon

class StorageHelper(context: Context?) {

    private val myContext:Context? = context

    fun saveItemList(s: SharedPreferences, l:MutableList<Item>) {
        with (s.edit()) {
            putString(myContext?.getString(R.string.itemList_sharedPref), Klaxon().toJsonString(l))
            apply()
        }
    }

    fun loadItemList(s: SharedPreferences):MutableList<Item> {
        var jSONString: String? = s.getString(myContext?.getString(R.string.itemList_sharedPref), "")
        if(!jSONString.isNullOrEmpty()) {
            if(jSONString[0] != '[')
                jSONString = "[${jSONString}]" //convert to json array
            val items = Klaxon().parseArray<Item>(jSONString)?.toMutableList()
            if (items != null) {
                return items
            }
        }
        return mutableListOf()
    }

    fun saveItem(s: SharedPreferences, i:Item) {
        with (s.edit()) {
            putString(myContext?.getString(R.string.item_sharedPref), Item.toJSON(i).toString())
            apply()
        }
    }

    fun loadItem(s: SharedPreferences):Item? {
        val jSONString : String? = s.getString(myContext?.getString(R.string.item_sharedPref), "")
        return jSONString?.let { Item.fromStringJSON(it) }
    }

    fun saveProfile(s: SharedPreferences, p:Profile) {
        with (s.edit()) {
            putString(myContext?.getString(R.string.profile_sharedPref), Profile.toJSON(p).toString())
            apply()
        }
    }

    fun loadProfile(s: SharedPreferences):Profile? {
        val jSONString : String? = s.getString(myContext?.getString(R.string.profile_sharedPref), "")
        return jSONString?.let { Profile.fromStringJSON(it) }
    }
}