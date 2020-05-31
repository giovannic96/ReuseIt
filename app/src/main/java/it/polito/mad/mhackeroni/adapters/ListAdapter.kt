package it.polito.mad.mhackeroni.adapters

import android.app.AlertDialog
import android.content.Context
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.view.ItemDetailsFragment
import java.lang.ref.WeakReference

class ListAdapter<T>(context: Context?, textViewResourceId: Int, objects: List<String?>?,
                     private val listener: ListAdapterListener, private val alreadySold: Boolean) :
        ArrayAdapter<String?>(context!!, textViewResourceId, objects!!) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        convertView = if(alreadySold)
            inflater.inflate(R.layout.interested_buyer_sold, parent, false)
        else
            inflater.inflate(R.layout.interested_buyer, parent, false)

        val listenerRef: WeakReference<ListAdapterListener>? = WeakReference(listener)
        val nickname: TextView = convertView!!.findViewById(R.id.buyer_nickname)
        val p = getItem(position)
        nickname.text = p

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