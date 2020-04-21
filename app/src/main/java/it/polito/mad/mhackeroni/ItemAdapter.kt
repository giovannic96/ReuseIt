package it.polito.mad.mhackeroni

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.lang.ref.WeakReference

class ItemAdapter(private val items: MutableList<Item>, private val listener: MyAdapterListener):
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item, parent, false) //get layout inflater and inflate item layout
        return ViewHolder(v, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])

    }

    class ViewHolder(v: View, listener: MyAdapterListener): RecyclerView.ViewHolder(v) {
        private val name:TextView = v.findViewById(R.id.item_name)
        private val price:TextView = v.findViewById(R.id.item_price)
        private val editItem: MaterialButton = v.findViewById(R.id.edit_item)
        private val listenerRef: WeakReference<MyAdapterListener>? = WeakReference(listener)

        fun bind(i:Item) {
            name.text = i.name
            price.text = i.price.toString() + "€"

            editItem.setOnClickListener {
                listenerRef?.get()?.editItemViewOnClick(i)
            }
        }
    }

    interface MyAdapterListener {
        fun editItemViewOnClick(item: Item)
        //fun iconImageViewOnClick(v: View?, position: Int)
    }

}