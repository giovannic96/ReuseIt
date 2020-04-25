package it.polito.mad.mhackeroni

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.mhackeroni.ItemAdapter.MyAdapterListener

class ItemListFragment: Fragment() {

    private var items: MutableList<Item> = mutableListOf()
    private lateinit var myAdapter:ItemAdapter
    private lateinit var sharedPref: SharedPreferences
    private val storageHelper:StorageHelper = StorageHelper(context)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        sharedPref = requireContext().getSharedPreferences(getString(R.string.shared_pref_list), Context.MODE_PRIVATE)
        val v = inflater.inflate(R.layout.fragment_itemlist, container, false)
        val itemList:RecyclerView = v.findViewById(R.id.item_list)
        items = storageHelper.loadItemList(sharedPref)

        val fab:FloatingActionButton = v.findViewById(R.id.fab)
        fab.setOnClickListener {
            navigateWithoutInfo(R.id.action_nav_itemList_to_nav_ItemDetailEdit)
        }

        myAdapter = ItemAdapter(items, object : MyAdapterListener {
            override fun editItemViewOnClick(item: Item) {
                navigateWithInfo(R.id.action_nav_itemList_to_nav_ItemDetailEdit, item)
            }

            override fun itemViewOnClick(item: Item) {
                navigateWithInfo(R.id.action_nav_itemList_to_nav_ItemDetail, item)
            }
        })
        itemList.adapter = myAdapter
        itemList.layoutManager = if(items.size == 0)
                                    LinearLayoutManager(context)
                                else {
                                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                                        StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
                                    else
                                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                                }
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getResultAndUpdateList()
    }

    override fun onResume() {
        super.onResume()
        myAdapter.refresh(storageHelper.loadItemList(sharedPref));
    }

    private fun navigateWithInfo(layoutId: Int, item: Item) {
        val bundle = Bundle()
        bundle.putString("item", item.let { Item.toJSON(it).toString()})
        view?.findNavController()?.navigate(layoutId, bundle)
    }

    private fun navigateWithoutInfo(layoutId: Int) {
        val bundle = Bundle()
        bundle.putString("item", null)
        view?.findNavController()?.navigate(layoutId, bundle)
    }

    private fun getResultAndUpdateList() {
        val newItemJSON = arguments?.getString("new_item", "")
        if(!newItemJSON.isNullOrEmpty()) {
            insertSingleItem(newItemJSON.let { Item.fromStringJSON(it) }) //update list
        }
        arguments?.clear()
    }

    private fun insertSingleItem(newItem: Item?) {
        if(newItem != null) {
            items.add(0, newItem) //TODO set correct position (maybe sort items by date)
            storageHelper.saveItemList(sharedPref, items)
            myAdapter.notifyItemInserted(0)
        }
    }
}