package it.polito.mad.mhackeroni.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.model.Item
import java.lang.IllegalStateException
import java.lang.ref.WeakReference
import java.util.logging.Level
import java.util.logging.Logger

class FavItemAdapter(private var items: MutableList<Item>, private val listener: MyAdapterListener):
    RecyclerView.Adapter<FavItemAdapter.ViewHolder>() {

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
                .inflate(R.layout.fav_item, parent, false) //get layout inflater and inflate item layout
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

    fun reload(newList : List<Item>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    //class that handles list with items
    class ItemViewHolder(v: View, listener: MyAdapterListener): ViewHolder(v) {
        private val name:TextView = v.findViewById(R.id.item_name)
        private val price:TextView = v.findViewById(R.id.item_price)
        private val listenerRef: WeakReference<MyAdapterListener>? = WeakReference(listener)
        private val image:ImageView = v.findViewById(R.id.drawable_pic)
        private val context = v.context

        override fun bind(item: Item) {
            name.text = item.name
            price.text = item.price.toString() + " €"

            itemView.setOnClickListener {
                listenerRef?.get()?.itemViewOnClick(item)
            }

            if(item.image.isNullOrEmpty()) {
                image.setImageResource(R.drawable.ic_box)
            } else {
                val requestOptions = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)

                try {
                    Glide.with(context.applicationContext)
                        .load(item.image)
                        .apply(requestOptions)
                        .into(image)
                } catch(ex: IllegalStateException) {
                    val logger: Logger = Logger.getLogger(FavItemAdapter::class.java.name)
                    logger.log(Level.WARNING, "context not attached", ex)
                }
            }
        }
    }

    //class that handles empty list
    class EmptyViewHolder(v: View): ViewHolder(v) {
        override fun bind(item: Item) {}
    }

    interface MyAdapterListener {
        fun itemViewOnClick(item: Item) //listener for click on any part of the card view (except edit button)
    }
}