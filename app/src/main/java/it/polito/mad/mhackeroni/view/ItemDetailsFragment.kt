package it.polito.mad.mhackeroni.view

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.view.View.OnTouchListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.model.Profile
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import it.polito.mad.mhackeroni.utilities.ImageUtils
import it.polito.mad.mhackeroni.viewmodel.ItemDetailsFragmentViewModel
import kotlinx.android.synthetic.main.fragment_item_details.*
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.collections.ArrayList

class ItemDetailsFragment: Fragment() {
    var price: Double? = null
    lateinit var vm : ItemDetailsFragmentViewModel
    var item : Item? = Item()
    var canModify : Boolean = true
    private var isOwner = false
    val logger: Logger = Logger.getLogger(ItemDetailsFragment::class.java.name)
    private lateinit var interestedUsers: MutableList<Pair<String, String>>
    private var snackbar : Snackbar? = null

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

        if((FirebaseRepo.INSTANCE.getID(requireContext()) != vm.owner) && !vm.owner.isNullOrEmpty())
            canModify = false

        hide_fab()
        if(!canModify) {
            requireActivity().invalidateOptionsMenu()
            itemState.visibility = View.GONE
        } else {
            hide_fab()
            isOwner = true
        }

        vm.getItem().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            item = it

            checkFavorite(isOwner)

