package it.polito.mad.mhackeroni.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.model.Profile

class ListAdapter<T>(
    context: Context?, textViewResourceId: Int,
    objects: List<String?>?
) :
    ArrayAdapter<String?>(context!!, textViewResourceId, objects!!) {
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        convertView = inflater.inflate(R.layout.interested_buyer, parent, false)

        //val image: ImageView = convertView!!.findViewById(R.id.buyer_image)

        val nickname: TextView = convertView!!.findViewById(R.id.buyer_nickname)
        val p = getItem(position)

        nickname.text = p


        return convertView
    }
}