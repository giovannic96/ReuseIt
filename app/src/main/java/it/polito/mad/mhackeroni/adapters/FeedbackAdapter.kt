package it.polito.mad.mhackeroni.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import it.polito.mad.mhackeroni.R

class FeedbackAdapter<T>(context: Context?, textViewResourceId: Int, objects: List<String?>?) :
    ArrayAdapter<String?>(context!!, textViewResourceId, objects!!) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        convertView = inflater.inflate(R.layout.rating_feedback, parent, false)

        val itemName: TextView = convertView!!.findViewById(R.id.itemName)
        var itemComment: TextView = convertView!!.findViewById(R.id.itemComment)
        var star1: ImageView = convertView!!.findViewById(R.id.imageStar1)
        var star2: ImageView = convertView!!.findViewById(R.id.imageStar2)
        var star3: ImageView = convertView!!.findViewById(R.id.imageStar3)
        var star4: ImageView = convertView!!.findViewById(R.id.imageStar4)
        var star5: ImageView = convertView!!.findViewById(R.id.imageStar5)

        var feedback: String = getItem(position)!!

        itemName.text = feedback.substringBefore(':')
        if(feedback.substringAfter("-")=="null"){
            itemComment.visibility = View.GONE
        }
        else{
            itemComment.text =  "\"${feedback.substringAfter("-")}\""
        }
        var numRating: Int = feedback.substringAfter(":").substringBefore('-').toInt()

        when(numRating){
            1 ->{
                star1.background = context.resources.getDrawable(R.drawable.ic_star)
                star2.background = context.resources.getDrawable(R.drawable.ic_emptystar)
                star3.background = context.resources.getDrawable(R.drawable.ic_emptystar)
                star4.background = context.resources.getDrawable(R.drawable.ic_emptystar)
                star5.background = context.resources.getDrawable(R.drawable.ic_emptystar)
            }
            2 ->{
                star1.background = context.resources.getDrawable(R.drawable.ic_star)
                star2.background = context.resources.getDrawable(R.drawable.ic_star)
                star3.background = context.resources.getDrawable(R.drawable.ic_emptystar)
                star4.background = context.resources.getDrawable(R.drawable.ic_emptystar)
                star5.background = context.resources.getDrawable(R.drawable.ic_emptystar)
            }
            3 ->{
                star1.background = context.resources.getDrawable(R.drawable.ic_star)
                star2.background = context.resources.getDrawable(R.drawable.ic_star)
                star3.background = context.resources.getDrawable(R.drawable.ic_star)
                star4.background = context.resources.getDrawable(R.drawable.ic_emptystar)
                star5.background = context.resources.getDrawable(R.drawable.ic_emptystar)
            }
            4 ->{star1.background = context.resources.getDrawable(R.drawable.ic_star)
                star2.background = context.resources.getDrawable(R.drawable.ic_star)
                star3.background = context.resources.getDrawable(R.drawable.ic_star)
                star4.background = context.resources.getDrawable(R.drawable.ic_star)
                star5.background = context.resources.getDrawable(R.drawable.ic_emptystar)

            }
            5 ->{
                star1.background = context.resources.getDrawable(R.drawable.ic_star)
                star2.background = context.resources.getDrawable(R.drawable.ic_star)
                star3.background = context.resources.getDrawable(R.drawable.ic_star)
                star4.background = context.resources.getDrawable(R.drawable.ic_star)
                star5.background = context.resources.getDrawable(R.drawable.ic_star)
            }
        }

        return convertView
    }
}