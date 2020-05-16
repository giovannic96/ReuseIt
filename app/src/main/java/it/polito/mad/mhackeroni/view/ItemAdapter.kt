package it.polito.mad.mhackeroni.view

import android.media.AudioRecord.MetricsConstants.SOURCE
import android.util.Log
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
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.analytics.FirebaseAnalytics.Param.SOURCE
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.model.Item
import java.lang.ref.WeakReference
import java.sql.Time

class ItemAdapter(private var items: MutableList<Item>, private val listener: MyAdapterListener):
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 10
    private val VIEW_TYPE_EMPTY = 11
    var allow_modify = true

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
            holder.bind(items[position], allow_modify)
    }

    abstract class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        abstract fun bind(item: Item, canModify : Boolean)
    }

    // TODO: Delete this
    fun refresh(newItems: MutableList<Item>) {
        items.clear()
        // newItems.sort()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun reload(newList : List<Item>){
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    //class that handles list with items
    class ItemViewHolder(v: View, listener: MyAdapterListener): ViewHolder(v) {
        private val name:TextView = v.findViewById(R.id.item_name)
        private val price:TextView = v.findViewById(R.id.item_price)
        private val editItem: ImageView = v.findViewById(R.id.edit_item)
        private val listenerRef: WeakReference<MyAdapterListener>? = WeakReference(listener)
        private val image:ImageView = v.findViewById(R.id.drawable_pic)
        private val context = v.context
        private val progressBar:ProgressBar = v.findViewById(R.id.item_progressbar)

        override fun bind(item: Item, allow_modify: Boolean) {
            name.text = item.name
            price.text = item.price.toString() + " €"

            if(allow_modify) {
                editItem.setOnClickListener {
                    listenerRef?.get()?.editItemViewOnClick(item)
                }
            } else {
                editItem.visibility = View.GONE
            }

            itemView.setOnClickListener {
                listenerRef?.get()?.itemViewOnClick(item)
            }

            if(item.image.isNullOrEmpty()) {
                image.setImageResource(R.drawable.ic_box)
            }else {
                val requestOptions = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)

                Glide.with(context.applicationContext)
                    .load(item.image)
                    .apply(requestOptions)
                    .into(image)

                /*
                val imageRef = item.image
                val ref = Firebase.storage.reference
                    .child("items_images")
                    .child(imageRef!!)

                progressBar.visibility = View.VISIBLE
                ref.downloadUrl.addOnCompleteListener {
                    if(it.isSuccessful) {
                        Glide.with(context.applicationContext)
                            .load(it.result)
                            .apply(requestOptions)
                            .into(image)
                    }
                    progressBar.visibility = View.INVISIBLE
                }
                 */
            }
        }
    }

    //class that handles empty list
    class EmptyViewHolder(v: View): ViewHolder(v) {
        override fun bind(item: Item, allow_modify: Boolean) {}
    }

    interface MyAdapterListener {
        fun editItemViewOnClick(item: Item) //listener for edit button of the card view
        fun itemViewOnClick(item: Item) //listener for click on any part of the card view (except edit button)
    }


}