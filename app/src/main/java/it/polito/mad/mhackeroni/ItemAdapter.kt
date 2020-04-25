package it.polito.mad.mhackeroni

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

class ItemAdapter(private var items: MutableList<Item>, private val listener: MyAdapterListener):
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    init {
        getItemViewType(0)
    }

    private val VIEW_TYPE_ITEM = 10
    private val VIEW_TYPE_EMPTY = 11

    override fun getItemCount(): Int {
        return if(items.size == 0) 1 else items.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (items.size == 0) VIEW_TYPE_EMPTY else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v:View

        //return view with items
        return if (viewType == VIEW_TYPE_ITEM) {
            v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item, parent, false) //get layout inflater and inflate item layout
            ItemViewHolder(v, listener)
        }
        else { //return view with empty list
            v = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_empty, parent, false);
            EmptyViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        if (viewType == VIEW_TYPE_ITEM) //bind item only if list is not empty
            holder.bind(items[position])
    }

    abstract class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        abstract fun bind(item: Item)
    }

    fun refresh(newItems: MutableList<Item>) {
        items = newItems
        notifyDataSetChanged()
    }

    //class that handles list with items
    class ItemViewHolder(v: View, listener: MyAdapterListener): ViewHolder(v) {
        private val name:TextView = v.findViewById(R.id.item_name)
        private val price:TextView = v.findViewById(R.id.item_price)
        private val editItem: ImageView = v.findViewById(R.id.edit_item)
        private val listenerRef: WeakReference<MyAdapterListener>? = WeakReference(listener)

        override fun bind(item:Item) {
            name.text = item.name
            price.text = item.price.toString() + " €"

            editItem.setOnClickListener {
                listenerRef?.get()?.editItemViewOnClick(item)
            }
            itemView.setOnClickListener {
                listenerRef?.get()?.itemViewOnClick(item)
            }
        }
    }

    //class that handles empty list
    class EmptyViewHolder(v: View): ViewHolder(v) {
        override fun bind(item: Item) {}
    }

    interface MyAdapterListener {
        fun editItemViewOnClick(item: Item) //listener for edit button of the card view
        fun itemViewOnClick(item: Item) //listener for click on any part of the card view (except edit button)
    }

}