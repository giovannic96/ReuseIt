package it.polito.mad.mhackeroni.adapters

import android.app.AlertDialog
import android.content.Context
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.model.Profile
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import it.polito.mad.mhackeroni.view.ItemDetailsFragment
import kotlinx.android.synthetic.main.fragment_item_details.*
import java.lang.ref.WeakReference

class ListAdapter<T>(context: Context?, textViewResourceId: Int, objects: List<String?>?,
                     private val listener: ListAdapterListener, private val alreadySold: Boolean) :
        ArrayAdapter<String?>(context!!, textViewResourceId, objects!!) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var profile : MutableLiveData<Profile> = MutableLiveData()
        var convertView = convertView
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        convertView = if(alreadySold)
            inflater.inflate(R.layout.interested_buyer_sold, parent, false)
        else
            inflater.inflate(R.layout.interested_buyer, parent, false)

        val listenerRef: WeakReference<ListAdapterListener>? = WeakReference(listener)
        val nickname: TextView = convertView!!.findViewById(R.id.buyer_nickname)
        var totRating: Double = 0.0
        var numRating: Double = 0.0
        var rating: TextView = convertView!!.findViewById(R.id.buyer_rating)

        val repo = FirebaseRepo.INSTANCE

        repo.getProfileRef(getItem(position)!!).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                profile.value = snapshot.toObject(Profile::class.java)
            } else {
                profile.value = Profile()
            }
            nickname.text = profile.value?.nickname
            totRating = profile.value?.totRating!!
            numRating = profile.value?.numRating!!

            var ratingDiv: Double = totRating.div(numRating)

            if(ratingDiv!=0.0) {
                val number3digits: Double = Math.round(ratingDiv * 1000.0) / 1000.0
                val number2digits: Double = Math.round(number3digits * 100.0) / 100.0
                val solution: Double = Math.round(number2digits * 10.0) / 10.0
                rating.text = "${solution.toString()} (${numRating!!.toInt()})"
            }
            else{
                rating.text = "0.0 (0)"
            }
        }



        if(!alreadySold) {
            val sellButton: Button = convertView!!.findViewById(R.id.interested_sellButton)
            sellButton.setOnClickListener {
                // Initialize a new instance of
                val builder = AlertDialog.Builder(context)

                // Set the alert dialog title
                builder.setTitle(R.string.sellingTitle)
                val messageDialog = context.resources.getString(R.string.sellingDialog)
                val messageSold = context.resources.getString(R.string.itemSold)

                // Display a message on alert dialog
                builder.setMessage("$messageDialog ${nickname.text}?")

                // Set a positive button and its click listener on alert dialog
                builder.setPositiveButton(R.string.yes) { dialog, which ->
                    listenerRef?.get()?.sellItemViewOnClick(nickname.text.toString())
                }

                builder.setNegativeButton(R.string.no) { dialog, which ->
                    //close dialog
                }

                // Finally, make the alert dialog using builder
                val dialog: AlertDialog = builder.create()

                // Display the alert dialog on app interface
                dialog.show()
            }
        }
        return convertView
    }

    interface ListAdapterListener {
        fun sellItemViewOnClick(nickname: String) //listener for sell button
    }
}