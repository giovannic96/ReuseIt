package it.polito.mad.mhackeroni

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(private val items: Array<Item>):RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item, parent, false) //get layout inflater and inflate item layout
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val name:TextView = v.findViewById(R.id.item_name)
        private val price:TextView = v.findViewById(R.id.item_price)

        fun bind(i:Item) {
            name.text = i.name
            price.text = i.price.toString() + "€"
        }
    }

}