            try {
                if(!it.image.isNullOrEmpty()) {
                    /*
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
                     */
                    val requestOptions = RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)

                    try {
                        Glide.with(requireContext().applicationContext)
                            .load(it.image)
                            .apply(requestOptions)
                            .into(itemImage)
                    } catch(ex: java.lang.IllegalStateException) {
                        logger.log(Level.WARNING, "context not attached", ex)
                    }
                }
            } catch (e: Exception) {
                Snackbar.make(view,R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
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
                Snackbar.make(view,R.string.price_error, Snackbar.LENGTH_SHORT).show()
            }

            val defaultColor:Int;

            when(it.state){
                Item.ItemState.AVAILABLE -> {
                    itemState.text = getString(R.string.stateAvailable)
                    itemState.setBackgroundResource(R.drawable.back_green)
                }
                Item.ItemState.SOLD -> {
                    itemState.text = getString(R.string.stateSold)
                    itemState.setBackgroundResource(R.drawable.back_red)
                }
                Item.ItemState.BLOCKED -> {
                    itemState.text = getString(R.string.stateBlocked)
                    itemState.setBackgroundResource(R.drawable.back_grey)
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

            imageProfileItem.setOnClickListener { listener ->
                val bundle = Bundle()
                bundle.putString(getString(R.string.uid), it.user)
                bundle.putBoolean("fromItem", true)
                view.findNavController()
                    .navigate(R.id.action_nav_ItemDetail_to_nav_showProfile, bundle)
            }

            if(!it.user.isEmpty()) {
                vm.getProfile().removeObservers(viewLifecycleOwner)

                vm.getProfile().observe(viewLifecycleOwner, Observer {
                    itemSeller.text = it.nickname

                    if(!it.image.isNullOrEmpty()) {
                        profile_progress_bar_item.visibility = View.VISIBLE
                        val imagePath: String = it.image!!

                        val ref = Firebase.storage.reference
                            .child("profiles_images")
                            .child(imagePath)

                        ref.downloadUrl.addOnCompleteListener {
                            if(imageProfileItem != null) {
                                if (it.isSuccessful) {
                                    try {
                                        Glide.with(requireContext())
                                            .load(it.result)
                                            .into(imageProfileItem)
                                    } catch (ex: IllegalStateException) {
                                        logger.log(Level.WARNING, "context not attached", ex)
                                    }
                                }
                                profile_progress_bar_item?.visibility = View.INVISIBLE
                            }
                        }
                    }
                })
            }
            else{
                imageProfileItem.setImageResource(R.drawable.ic_avatar)
            }

            if(!it.id.isNullOrEmpty() && canModify){
                vm.getInterestedUsers(it.id).observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                    if(it.isNotEmpty()) {
                        interestedUsers = ArrayList()

                        it.forEach {
                            interestedUsers.add(Pair(it.nickname, it.id))
                        }

                        buyers_listview_label.visibility = View.VISIBLE
                    }

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
                                    hide_fab()
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
                    Snackbar.make(view,R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        buyers_listview_label.setOnClickListener {
            showInterestedDialog()
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

    private fun checkFavorite(isOwner: Boolean) {
        val repo : FirebaseRepo = FirebaseRepo.INSTANCE
        val entry = item
        val uid = repo.getID(requireContext())
        if(entry != null) {
            repo.checkFavorite(uid, entry.id).addOnCompleteListener {
                if(it.isSuccessful){
                    if(it.result?.isEmpty!!) {
                        if(!isOwner)
                            if(fab_buy != null)
                                fab_buy.visibility = View.VISIBLE
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

        val uploadImage : Boolean = arguments?.getBoolean("uploadImage", true) ?: true

        if (!editedItemJSON.isNullOrEmpty()) {
            val oldItem = arguments?.getString("old_item", "")

            if(oldItem.equals(editedItemJSON)){
                handleSelectedItem(editedItemJSON)
            }

            if (oldItem != null) {
                handleEditItem(editedItemJSON, oldItem, uploadImage)
            }
        }

        else if (!selectedItemJSON.isNullOrEmpty()) {
            handleSelectedItem(selectedItemJSON)
        }

        arguments?.clear() // clear arguments
    }

    private fun handleEditItem(editedItemJSON: String, oldItem: String, needUpload : Boolean) {
        snackbar = view?.let { Snackbar.make(it, getString(R.string.item_update), Snackbar.LENGTH_LONG) }

        item = Item.fromStringJSON(oldItem)

        if (snackbar != null) {
            snackbar!!.setAction(getString(R.string.undo), View.OnClickListener {

                val repo : FirebaseRepo = FirebaseRepo.INSTANCE
                val prevItem = Item.fromStringJSON(
                    oldItem
                )!!
                if(view != null){
                    prevItem.user = repo.getID(requireContext())

                    if (prevItem != null) {
                        if(needUpload)
                            FirebaseRepo.INSTANCE.updateItem(prevItem.id, prevItem)
                        else
                            FirebaseRepo.INSTANCE.updateItem(prevItem.id, prevItem, false)
                    }
                }
            })
            snackbar!!.show()
        }
    }


    private fun handleSelectedItem(selectedItemJSON: String) {
        item = selectedItemJSON.let {
            Item.fromStringJSON(
                it
            )
        }
    }

    private fun hide_fab(){
        fab_buy.visibility = View.INVISIBLE
    }

    override fun onDetach() {
        super.onDetach()

        if(snackbar != null)
            snackbar!!.dismiss()
    }


    private fun showInterestedDialog() {
        val dialog = Dialog(requireActivity())

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.interested_dialog_box)

        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        val cancelBtn = dialog.findViewById<Button>(R.id.interested_cancel_btn)

        val interested = dialog.findViewById<ListView>(R.id.interested_listView)

        vm = ViewModelProvider(this).get(ItemDetailsFragmentViewModel::class.java)
        getNavigationInfo()

        if(vm.itemId.isEmpty())
            vm.itemId = item?.id ?: ""

        if((FirebaseRepo.INSTANCE.getID(requireContext()) != vm.owner) && !vm.owner.isNullOrEmpty())
            canModify = false

        hide_fab()
        if(!canModify) {
            requireActivity().invalidateOptionsMenu()
            itemState.visibility = View.GONE
        } else {
            hide_fab()
            isOwner = true
        }

        checkFavorite(isOwner)

        vm.getItem().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            item = it

            if(!it.id.isNullOrEmpty() && canModify) {
                vm.getInterestedUsers(it.id)
                    .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                        if (it.isNotEmpty()) {
                            interestedUsers = ArrayList()

                            it.forEach {
                                interestedUsers.add(Pair(it.nickname, it.id))
                            }

                            val arrayAdapter: ListAdapter<String> = ListAdapter(
                                requireContext(),
                                android.R.layout.simple_list_item_1,
                                interestedUsers.map { it.first}
                            )

                            interested.adapter = arrayAdapter
                            interested.visibility = View.VISIBLE
                        }

                    })
            }


            interested.onItemClickListener = object : AdapterView.OnItemSelectedListener,
                AdapterView.OnItemClickListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                }

                override fun onItemClick(
                    parent: AdapterView<*>?,
                    viewItem: View?,
                    position: Int,
                    id: Long
                ) {
                    // navigate to selected profile
                    val bundle = Bundle()
                    bundle.putString(getString(R.string.uid), interestedUsers[position].second)
                    view?.findNavController()
                        ?.navigate(R.id.action_nav_ItemDetail_to_nav_showProfile, bundle)
                    dialog.dismiss()
                }
            }

            interested.setOnTouchListener(OnTouchListener { v, event ->
                v.parent.requestDisallowInterceptTouchEvent(true)
                false
            })
        })

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
