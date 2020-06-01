package it.polito.mad.mhackeroni.view

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.View.OnTouchListener
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.adapters.ListAdapter
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import it.polito.mad.mhackeroni.utilities.ImageUtils
import it.polito.mad.mhackeroni.viewmodel.ItemDetailsFragmentViewModel
import kotlinx.android.synthetic.main.fragment_item_details.*
import kotlinx.android.synthetic.main.fragment_item_details.imageStar1
import kotlinx.android.synthetic.main.fragment_item_details.imageStar2
import kotlinx.android.synthetic.main.fragment_item_details.imageStar3
import kotlinx.android.synthetic.main.fragment_item_details.imageStar4
import kotlinx.android.synthetic.main.fragment_item_details.imageStar5
import kotlinx.android.synthetic.main.fragment_item_details.rating
import kotlinx.android.synthetic.main.fragment_show_profile.*
import java.util.logging.Level
import java.util.logging.Logger


class ItemDetailsFragment: Fragment(), OnMapReadyCallback {
    var price: Double? = null
    lateinit var vm : ItemDetailsFragmentViewModel
    var item : Item? = Item()
    var canModify : Boolean = true
    private var isOwner = false
    val logger: Logger = Logger.getLogger(ItemDetailsFragment::class.java.name)
    private lateinit var interestedUsers: MutableList<Pair<String, String>>
    private var snackbar : Snackbar? = null
    private var googleMap : GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_item_details, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm = ViewModelProvider(this).get(ItemDetailsFragmentViewModel::class.java)
        getNavigationInfo()

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.item_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        if(vm.itemId.isEmpty())
            vm.itemId = item?.id ?: ""

        if((FirebaseRepo.INSTANCE.getID(requireContext()) != vm.owner) && !vm.owner.isNullOrEmpty())
            canModify = false

        //hide_fab()
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

