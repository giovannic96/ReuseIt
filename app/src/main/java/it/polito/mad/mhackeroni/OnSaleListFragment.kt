package it.polito.mad.mhackeroni

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.mhackeroni.ItemAdapter.MyAdapterListener

import it.polito.mad.mhackeroni.utilities.StorageHelper

class OnSaleListFragment: Fragment() {

    private var items: MutableList<Item> = mutableListOf()
    private lateinit var myAdapter:ItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_itemlist_sale, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vm : OnSaleListFragmentViewModel = ViewModelProvider(this).get(OnSaleListFragmentViewModel::class.java)
        
        vm.getItems().observe(viewLifecycleOwner, Observer {
            myAdapter.reload(it)
        })

        val itemList:RecyclerView = view.findViewById(R.id.item_list_sale)

        val fab:FloatingActionButton = view.findViewById(R.id.fab_sale)
        fab.setOnClickListener {
            navigateWithoutInfo(R.id.action_nav_itemList_to_nav_ItemDetailEdit)
        }

        myAdapter = ItemAdapter(items, object : MyAdapterListener {

            override fun editItemViewOnClick(item: Item) {
                // TODO: REMOVE ALL EDIT REFERENCES
                // navigateWithInfo(R.id.action_nav_itemList_to_nav_ItemDetailEdit, item)
            }

            override fun itemViewOnClick(item: Item) {
                navigateWithInfo(R.id.action_nav_itemListSale_to_nav_ItemDetail, item)
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

        getResultAndUpdateList(itemList)
    }

    override fun onResume() {
        super.onResume()
        // myAdapter.refresh(storageHelper.loadItemList(sharedPref));

    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_filter_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

        // TODO: Handle search - filters
    }

    private fun navigateWithInfo(layoutId: Int, item: Item) {
        val bundle = Bundle()

        // TODO pass whole item or just document ID

        bundle.putString("item", item.let { Item.toJSON(it).toString()})
        bundle.putBoolean("fromList", true)
        view?.findNavController()?.navigate(layoutId, bundle)
    }

    private fun navigateWithoutInfo(layoutId: Int) {
        val bundle = Bundle()
        bundle.putString("item", null)
        view?.findNavController()?.navigate(layoutId, bundle)
    }

    private fun getResultAndUpdateList(recyclerView: RecyclerView) {

        // TODO: Check this

        val newItemJSON = arguments?.getString("new_item", "")
        if(!newItemJSON.isNullOrEmpty()) {
            insertSingleItem(newItemJSON.let { Item.fromStringJSON(it) }) //update list
            recyclerView.layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
                else
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        } else {
            val editedItemJSON = arguments?.getString("edited_item")
            val oldItemJSON = arguments?.getString("old_item")

            if (editedItemJSON != null && oldItemJSON != null) {
                handleEditItem(editedItemJSON, oldItemJSON)
            }

        }

        arguments?.clear()
    }

    private fun handleEditItem(editedItemJSON: String, oldItem: String) {

        // TODO: not necessary
        val storageHelper =
            StorageHelper(requireContext())
        val sharedPref:SharedPreferences = requireContext()
            .getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE)

        if(editedItemJSON != oldItem) {

            val item = editedItemJSON.let { Item.fromStringJSON(it) }

            if (item != null) {
                storageHelper.editItem(sharedPref, item)
                val snackbar = view?.let { Snackbar.make(it, getString(R.string.item_update), Snackbar.LENGTH_LONG) }

                if (snackbar != null) {
                    snackbar.setAction(getString(R.string.undo), View.OnClickListener {

                        Item.fromStringJSON(oldItem)?.let { it1 ->
                            storageHelper.editItem(sharedPref,
                                it1
                            )
                        }

                        items = storageHelper.loadItemList(sharedPref)
                        myAdapter.refresh(items)

                    })
                    snackbar.show()
                }
            }
        }
    }

    private fun insertSingleItem(newItem: Item?) {
        if(newItem != null) {
            items.add(0, newItem) //TODO set correct position (maybe sort items by date)
            // storageHelper.saveItemList(sharedPref, items)
            myAdapter.notifyItemInserted(0)
        }
    }
}