package it.polito.mad.mhackeroni.adapters

import android.app.AlertDialog
import android.content.Context
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

        val nickname: TextView = convertView!!.findViewById(R.id.buyer_nickname)
        val sellButton: Button = convertView!!.findViewById(R.id.interested_sellButton)
        val p = getItem(position)
        nickname.text = p

        sellButton.setOnClickListener{
            // Initialize a new instance of
            val builder = AlertDialog.Builder(context)

            // Set the alert dialog title
            builder.setTitle(R.string.sellingTitle)
            val messageDialog = context.resources.getString(R.string.sellingDialog)
            val messageSold = context.resources.getString(R.string.itemSold)

            // Display a message on alert dialog
            builder.setMessage("$messageDialog ${nickname.text}?")

            // Set a positive button and its click listener on alert dialog
            builder.setPositiveButton(R.string.yes){dialog, which ->
                //cancellare le dialog
                //settare venduto all'item
                //settare buyer
                Snackbar.make(convertView, "$messageSold ${nickname.text}! (DA IMPLEMENTARE)", Snackbar.LENGTH_SHORT).show()

            }

            builder.setNegativeButton(R.string.no){dialog,which ->
                //close dialog
            }

            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()

            // Display the alert dialog on app interface
            dialog.show()
        }


        return convertView
    }
}