package it.polito.mad.mhackeroni

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_item_details.*

class ItemDetailsFragment: Fragment() {
    var item: Item? = null
    var price: Double? = null
    var cat: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_item_details, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        item = getResult(view)

        itemImage.setOnClickListener {
            val bundle=Bundle()
            try {
                if (item?.image?.let { it1 ->
                        ImageUtils.canDisplayBitmap(
                            it1,
                            requireContext()
                        )
                    }!!) {

                    bundle.putString("uri", item?.image.toString())
                    view.findNavController()
                        .navigate(R.id.action_nav_ItemDetail_to_nav_showImage, bundle)
                }
            } catch (e: Exception) {
                Snackbar.make(view, R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
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
        bundle.putString("item", item?.let { Item.toJSON(it).toString()})
        view?.findNavController()?.navigate(R.id.action_nav_ItemDetail_to_nav_ItemDetailEdit, bundle)
    }

    private fun getResult(view:View): Item? {
        //get item derived from edit fragment (editedItem)
        val editedItemJSON = arguments?.getString("new_item", "")

        //get item derived from list fragment (selectedItem)
        val selectedItemJSON = arguments?.getString("item", "")

        //EDITED ITEM
        return if(!editedItemJSON.isNullOrEmpty()) {
            handleEditItem(editedItemJSON)
            arguments?.clear() // clear arguments
            Item.fromStringJSON(editedItemJSON)
        }
        //SELECTED ITEM
        else if(!selectedItemJSON.isNullOrEmpty()) {
            handleSelectedItem(selectedItemJSON, view)
            arguments?.clear() // clear arguments
            Item.fromStringJSON(selectedItemJSON)
        } else
            null
    }

    private fun handleEditItem(editedItemJSON: String) {
        val oldItem = item
        if(editedItemJSON != oldItem?.let { Item.toJSON(it).toString() }) {
            item = editedItemJSON.let { Item.fromStringJSON(it) }

            val snackbar = view?.let { Snackbar.make(it, getString(R.string.item_update), Snackbar.LENGTH_LONG) }
            if (snackbar != null) {
                snackbar.setAction(getString(R.string.undo), View.OnClickListener {
                    item = oldItem
                })
                snackbar.show()
            }
        }
    }

    private fun handleSelectedItem(selectedItemJSON: String, view:View) {
        item = selectedItemJSON.let { Item.fromStringJSON(it) }
        updateItemView(item, view)
    }

    private fun updateItemView(item:Item?, view:View) {
        try {
            itemImage.setImageBitmap(item?.image?.let { it1 ->
                ImageUtils.getBitmap(it1, requireContext())
            })
        } catch (e: Exception) {
            Snackbar.make(view, R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
        }

        itemTitle.text = item?.name ?: resources.getString(R.string.defaultTitle)

        try {
            price = item?.price;
            if (price == null)
                itemPrice.text = resources.getString(R.string.defaultPrice)
            else
                itemPrice.text = "$price"
        }
        catch (e: Exception) {
            Snackbar.make(view, R.string.price_error, Snackbar.LENGTH_SHORT).show()
        }

        itemDesc.text = item?.desc ?: resources.getString(R.string.defaultDesc)
        cat = item?.category ?: resources.getString(R.string.defaultCategory)
        itemCategory.text = "$cat:"
        itemSubCategory.text = item?.subcategory ?: resources.getString(R.string.defaultSubCategory)
        itemExpiryDate.text = item?.expiryDate ?: resources.getString(R.string.defaultExpire)
        itemLocation.text = item?.location ?: resources.getString(R.string.defaultLocation)
        var cond = item?.condition ?: resources.getString(R.string.defaultCondition)
        var defCond = resources.getString(R.string.defaultCondition)
        itemCondition.text = "$defCond: $cond"
    }

    //TODO
    /*override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val price:Double = if(itemPrice.text.toString().isEmpty())
            0.0
        else
            itemPrice.text.toString().toDouble()

        item.value = Item(itemTitle.text.toString(), price,
                        itemDesc.text.toString(), cat, itemSubCategory.text.toString(),
                        itemExpiryDate.text.toString(), itemLocation.text.toString(),
                        itemCondition.text.toString(), item?.image
                    )


        outState.putString("item", item.value?.let { Item.toJSON(it).toString() })
    }*/
}