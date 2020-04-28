package it.polito.mad.mhackeroni

import android.content.Context
import android.content.SharedPreferences

object IDGenerator {
    lateinit var sharedPref: SharedPreferences

    fun getNextID(context: Context): Int{
        sharedPref = context.getSharedPreferences(context.getString(R.string.shared_pref), Context.MODE_PRIVATE)
        var count = sharedPref.getInt("next_id", 0) //0 is default value.
        count++;

        sharedPref.edit().putInt("next_id", count).apply()

        return count
    }
}