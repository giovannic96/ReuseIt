package it.polito.mad.mhackeroni

import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.MenuItemCompat
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
    private var searchFilter : ItemFilter = ItemFilter()
    private lateinit var vm : OnSaleListFragmentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_itemlist_sale, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm = ViewModelProvider(this).get(OnSaleListFragmentViewModel::class.java)

        val itemList:RecyclerView = view.findViewById(R.id.item_list_sale)

        val fab:FloatingActionButton = view.findViewById(R.id.fab_sale)
        fab.setOnClickListener {
           //TODO: remove FAB? navigateWithoutInfo(R.id.action_nav_itemListSale_to_nav_ItemDetail)
        }

        myAdapter = ItemAdapter(mutableListOf(), object : MyAdapterListener {

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

        vm.getItems().observe(viewLifecycleOwner, Observer {
            myAdapter.reload(it)
        })


        // getResultAndUpdateList(itemList)
    }

    override fun onResume() {
        super.onResume()
        // myAdapter.refresh(storageHelper.loadItemList(sharedPref));

    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_filter_menu, menu)

        // TODO: Handle search - filters


        val searchItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem.actionView as SearchView
        // searchView.setQueryHint()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                searchFilter.name = query
                updateFilter()
                return false
            }

        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun navigateWithInfo(layoutId: Int, item: Item) {
        val bundle = Bundle()

        // TODO pass whole item or just document ID

        bundle.putString("item", item.let { Item.toJSON(it).toString()})
        bundle.putBoolean("fromList", true)
        view?.findNavController()?.navigate(layoutId, bundle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.menu_filter -> {
                showFilterDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)

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

    private fun updateFilter(){

        vm.getItems().removeObservers(viewLifecycleOwner)
        vm.getItems().observe(viewLifecycleOwner, Observer {

            myAdapter.reload(it.filter { item -> searchFilter.match(item) })
        })
    }

    private fun showFilterDialog() {
        val dialog = Dialog(requireActivity())

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.filter_dialog_box)

        val okBtn = dialog.findViewById<Button>(R.id.filter_ok_btn)
        val cancelBtn = dialog.findViewById<Button>(R.id.filter_cancel_btn)

        okBtn.setOnClickListener {
            dialog.dismiss()

            // updateFilter()
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        // TODO: Handle the selected values - fix the layout

        dialog.show()

    }
}