            when(it.state) {
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

            if(it.lat != null && it.lng != null){
                val pos = LatLng(it.lat!!, it.lng!!)
                var pPos: LatLng? = null
                var image : Bitmap? = null

                vm.getLoggedProfile(requireContext()).get().addOnCompleteListener {
                    if(it.isSuccessful && it.result?.exists()!!){
                        val loggedProfile = it.result
                        val pLat = loggedProfile?.getDouble("lat")
                        val pLng = loggedProfile?.getDouble("lng")

                        if(pLat != null && pLng != null){
                            pPos = LatLng(pLat, pLng)
                        }
                    }

                    if(pPos != null && !canModify){

                        googleMap!!.addMarker(
                            MarkerOptions()
                                .position(pPos!!)
                                .title(getString(R.string.yourPosition))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        )

                        googleMap!!.addPolyline(
                            PolylineOptions().add(
                                pPos,
                                pos
                            ).width(2F).color(Color.BLUE).geodesic(true)
                        )
                    }
                }

                if(googleMap != null) {
                    googleMap!!.addMarker(
                        MarkerOptions()
                            .position(pos)
                            .title(getString(R.string.itemPosition))
                    )

                    googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(pos))
                }

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

                    if(it.numRating == 0.0 || it.totRating == 0.0){
                        imageStar1.background = resources.getDrawable(R.drawable.ic_emptystar)
                        imageStar2.background = resources.getDrawable(R.drawable.ic_emptystar)
                        imageStar3.background = resources.getDrawable(R.drawable.ic_emptystar)
                        imageStar4.background = resources.getDrawable(R.drawable.ic_emptystar)
                        imageStar5.background = resources.getDrawable(R.drawable.ic_emptystar)
                        rating.text = "(0)"
                    }
                    else{
                        var ratingDiv: Double = it.totRating?.div(it.numRating!!) ?: 0.0

                        if(ratingDiv!=0.0){
                            val number3digits:Double = Math.round(ratingDiv * 1000.0) / 1000.0
                            val number2digits:Double = Math.round(number3digits * 100.0) / 100.0
                            val solution:Double = Math.round(number2digits * 10.0) / 10.0
                            rating.text = "${solution.toString()} (${it?.numRating!!.toInt()})"

                            if(solution>0.0 && solution<1.0){
                                imageStar1.background = resources.getDrawable(R.drawable.ic_halfstar)
                                imageStar2.background = resources.getDrawable(R.drawable.ic_emptystar)
                                imageStar3.background = resources.getDrawable(R.drawable.ic_emptystar)
                                imageStar4.background = resources.getDrawable(R.drawable.ic_emptystar)
                                imageStar5.background = resources.getDrawable(R.drawable.ic_emptystar)
                            }
                            else if(solution == 1.0){
                                imageStar1.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar2.background = resources.getDrawable(R.drawable.ic_emptystar)
                                imageStar3.background = resources.getDrawable(R.drawable.ic_emptystar)
                                imageStar4.background = resources.getDrawable(R.drawable.ic_emptystar)
                                imageStar5.background = resources.getDrawable(R.drawable.ic_emptystar)
                            }
                            else if(solution>1.0 && solution<2.0){
                                imageStar1.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar2.background = resources.getDrawable(R.drawable.ic_halfstar)
                                imageStar3.background = resources.getDrawable(R.drawable.ic_emptystar)
                                imageStar4.background = resources.getDrawable(R.drawable.ic_emptystar)
                                imageStar5.background = resources.getDrawable(R.drawable.ic_emptystar)
                            }
                            else if(solution == 2.0){
                                imageStar1.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar2.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar3.background = resources.getDrawable(R.drawable.ic_emptystar)
                                imageStar4.background = resources.getDrawable(R.drawable.ic_emptystar)
                                imageStar5.background = resources.getDrawable(R.drawable.ic_emptystar)
                            }
                            else if(solution>2.0 && solution<3.0){
                                imageStar1.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar2.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar3.background = resources.getDrawable(R.drawable.ic_halfstar)
                                imageStar4.background = resources.getDrawable(R.drawable.ic_emptystar)
                                imageStar5.background = resources.getDrawable(R.drawable.ic_emptystar)
                            }
                            else if(solution == 3.0){
                                imageStar1.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar2.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar3.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar4.background = resources.getDrawable(R.drawable.ic_emptystar)
                                imageStar5.background = resources.getDrawable(R.drawable.ic_emptystar)
                            }
                            else if(solution>3.0 && solution<4.0){
                                imageStar1.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar2.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar3.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar4.background = resources.getDrawable(R.drawable.ic_halfstar)
                                imageStar5.background = resources.getDrawable(R.drawable.ic_emptystar)
                            }
                            else if(solution == 4.0){
                                imageStar1.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar2.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar3.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar4.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar5.background = resources.getDrawable(R.drawable.ic_emptystar)
                            }
                            else if(solution>4.0 && solution<5.0){
                                imageStar1.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar2.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar3.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar4.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar5.background = resources.getDrawable(R.drawable.ic_halfstar)
                            }
                            else if(solution == 5.0){
                                imageStar1.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar2.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar3.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar4.background = resources.getDrawable(R.drawable.ic_star)
                                imageStar5.background = resources.getDrawable(R.drawable.ic_star)
                            }

                        }
                        else{
                            rating.text = "(0)"
                        }

                    }

                })
            }
            else{
                imageProfileItem.setImageResource(R.drawable.ic_avatar)
            }

            if(canModify){
                buyers_listview_label.visibility = View.VISIBLE
                buyers_listview_layout.visibility = View.VISIBLE

                vm.getInterestedUsers(it.id).observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                    if(it.isNotEmpty()) {
                        interestedUsers = ArrayList()

                        it.forEach {
                            interestedUsers.add(Pair(it.nickname, it.id))
                        }
                    }

                })
            }
        })

        val scroll: ScrollView = view.findViewById(R.id.detail_scrollview)
        val transparent: ImageView = view.findViewById(R.id.imagetransparent)

        transparent.setOnTouchListener { v, event ->
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    // Disallow ScrollView to intercept touch events.
                    scroll.requestDisallowInterceptTouchEvent(true)
                    // Disable touch on transparent view
                    false
                }
                MotionEvent.ACTION_UP -> {
                    // Allow ScrollView to intercept touch events.
                    scroll.requestDisallowInterceptTouchEvent(false)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    scroll.requestDisallowInterceptTouchEvent(true)
                    false
                }
                else -> true
            }
        }


        fab_buy.setOnClickListener {
            val repo : FirebaseRepo = FirebaseRepo.INSTANCE
            val entry = item
            val uid = repo.getID(requireContext())
            if(entry != null) {
                repo.checkFavorite(uid, entry.id).addOnCompleteListener {
                    if(it.isSuccessful){
                        if(it.result?.isEmpty!!) {
                            repo.insertFavorite(repo.getID(requireContext()), entry).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    fab_buy.setBackgroundResource(R.drawable.ic_fav)
                                    Snackbar.make(view, getString(R.string.favorite), Snackbar.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            repo.getFavDocId(repo.getID(requireContext()), entry.id).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    if(!it.result?.isEmpty!!) {
                                        var idDoc: String = ""
                                        for (document in it.result!!) {
                                            idDoc = document.id
                                            break
                                        }
                                        repo.removeFavorite(idDoc).addOnCompleteListener {
                                            if(it.isSuccessful) {
                                                fab_buy.setBackgroundResource(R.drawable.ic_fav_unselect)
                                                Snackbar.make(view, getString(R.string.unfavorite), Snackbar.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }
                            }
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
            showInterestedDialog(item?.state.toString() == "SOLD")
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
        // val entry = item
        val itemId = vm.itemId
        val uid = repo.getID(requireContext())
        if(itemId != null) {
            repo.checkFavorite(uid, itemId).addOnCompleteListener {
                if(it.isSuccessful){
                    if(it.result?.isEmpty!!) {
                        if(!isOwner && fab_buy != null)
                            fab_buy.setBackgroundResource(R.drawable.ic_fav_unselect)
                    } else {
                        if(!isOwner && fab_buy != null)
                            fab_buy.setBackgroundResource(R.drawable.ic_fav)
                    }
                    if(!isOwner && fab_buy != null)
                        fab_buy.visibility = View.VISIBLE
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

    private fun hide_fab() {
        fab_buy.visibility = View.INVISIBLE
    }

    override fun onDetach() {
        super.onDetach()

        if(snackbar != null)
            snackbar!!.dismiss()
    }

    private fun showInterestedDialog(itemAlreadySold: Boolean = false) {

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

            if(canModify) {
                vm.getInterestedUsers(it.id)
                    .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                        if (it.isNotEmpty()) {
                            interestedUsers = ArrayList()

                            it.forEach {
                                interestedUsers.add(Pair(it.nickname, it.id))
                            }

                            val arrayAdapter: ListAdapter<String> =
                                ListAdapter(
                                    requireContext(),
                                    android.R.layout.simple_list_item_1,
                                    interestedUsers.map { it.second },
                                    object : ListAdapter.ListAdapterListener {
                                        override fun sellItemViewOnClick(nicknameBuyer: String) {

                                            // update item -> set buyer and state
                                            vm.updateItemSold(nicknameBuyer).addOnCompleteListener {
                                                if (it.isSuccessful) {
                                                    view?.let { it1 ->
                                                        dialog.dismiss()
                                                        Snackbar.make(it1, R.string.item_sold, Snackbar.LENGTH_SHORT).show()
                                                    }
                                                } else
                                                    view?.let { it1 ->
                                                        dialog.dismiss()
                                                        Snackbar.make(it1, R.string.item_sold_error, Snackbar.LENGTH_SHORT).show()
                                                    }
                                            }
                                        }
                                    },
                                    itemAlreadySold
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

    override fun onMapReady(gmap: GoogleMap?) {
        googleMap = gmap
    }
}
