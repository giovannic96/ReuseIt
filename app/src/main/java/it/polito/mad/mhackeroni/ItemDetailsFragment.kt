package it.polito.mad.mhackeroni

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.mhackeroni.utilities.ImageUtils
import it.polito.mad.mhackeroni.utilities.StorageHelper
import kotlinx.android.synthetic.main.fragment_item_details.*

class ItemDetailsFragment: Fragment() {
    var item: MutableLiveData<Item> = MutableLiveData()
    var price: Double? = null
    var cat: String = ""
    var cond: String = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_item_details, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        item.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            try {
                if(!item?.value?.image.isNullOrEmpty())
                    itemImage.setImageBitmap(item?.value?.image?.let { it1 ->
                        ImageUtils.getBitmap(it1, requireContext())
                })
            } catch (e: Exception) {
                Snackbar.make(view, R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
            }

            itemTitle.text = item?.value?.name ?: resources.getString(R.string.defaultTitle)

            try {
                price = item?.value?.price;
                if (price == null)
                    itemPrice.text = resources.getString(R.string.defaultPrice)
                else
                    itemPrice.text = "$price"
            }
            catch (e: Exception) {
                Snackbar.make(view, R.string.price_error, Snackbar.LENGTH_SHORT).show()
            }

            if (!item?.value?.desc.isNullOrEmpty()){
                itemDesc.text = item?.value?.desc
            }
            else{
                itemDesc.text = resources.getString(R.string.notSpecified)
            }

            if (!item?.value?.category.isNullOrEmpty()){
                itemCategory.text = item?.value?.category
            }
            else{
                itemCategory.text = resources.getString(R.string.notSpecified)
            }

            if (!item?.value?.subcategory.isNullOrEmpty()){
                itemSubCategory.text = item?.value?.subcategory
            }
            else{
                itemSubCategory.text = resources.getString(R.string.notSpecified)
            }

            if (!item?.value?.condition.isNullOrEmpty()){
                itemCondition.text = item?.value?.condition
            }
            else{
                itemCondition.text = resources.getString(R.string.notSpecified)
            }

            if (!item?.value?.expiryDate.isNullOrEmpty()){
                itemExpiryDate.text = item?.value?.expiryDate
            }
            else{
                itemExpiryDate.text = resources.getString(R.string.notSpecified)
            }

            if (!item?.value?.location.isNullOrEmpty()){
                itemLocation.text = item?.value?.location
            }
            else{
                itemLocation.text = resources.getString(R.string.notSpecified)
            }

        })

        getResult(view)

        itemImage.setOnClickListener {
            val bundle=Bundle()
            if (!item?.value?.image.isNullOrEmpty()) {
                try {
                    if (item?.value?.image?.let { it1 ->
                            ImageUtils.canDisplayBitmap(
                                it1,
                                requireContext())
                        }!!) {

                        bundle.putString("uri", item?.value?.image.toString())
                        view.findNavController()
                            .navigate(R.id.action_nav_ItemDetail_to_nav_showImage, bundle)
                    }
                } catch (e: Exception) {
                    Snackbar.make(view, R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item selection
        return when (item.itemId) {
            R.id.menu_edit -> {
                editItem()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editItem() {
        val bundle = Bundle()
        bundle.putString("item", item?.let { it.value?.let { it1 -> Item.toJSON(it1).toString() } })
        view?.findNavController()?.navigate(R.id.action_nav_ItemDetail_to_nav_ItemDetailEdit, bundle)
    }

    private fun getResult(view:View) {
        //get item derived from edit fragment (editedItem)
        val editedItemJSON = arguments?.getString("new_item", "")

        //get item derived from list fragment (selectedItem)
        val selectedItemJSON = arguments?.getString("item", "")

        //EDITED ITEM
        if (!editedItemJSON.isNullOrEmpty()) {
            val oldItem = arguments?.getString("old_item", "")

            if(oldItem.equals(editedItemJSON)){
                handleSelectedItem(editedItemJSON, view)
            }

            if (oldItem != null) {
                handleEditItem(editedItemJSON, oldItem)
            }
        }
        //SELECTED ITEM
        else if (!selectedItemJSON.isNullOrEmpty()) {
            handleSelectedItem(selectedItemJSON, view)
        }

        arguments?.clear() // clear arguments
    }

    private fun handleEditItem(editedItemJSON: String, oldItem: String) {
        val storageHelper =
            StorageHelper(requireContext())
        val sharedPref:SharedPreferences = requireContext()
            .getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE)

        if(editedItemJSON != oldItem) {

            item?.value = editedItemJSON.let { Item.fromStringJSON(it) }


            item.value?.let { storageHelper.editItem(sharedPref, it) }

            val snackbar = view?.let { Snackbar.make(it, getString(R.string.item_update), Snackbar.LENGTH_LONG) }
            if (snackbar != null) {
                snackbar.setAction(getString(R.string.undo), View.OnClickListener {
                    item.value = Item.fromStringJSON(oldItem)

                    if(item.value?.image.isNullOrEmpty()) {
                        itemImage.setImageResource(R.drawable.ic_itemimage)
                    }
                    item.value?.let { storageHelper.editItem(sharedPref, it) }

                })
                snackbar.show()
            }
        }
    }


    private fun handleSelectedItem(selectedItemJSON: String, view:View) {
        item?.value = selectedItemJSON.let { Item.fromStringJSON(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("item", item?.let { Item.toJSON(item?.value!!).toString() })

    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            val savedItem  = savedInstanceState.getString("item")?.let { Item.fromStringJSON(it) }

            if(savedItem != null)
                item.value = savedItem
        }
    }
}