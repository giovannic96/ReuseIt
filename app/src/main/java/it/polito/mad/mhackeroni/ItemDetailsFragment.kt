package it.polito.mad.mhackeroni

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import it.polito.mad.mhackeroni.utilities.ImageUtils
import kotlinx.android.synthetic.main.fragment_item_details.*


class ItemDetailsFragment: Fragment() {
    var price: Double? = null
    lateinit var vm : ItemDetailsFragmentViewModel
    var item : Item? = Item()
    var canModify : Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_item_details, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm = ViewModelProvider(this).get(ItemDetailsFragmentViewModel::class.java)
        getNavigationInfo()

        if(vm.itemId.isEmpty())
            vm.itemId = item?.id ?: ""

        if(!canModify){
            requireActivity().invalidateOptionsMenu()
            itemState.visibility = View.GONE
        } else {
            fab_buy.visibility = View.INVISIBLE
        }

        checkFavorite()

        vm.getItem().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            item = it

            try {
                if(!it.image.isNullOrEmpty()) {
                    detail_progressbar.visibility = View.VISIBLE

                    val imageRef = it.image
                    val ref = Firebase.storage.reference
                        .child("items_images")
                        .child(imageRef!!)

                    ref.downloadUrl.addOnCompleteListener {
                        if (it.isSuccessful) {
                            Glide.with(requireContext())
                                .load(it.result)
                                .into(itemImage)
                        }
                        detail_progressbar.visibility = View.INVISIBLE
                    }
                }
            } catch (e: Exception) {
                Snackbar.make(view, R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
            }

            itemTitle.text = it.name
            try {
                price = it.price
                if (price == null)
                    itemPrice.text = resources.getString(R.string.defaultPrice)
                else
                    itemPrice.text = "$price"
            }
            catch (e: Exception) {
                Snackbar.make(view, R.string.price_error, Snackbar.LENGTH_SHORT).show()
            }

            when(it.state){
                Item.ItemState.AVAILABLE -> {
                    itemState.text = getString(R.string.stateAvailable)
                }
                Item.ItemState.SOLD -> {
                    itemState.text = getString(R.string.stateSold)
                }
                Item.ItemState.BLOCKED -> {
                    itemState.text = getString(R.string.stateBlocked)
                }
            }
            if (!it.desc.isEmpty()){
                itemDesc.text =it.desc
            }
            else{
                itemDesc.text = resources.getString(R.string.notSpecified)
            }

            if (!it?.category.isNullOrEmpty()){
                itemCategory.text = it.category
            }
            else{
                itemCategory.text = resources.getString(R.string.notSpecified)
            }

            if (!it.subcategory.isEmpty()){
                itemSubCategory.text = it?.subcategory
            }
            else{
                itemSubCategory.text = resources.getString(R.string.notSpecified)
            }

            if (!it.condition.isEmpty()){
                itemCondition.text = it?.condition
            }
            else{
                itemCondition.text = resources.getString(R.string.notSpecified)
            }

            if (!it.expiryDate.isEmpty()){
                itemExpiryDate.text = it.expiryDate
            }
            else{
                itemExpiryDate.text = resources.getString(R.string.notSpecified)
            }

            if (!it.location.isEmpty()){
                itemLocation.text = it.location
            }
            else{
                itemLocation.text = resources.getString(R.string.notSpecified)
            }

            itemSeller.setOnClickListener { listener ->
                val bundle = Bundle()
                bundle.putString(getString(R.string.uid), it.user)
                view.findNavController()
                    .navigate(R.id.action_nav_ItemDetail_to_nav_showProfile, bundle)
            }

            // TODO: Check this lines
            if(!it.user.isEmpty()) {
                vm.getProfile().removeObservers(viewLifecycleOwner)

                vm.getProfile().observe(viewLifecycleOwner, Observer {
                   itemSeller.text = it.nickname
               })
            }
        })

        fab_buy.setOnClickListener {
            val repo : FirebaseRepo = FirebaseRepo.INSTANCE
            val entry = item
            val uid = repo.getID(requireContext())
            if(entry != null) {
                repo.checkFavorite(uid, entry.id).addOnCompleteListener {
                    if(it.isSuccessful){
                        if(it.result?.isEmpty!!){
                            repo.insertFavorite(repo.getID(requireContext()), entry).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    // TODO: Add undo
                                    Snackbar.make(view, getString(R.string.favorite), Snackbar.LENGTH_LONG)
                                        .show()
                                }
                            }
                        } else {
                            Snackbar.make(view, getString(R.string.alreadyFavorite), Snackbar.LENGTH_LONG).show()
                            hide_fab()

                        }
                    }
                }
            }
        }

        itemImage.setOnClickListener {
            val bundle=Bundle()
            if (!item?.image.isNullOrEmpty()) {
                try {
                    if (item?.image?.let { it1 ->
                            ImageUtils.canDisplayBitmap(
                                it1,
                                requireContext())
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
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if(canModify)
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

    private fun checkFavorite(){
        val repo : FirebaseRepo = FirebaseRepo.INSTANCE
        val entry = item
        val uid = repo.getID(requireContext())
        if(entry != null) {
            repo.checkFavorite(uid, entry.id).addOnCompleteListener {
                if(it.isSuccessful){
                    if(!it.result?.isEmpty!!){
                        fab_buy.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun editItem() {
        val bundle = Bundle()
        bundle.putString("item", item?.let { it.let { it1 -> Item.toJSON(it1).toString() } })
        view?.findNavController()?.navigate(R.id.action_nav_ItemDetail_to_nav_ItemDetailEdit, bundle)
    }

    private fun getNavigationInfo() {

        canModify = arguments?.getBoolean("allowModify", true) ?: true

        //get item derived from edit fragment (editedItem)
        val editedItemJSON = arguments?.getString("new_item", "")

        //get item derived from list fragment (selectedItem)
        val selectedItemJSON = arguments?.getString("item", "")

        if (!editedItemJSON.isNullOrEmpty()) {
            val oldItem = arguments?.getString("old_item", "")

            if(oldItem.equals(editedItemJSON)){
                handleSelectedItem(editedItemJSON)
            }

            if (oldItem != null) {
                handleEditItem(editedItemJSON, oldItem)
            }
        }

        else if (!selectedItemJSON.isNullOrEmpty()) {
            handleSelectedItem(selectedItemJSON)
        }

        arguments?.clear() // clear arguments
    }

    private fun handleEditItem(editedItemJSON: String, oldItem: String) {
        val snackbar = view?.let { Snackbar.make(it, getString(R.string.undo), Snackbar.LENGTH_LONG) }

        item = Item.fromStringJSON(oldItem)

        if (snackbar != null) {
            snackbar.setAction(getString(R.string.undo), View.OnClickListener {

                val repo : FirebaseRepo = FirebaseRepo.INSTANCE
                val prevItem = Item.fromStringJSON(oldItem)!!
                prevItem.user = repo.getID(requireContext())

                if(prevItem != null)
                    FirebaseRepo.INSTANCE.updateItem(prevItem.id, prevItem)

                })
            snackbar.show()
        }
    }


    private fun handleSelectedItem(selectedItemJSON: String) {
      item = selectedItemJSON.let { Item.fromStringJSON(it) }
    }

    private fun hide_fab(){
       fab_buy.visibility = View.INVISIBLE
    }
}