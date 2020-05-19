package it.polito.mad.mhackeroni.view


import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.view.ItemAdapter.MyAdapterListener
import it.polito.mad.mhackeroni.viewmodel.ItemListFragmentViewModel
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import kotlinx.android.synthetic.main.fragment_itemlist.*


class ItemListFragment: Fragment() {

    private lateinit var myAdapter: ItemAdapter
    private lateinit var vm : ItemListFragmentViewModel
    private lateinit var itemList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_itemlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm = ViewModelProvider(this).get(ItemListFragmentViewModel::class.java)
        val repo : FirebaseRepo = FirebaseRepo.INSTANCE
        vm.uid = repo.getID(requireContext())
        getNavigationDetails()

        itemList = view.findViewById(R.id.item_list)

        myAdapter = ItemAdapter(
            mutableListOf(),
            object : MyAdapterListener {

                override fun editItemViewOnClick(item: Item) {
                    navigateWithInfo(
                        R.id.action_nav_itemList_to_nav_ItemDetailEdit,
                        item
                    )
                }

                override fun itemViewOnClick(item: Item) {
                    navigateWithInfo(
                        R.id.action_nav_itemList_to_nav_ItemDetail,
                        item
                    )
                }
            })
        fab.setOnClickListener {
            navigateWithoutInfo()
        }

        myAdapter.allow_modify = true
        itemList.adapter = myAdapter
        itemList.layoutManager = LinearLayoutManager(context)

        vm.getItems().observe(viewLifecycleOwner, Observer {
            myAdapter.reload(it)
            updateLayoutManager(it)
        })
    }

    private fun updateLayoutManager(list: List<Item>) {
        itemList.layoutManager = if(list.isEmpty())
            LinearLayoutManager(context)
        else {
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
            else
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    private fun navigateWithInfo(layoutId: Int, item: Item) {
        val bundle = Bundle()
        bundle.putString("item", item.let { Item.toJSON(it).toString()})
        bundle.putBoolean("fromList", true)
        view?.findNavController()?.navigate(layoutId, bundle)
    }

    private fun navigateWithoutInfo() {
        val bundle = Bundle()
        bundle.putString("item", null)
        view?.findNavController()?.navigate(R.id.action_nav_itemList_to_nav_ItemDetailEdit, bundle)
    }

    private fun getNavigationDetails() {
        //get item derived from edit fragment (editedItem)
        val editedItemJSON = arguments?.getString("edited_item", "") ?: ""
        val oldItem = arguments?.getString("old_item", "") ?: ""
        val uploadImage : Boolean = arguments?.getBoolean("uploadImage", true) ?: true

        arguments?.clear()

        if(!editedItemJSON.isEmpty() && !oldItem.isEmpty() && oldItem != editedItemJSON){
            val snackbar = view?.let { Snackbar.make(it, getString(R.string.undo), Snackbar.LENGTH_LONG) }
            if (snackbar != null) {
                snackbar.setAction(getString(R.string.undo), View.OnClickListener {
                    val repo : FirebaseRepo = FirebaseRepo.INSTANCE
                    val prevItem = Item.fromStringJSON(
                        oldItem
                    )!!
                    if(view != null){
                        prevItem.user = repo.getID(requireContext())

                        if (prevItem != null) {
                            if(uploadImage)
                                FirebaseRepo.INSTANCE.updateItem(prevItem.id, prevItem)
                            else
                                FirebaseRepo.INSTANCE.updateItem(prevItem.id, prevItem, false)
                        }
                    }
                })
                snackbar.show()
            }
        }
    }
}