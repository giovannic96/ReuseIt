package it.polito.mad.mhackeroni.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import it.polito.mad.mhackeroni.utilities.IDGenerator
import it.polito.mad.mhackeroni.utilities.ImageUtils
import it.polito.mad.mhackeroni.utilities.Validation
import it.polito.mad.mhackeroni.viewmodel.EditItemFragmentViewModel
import it.polito.mad.mhackeroni.viewmodel.MapViewModel
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_item_details.*
import kotlinx.android.synthetic.main.fragment_item_edit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class ItemEditFragment: Fragment() {
    var item: Item? = null
    private var oldItem: Item? = null
    private lateinit var currentItemPhotoPath: String
    private val rotationCount: MutableLiveData<Int> = MutableLiveData()
    private val helperTextVisible: MutableLiveData<Boolean> = MutableLiveData()
    private val REQUEST_PICKIMAGE=9002
    private val REQUEST_CREATEIMAGE=9001
    private val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE=9002
    private var isAddingItem: Boolean = false
    private var cat : String? = null
    private var subCat: String? = null
    private var cond : String? = null
    val c = Calendar.getInstance()
    private var pickerShowing = false
    lateinit var vm : EditItemFragmentViewModel
    private var startCamera = false
    private var imageChanged = false
    val logger: Logger = Logger.getLogger(ItemEditFragment::class.java.name)
    private var state : Item.ItemState = Item.ItemState.AVAILABLE
    private var isBlocked = false
    private var mapViewModel: MapViewModel = MapViewModel()
    private var location : String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_item_edit, container, false)
        activity?.run {
            mapViewModel = ViewModelProviders.of(requireActivity()).get(MapViewModel::class.java)
        }
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm = ViewModelProvider(this).get(EditItemFragmentViewModel::class.java)

        val rotationSaved = savedInstanceState?.getInt("rotation")
        if(rotationSaved != null)
            rotationCount.value = rotationSaved
        else
            rotationCount.value = 0

        val helperTextSaved = savedInstanceState?.getBoolean("helperText")
        if(helperTextSaved != null)
            helperTextVisible.value = helperTextSaved
        else
            helperTextVisible.value = true

        var itemJSON= arguments?.getString("item", "")

        //NEW ITEM
        if(itemJSON.isNullOrEmpty()) {
            isAddingItem = true

            val savedItem = savedInstanceState?.getString("item")?.let {
                Item.fromStringJSON(
                    it
                )
            }
            if(!savedItem?.image.isNullOrEmpty())
                currentItemPhotoPath = savedItem?.image ?: ""

            edit_state.visibility = View.GONE
            action_layout.visibility = View.GONE
        }
        //EDIT ITEM
        else {
            isAddingItem = false
            val savedItem = savedInstanceState?.getString("item")?.let {
                Item.fromStringJSON(
                    it
                )
            }
            item =
                Item.fromStringJSON(itemJSON)

            oldItem =
                Item.fromStringJSON(itemJSON)
            currentItemPhotoPath = item?.image.toString()

            vm.itemId = oldItem?.id.toString()

            if (savedItem != null) {
                item = savedItem // Get saved value
                currentItemPhotoPath = savedItem.image.toString()
            }
        }

        if(isAddingItem){
            var itemData = vm.getLocalItem()

            if(itemData != null){
                edit_itemTitle.setText(itemData.name)
                edit_itemPrice.setText(
                    itemData.price.toString()
                )
                edit_itemDesc.setText(itemData.desc)
                edit_itemExpiryDate.setText(itemData.expiryDate)
                edit_itemLocation.setText(itemData.location)

                radio_available.isChecked = true
                 //radio_sold.isChecked = false
                radio_block.isChecked = false

                try {
                    if(itemData.image.isNullOrEmpty()){
                        // edit_itemImage.setImageResource(R.drawable.ic_box)
                        Glide.with(requireContext())
                            .load(R.drawable.ic_box)
                            .into(edit_itemImage as ImageView)

                    } else {
                        Glide.with(requireContext())
                            .load(itemData.image)
                            .into(edit_itemImage as ImageView)
                    }
                } catch(ex: IllegalStateException) {
                    logger.log(Level.WARNING, "context not attached", ex)
                } catch (e: Exception) {
                    Snackbar.make(view,R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
                }
            }

        } else {
            vm.getItem().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                var itemData = it

                if(itemData.state == Item.ItemState.SOLD){
                    edit_state.visibility = View.GONE
                    action_layout.visibility = View.GONE
                } else {
                    edit_state.visibility = View.VISIBLE
                    action_layout.visibility = View.VISIBLE
                }

                if(vm.getLocalItem() != null){
                    itemData = vm.getLocalItem()
                }

                if(itemData != null){
                    edit_itemTitle.setText(itemData.name)
                    edit_itemPrice.setText(
                        itemData.price.toString()
                    )
                    edit_itemDesc.setText(itemData.desc)
                    edit_itemExpiryDate.setText(itemData.expiryDate)

                    if(location.isNullOrEmpty())
                        edit_itemLocation.setText(itemData.location)
                    else
                        edit_itemLocation.setText(location)

                    val state = itemData.state.toString()


                    if(state == "AVAILABLE"){
                        radio_available.isChecked = true
                        // radio_sold.isChecked = false
                        radio_block.isChecked = false
                    }
                    /*
                    else if(state == "SOLD"){
                        radio_available.isChecked = false
                        // radio_sold.isChecked = true
                        radio_block.isChecked = false
                    }
                     */
                    else if(state == "BLOCKED"){
                        isBlocked = true
                        radio_available.isChecked = false
                        // radio_sold.isChecked = false
                        radio_block.isChecked = true
                    }

                    try {
                        if(itemData.image.isNullOrEmpty()){
                            // edit_itemImage.setImageResource(R.drawable.ic_box)
                            Glide.with(requireContext())
                                .load(R.drawable.ic_box)
                                .into(edit_itemImage as ImageView)
                        } else {
                            Glide.with(requireContext())
                                .load(itemData.image)
                                .into(edit_itemImage as ImageView)
                        }
                    } catch(ex: IllegalStateException) {
                        logger.log(Level.WARNING, "context not attached", ex)
                    } catch (e: Exception) {
                        Snackbar.make(view,R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
        }


        handleDatePicker()

        mapViewModel.position.observe(viewLifecycleOwner, androidx.lifecycle.Observer { position ->

            if(position != null) {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())

                try{
                    val addresses: List<Address> = geocoder
                        .getFromLocation(
                            position.latitude,
                            position.longitude,
                            1
                        )

                    val city: String = addresses[0].locality
                    if (edit_location != null)
                        edit_location.setText(city)
                    location = city

                    if(item == null)
                        item = Item()

                    item?.lat = position.latitude
                    item?.lng = position.longitude

                    if(isAddingItem) {
                        item?.let { vm.updateLocalItem(it) }
                    }

                } catch (e: java.lang.IllegalStateException) {
                    Snackbar.make(view, getString(R.string.locationError), Snackbar.LENGTH_SHORT)
                        .show()
                } catch (e: Exception){
                    Snackbar.make(view, getString(R.string.networkerror), Snackbar.LENGTH_SHORT)
                        .show()
                }


                mapViewModel.position.value = null
            }
        })

        edit_itemLocation.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_nav_ItemDetailEdit_to_mapFragment)
        }

        edit_state.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.radio_available -> {
                    state = Item.ItemState.AVAILABLE
                    isBlocked = false
                }
                R.id.radio_block -> {
                    if(!isBlocked) {
                        Snackbar.make(view, getString(R.string.blockedHint), Snackbar.LENGTH_LONG)
                            .show()
                    }
                    state = Item.ItemState.BLOCKED
                }
                /*
                R.id.radio_sold -> {
                    state = Item.ItemState.SOLD
                    isBlocked = false
                }
                 */
            }
        }

        rotationCount.observe(requireActivity(), androidx.lifecycle.Observer {
            val deg: Float = 90f * it
            edit_itemImage.animate().rotation(deg).interpolator =
                AccelerateDecelerateInterpolator()
        })

        helperTextVisible.observe(requireActivity(), androidx.lifecycle.Observer {
            if(helperTextVisible.value == false)
                edit_itemSubCategory_wrapper?.helperText = ""
            else
                edit_itemSubCategory_wrapper?.helperText = resources.getString(R.string.helper_subcat)
        })

        edit_itemCamera.setOnClickListener {
            val popupMenu=PopupMenu(requireContext(), edit_itemCamera)
            popupMenu.menuInflater.inflate(R.menu.context_menu_image, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.fromCamera -> {
                        startCamera = true
                        dispatchTakePictureIntent()
                    }
                    R.id.fromGallery -> {
                        dispatchPickImageIntent()
                    }
                    else -> {
                        // Nothing to do
                    }
                }
                true
            }
            popupMenu.show()
        }

        btn_rotate_imageItem.setOnClickListener {
            startCamera = false
          if(::currentItemPhotoPath.isInitialized && currentItemPhotoPath.isNullOrEmpty()){
              Snackbar
                  .make(view.rootView, resources.getString(R.string.rotate_error), Snackbar.LENGTH_SHORT)
                  .show()
          } else  if(::currentItemPhotoPath.isInitialized
                && ImageUtils.canDisplayBitmap(currentItemPhotoPath, requireContext())
                && hasExStoragePermission()){
                rotationCount.value = rotationCount.value?.plus(1)
            } else if(!hasExStoragePermission()) {
                checkExStoragePermission()
            } else{
                Snackbar
                    .make(view.rootView, resources.getString(R.string.rotate_error), Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        // Handle menu item selection
        return when (menuItem.itemId) {
            R.id.menu_save -> {

                if(isAddingItem){
                    item = item?.hasFeedback?.let {
                        Item(
                            "",
                            edit_itemTitle.text.toString(),
                            edit_itemPrice.text.toString().toDoubleOrNull() ?: 0.0,
                            edit_itemDesc.text.toString(),
                            cat ?: "",
                            subCat ?: "",
                            edit_itemExpiryDate.text.toString(),
                            edit_itemLocation.text.toString(),
                            cond ?: "",
                            null,
                            "",
                            it,
                            lat = item?.lat,
                            lng = item?.lng
                        )
                    }
                } else {
                    item = item?.hasFeedback?.let {
                        Item(
                            oldItem?.id ?: "",
                            edit_itemTitle.text.toString(),
                            edit_itemPrice.text.toString().toDoubleOrNull() ?: 0.0,
                            edit_itemDesc.text.toString(),
                            cat ?: oldItem!!.category,
                            subCat ?: oldItem!!.subcategory,
                            edit_itemExpiryDate.text.toString(),
                            edit_itemLocation.text.toString(),
                            cond ?: oldItem!!.condition,
                            null,
                            "",
                            it,
                            state = state,
                            lat = item?.lat,
                            lng = item?.lng
                        )
                    }
                }


                val nRotation = rotationCount.value

                if(::currentItemPhotoPath.isInitialized){
                    if (nRotation != null) {
                        if(nRotation != 0 && nRotation.rem(4) != 0
                            && ImageUtils.canDisplayBitmap(currentItemPhotoPath, requireContext())){ // Save the edited photo
                            ImageUtils.rotateImageFromUri(
                                Uri.parse(currentItemPhotoPath),
                                90.0F* nRotation,
                                requireContext()
                            )?.let {
                                currentItemPhotoPath = ImageUtils.insertImage(requireActivity().contentResolver,
                                    it
                                ).toString()
                            }
                        }
                    }
                    item?.image = currentItemPhotoPath
                }


                if(isAddingItem) {
                    item!!.id = IDGenerator.getNextID(requireContext()).toString()

                    if(!checkData())
                        return false

                    vm.updateLocalItem(item!!)

                    vm.addItem(requireContext()).addOnCompleteListener {
                        if(it.isSuccessful){
                            val bundle =
                                bundleOf("new_item" to item?.let { Item.toJSON(
                                    it
                                ).toString() })

                            view?.findNavController()
                                ?.navigate(R.id.action_nav_ItemDetailEdit_to_nav_itemList, bundle)
                        } else {
                            view?.let { it1 -> Snackbar.make(it1, getString(R.string.errorConnection), Snackbar.LENGTH_SHORT).show() }
                            val bundle =
                                bundleOf("new_item" to item?.let { Item.toJSON(
                                    it
                                ).toString() })

                            view?.findNavController()
                                ?.navigate(R.id.action_nav_ItemDetailEdit_to_nav_itemList, bundle)

                        }
                    }
                }else {

                    if(!checkData())
                        return false

                    item!!.id = oldItem?.id ?: ""
                    val fromList = arguments?.getBoolean("fromList", false)
                    vm.updateLocalItem(item!!)

                    if(imageChanged) {
                        vm.updateItem(requireContext()).addOnCompleteListener {
                            if (it.isSuccessful) {
                                /*
                                val bundle =
                                    bundleOf("new_item" to item?.let {
                                        Item.toJSON(it).toString()
                                    })

                                 */

                                if (fromList!!) {
                                    val bundle =
                                        bundleOf("edited_item" to item?.let {
                                            Item.toJSON(it).toString()
                                        },
                                            "old_item" to oldItem?.let {
                                                Item.toJSON(it).toString()
                                            })
                                    view?.findNavController()
                                        ?.navigate(R.id.action_nav_ItemDetailEdit_to_nav_itemList, bundle)
                                } else {
                                    val bundle =
                                        bundleOf("new_item" to item?.let {
                                            Item.toJSON(it).toString()
                                        },
                                            "old_item" to oldItem?.let {
                                                Item.toJSON(it).toString()
                                            })
                                    view?.findNavController()
                                        ?.navigate(R.id.action_nav_ItemDetailEdit_to_nav_ItemDetail, bundle)
                                }
                            } else {
                                view?.let { it1 ->
                                    Snackbar.make(it1, getString(R.string.errorConnection), Snackbar.LENGTH_SHORT).show()
                                }

                                val bundle =
                                    bundleOf("new_item" to item?.let {
                                        Item.toJSON(it).toString()
                                    })

                                if (fromList!!) {
                                    val bundle =
                                        bundleOf("edited_item" to item?.let {
                                            Item.toJSON(it).toString()
                                        },
                                            "old_item" to oldItem?.let {
                                                Item.toJSON(it).toString()
                                            })
                                    view?.findNavController()
                                        ?.navigate(R.id.action_nav_ItemDetailEdit_to_nav_itemList, bundle)
                                } else {
                                    val bundle =
                                        bundleOf("new_item" to item?.let {
                                            Item.toJSON(it).toString()
                                        },
                                            "old_item" to oldItem?.let {
                                                Item.toJSON(it).toString()
                                            })
                                    view?.findNavController()
                                        ?.navigate(R.id.action_nav_ItemDetailEdit_to_nav_ItemDetail, bundle)
                                }
                            }
                        }
                    } else {
                        vm.updateItemNoImageUpload(requireContext()).addOnCompleteListener {

                            val localImage = vm.getLocalItem()?.image!!

                            if (it.isSuccessful) {
                                val bundle =
                                    bundleOf("uploadImage" to false, "new_item" to item?.let {
                                        Item.toJSON(it).toString()
                                    })

                                if (fromList!!) {
                                    val bundle =
                                        bundleOf("uploadImage" to false, "edited_item" to item?.let {
                                            Item.toJSON(it).toString()
                                        },
                                            "old_item" to oldItem?.let {
                                                Item.toJSON(it).toString()
                                            })
                                    view?.findNavController()
                                        ?.navigate(R.id.action_nav_ItemDetailEdit_to_nav_itemList, bundle)
                                } else {
                                    val bundle =
                                        bundleOf("uploadImage" to false, "new_item" to item?.let {
                                            Item.toJSON(it).toString()
                                        },
                                            "old_item" to oldItem?.let {
                                                Item.toJSON(it).toString()
                                            })
                                    view?.findNavController()
                                        ?.navigate(R.id.action_nav_ItemDetailEdit_to_nav_ItemDetail, bundle)
                                }
                            } else {
                                view?.let { it1 ->
                                    Snackbar.make(it1, getString(R.string.errorConnection), Snackbar.LENGTH_SHORT).show()
                                }

                                val bundle =
                                    bundleOf("new_item" to item?.let {
                                        Item.toJSON(it).toString()
                                    })

                                if (fromList!!) {
                                    val bundle =
                                        bundleOf("edited_item" to item?.let {
                                            Item.toJSON(it).toString()
                                        },
                                            "old_item" to oldItem?.let {
                                                Item.toJSON(it).toString()
                                            })
                                    view?.findNavController()
                                        ?.navigate(R.id.action_nav_ItemDetailEdit_to_nav_itemList, bundle)
                                } else {
                                    val bundle =
                                        bundleOf("new_item" to item?.let {
                                            Item.toJSON(it).toString()
                                        },
                                            "old_item" to oldItem?.let {
                                                Item.toJSON(it).toString()
                                            })
                                    view?.findNavController()
                                        ?.navigate(R.id.action_nav_ItemDetailEdit_to_nav_ItemDetail, bundle)
                                }
                            }
                        }
                    }

                }
                return true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    override fun onResume() {
        super.onResume()

        val categories = resources.getStringArray(R.array.categories)
        val subcategories = resources.getStringArray(R.array.subcategories)
        val arts = resources.getStringArray(R.array.arts)
        val sports = resources.getStringArray(R.array.sports)
        val babies = resources.getStringArray(R.array.babies)
        val womens = resources.getStringArray(R.array.womens)
        val mens = resources.getStringArray(R.array.mens)
        val electronics = resources.getStringArray(R.array.electronics)
        val games = resources.getStringArray(R.array.games)
        val automotives = resources.getStringArray(R.array.automotives)
        val conditions = resources.getStringArray(R.array.conditions)
        var selectedCat = categories

        val adapterCat: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        edit_itemCategory.setAdapter(adapterCat)

        var adapterSubcat: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            subcategories
        )

        edit_itemSubCategory.setAdapter(adapterSubcat)

        val adapterCond = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            conditions
        )
        edit_itemCondition.setAdapter(adapterCond)


        edit_itemCategory.onItemClickListener = object : AdapterView.OnItemSelectedListener,
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
                view: View?,
                position: Int,
                id: Long
            ) {

                edit_itemSubCategory.setText(R.string.selectSubcat)
                edit_itemCategory.error = null

                when(position){
                    0-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, arts)
                        selectedCat = arts
                    }
                    1-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, sports)
                        selectedCat = sports
                    }
                    2-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, babies)
                        selectedCat = babies
                    }
                    3-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, womens)
                        selectedCat = womens
                    }
                    4-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, mens)
                        selectedCat = mens
                    }
                    5-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, electronics)
                        selectedCat = electronics
                    }
                    6-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, games)
                        selectedCat = games
                    }
                    7-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, automotives)
                        selectedCat = automotives
                    }

                }

                edit_itemSubCategory.setAdapter(adapterSubcat)
                cat = categories[position]
            }
        }

        edit_itemSubCategory.onItemClickListener = object : AdapterView.OnItemSelectedListener,
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
                view: View?,
                position: Int,
                id: Long
            ) {
                edit_itemSubCategory.error = null
                subCat = selectedCat[position]
                helperTextVisible.value = false
            }
        }

        edit_itemCondition.onItemClickListener = object : AdapterView.OnItemSelectedListener,
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
                view: View?,
                position: Int,
                id: Long
            ) {
                edit_itemCondition.error = null
                cond = conditions[position]
            }
        }

        edit_itemPrice.filters = arrayOf<InputFilter>(Validation.DecimalDigitsInputFilter(5, 2))

        if(!item?.category.isNullOrEmpty()){
            val value = item?.category
            val pos = adapterCat.getPosition(value)

            edit_itemCategory.hint = "\n${value}"

            adapterCat.notifyDataSetChanged()
            try {
                edit_itemCategory.setSelection(pos)
            } catch(e: IndexOutOfBoundsException){
                // Not valid selection
            }
        }
        if(!item?.condition.isNullOrEmpty()){
            val value = item?.condition
            val pos = adapterCond.getPosition(value)

            edit_itemCondition.hint = "\n${value}"
            adapterCond.notifyDataSetChanged()
         try{
            edit_itemCondition.setSelection(pos)
            } catch(e: IndexOutOfBoundsException){
                // Not valid selection
            }

        }
        if(!item?.subcategory.isNullOrEmpty()){
            val value = item?.subcategory


           if (item?.category.isNullOrEmpty())
            return

            // val pos = adapterSubcat.getPosition(value)

            adapterSubcat.notifyDataSetChanged()
            edit_itemSubCategory.hint = "\n${value}"
            // edit_itemSubCategory.setSelection(pos)
        }

        if(isAddingItem && !location.isNullOrEmpty()){
            edit_itemLocation.setText(location)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        when (requestCode) {
            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {

                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted
                    if(startCamera)
                        dispatchTakePictureIntent()
                } else {
                    this.view?.let {
                        Snackbar.make(it, resources.getString(R.string.permission_err), Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATEIMAGE && resultCode == AppCompatActivity.RESULT_OK) {
            if(::currentItemPhotoPath.isInitialized){
                val oldPhoto = currentItemPhotoPath
                imageChanged = true

                currentItemPhotoPath = ImageUtils.getBitmap(currentItemPhotoPath, requireContext())?.let {
                    ImageUtils.insertImage(requireActivity().contentResolver,
                        it
                    )
                }.toString()
                File(oldPhoto).delete()

                // edit_itemImage.setImageBitmap(ImageUtils.getBitmap(currentItemPhotoPath, requireContext()))
                Glide.with(requireContext())
                    .load(currentItemPhotoPath)
                    .into(edit_itemImage as ImageView)

                rotationCount.value = 0
            }
        }
        else if(requestCode == REQUEST_PICKIMAGE && resultCode == Activity.RESULT_OK) {
            imageChanged = true
            // edit_itemImage.setImageBitmap(ImageUtils.getBitmap(data?.data.toString(), requireContext()))
            Glide.with(requireContext())
                .load(data?.data.toString())
                .into(edit_itemImage as ImageView)


            currentItemPhotoPath = data?.data.toString()
            getPermissionOnUri(Uri.parse(currentItemPhotoPath))
            rotationCount.value = 0
        } else if((requestCode == REQUEST_CREATEIMAGE || requestCode == REQUEST_PICKIMAGE) && resultCode == AppCompatActivity.RESULT_CANCELED){
            currentItemPhotoPath = oldItem?.image ?: ""
            item = oldItem
        } else {
            currentItemPhotoPath = oldItem?.image ?: ""
            item = oldItem
        }
    }

    private fun handleDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()

        //Define date constraints
        val constraintsBuilder = CalendarConstraints.Builder()
        constraintsBuilder.setValidator(DateValidatorPointForward.now())
        builder.setCalendarConstraints(constraintsBuilder.build())

        //build date picker and add callbacks
        val picker = builder.build()
        edit_itemExpiryDate.inputType = InputType.TYPE_NULL
        edit_itemLocation.inputType = InputType.TYPE_NULL

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            edit_itemExpiryDate.focusable = View.NOT_FOCUSABLE
            edit_itemLocation.focusable = View.NOT_FOCUSABLE
        } else {
            edit_itemExpiryDate.isFocusable = false
            edit_itemLocation.isFocusable = false
        }

        edit_itemExpiryDate.setOnClickListener {
            edit_item_container.clearFocus()

            if(!pickerShowing) {
                picker.show(parentFragmentManager, picker.toString())
                pickerShowing = true

                picker.addOnCancelListener {
                    pickerShowing = false
                }
                picker.addOnNegativeButtonClickListener {
                    pickerShowing = false
                }
                picker.addOnPositiveButtonClickListener {
                    edit_itemExpiryDate.setText(formatDate(it))
                    pickerShowing = false
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun formatDate(myDate:Long?): String {
        val date = myDate?.let { Date(it) }
        val format: DateFormat = SimpleDateFormat(resources.getString(R.string.date_format))
        format.timeZone = TimeZone.getTimeZone("Etc/UTC")
        return format.format(date)
    }

    private fun dispatchPickImageIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        this.startActivityForResult(intent, REQUEST_PICKIMAGE)
    }

    private fun dispatchTakePictureIntent() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!checkExStoragePermission()) {
                return
            }
        }

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile(requireContext())
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "it.polito.mad.mhackeroni.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    this.startActivityForResult(takePictureIntent, REQUEST_CREATEIMAGE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(context: Context): File {
        // Create an image file name
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${UUID.randomUUID()}_marketApp_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            currentItemPhotoPath = absolutePath
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if(item == null)
            item = Item(
                "",
                "",
                0.0,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                false
            )


        if(isAddingItem && ::currentItemPhotoPath.isInitialized) {
            item?.image = currentItemPhotoPath ?: ""
        }

        if(this.isVisible) {
            var price: Double

            try {
                price = edit_itemPrice.text.toString().toDouble() ?: 1.0
            } catch (e: Exception){
                price = 1.0
            }

            if (::currentItemPhotoPath.isInitialized) {
                item?.name = edit_itemTitle.text.toString()
                item?.price = price
                item?.desc = edit_itemDesc.text.toString()
                item?.expiryDate = edit_itemExpiryDate.text.toString()
                item?.location = edit_itemLocation.text.toString()

                if (!cat.isNullOrEmpty())
                    item?.category = cat ?: ""

                if(!subCat.isNullOrEmpty())
                    item?.subcategory = subCat ?: ""

                if(!cond.isNullOrEmpty())
                    item?.condition = cond ?: ""

                item?.image = currentItemPhotoPath

            } else {
                item?.name = edit_itemTitle.text.toString()
                item?.price = price
                item?.desc = edit_itemDesc.text.toString()
                item?.expiryDate = edit_itemExpiryDate.text.toString()
                item?.location = edit_itemLocation.text.toString()
                item?.category = cat ?: ""
                item?.subcategory = subCat ?: ""
                item?.condition = cond ?: ""
            }

            outState.putString("item", item?.let { Item.toJSON(
                it
            ).toString() })

            vm.updateLocalItem(item!!)
            rotationCount.value?.let { outState.putInt("rotation", it) }
            helperTextVisible.value?.let { outState.putBoolean("helperText", it) }
        }
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState != null) {
            val savedItem  = savedInstanceState.getString("item")?.let {
                Item.fromStringJSON(
                    it
                )
            }
            val rotation = savedInstanceState.getInt("rotation")
            val helperText = savedInstanceState.getBoolean("helperText")

            rotationCount.value = rotation
            helperTextVisible.value = helperText

            if(savedItem != null){
                item = savedItem
            }

        }
    }

    private fun hasExStoragePermission(): Boolean{
        return (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun checkExStoragePermission(): Boolean{
        if (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Not granted

            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user
                val builder = AlertDialog.Builder(requireActivity())

                // Set the alert dialog title
                builder.setTitle(R.string.warning_dialog)

                // Display a message on alert dialog
                builder.setMessage(resources.getString(R.string.permission_expl))

                // Set a positive button and its click listener on alert dialog
                builder.setPositiveButton("Ok"){ _, _ ->
                    requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
                }

                // Display a negative button on alert dialog
                builder.setNegativeButton(R.string.close_dialog, null)

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
            else {
                // No explanation needed, we can request the permission.
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
            }
            return false
        } else {
            // Permission has already been granted
            return true
        }
    }

    suspend fun addItem(item : Item){
        val firebaseRepo : FirebaseRepo = FirebaseRepo.INSTANCE

        withContext(Dispatchers.IO){
            firebaseRepo.insertItem(item)
        }
    }

    private fun getPermissionOnUri(uri:Uri){
        val contentResolver = requireActivity().contentResolver
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, takeFlags)
    }

    private fun checkData(): Boolean {
        var retVal = true

        if(item?.name.isNullOrEmpty()){
            view?.let { Snackbar.make(it, getString(R.string.error_data), Snackbar.LENGTH_SHORT).show() }
            edit_itemTitle.error = getString(R.string.required_field)
            retVal = false
        }

        if(item?.condition.isNullOrEmpty()){
            view?.let { Snackbar.make(it, getString(R.string.error_data), Snackbar.LENGTH_SHORT).show() }
            edit_itemCondition.error = getString(R.string.required_field)
            retVal = false
        }

        if(item?.category.isNullOrEmpty()){
            view?.let { Snackbar.make(it, getString(R.string.error_data), Snackbar.LENGTH_SHORT).show() }
            edit_itemCategory.error = getString(R.string.required_field)
            retVal = false
        }

        if(item?.subcategory.isNullOrEmpty()){
            view?.let { Snackbar.make(it, getString(R.string.error_data), Snackbar.LENGTH_SHORT).show() }
            edit_itemSubCategory.error = getString(R.string.required_field)
            retVal = false
        }

        if(item?.price == null){
            view?.let { Snackbar.make(it, getString(R.string.error_data), Snackbar.LENGTH_SHORT).show() }
            edit_itemPrice.error = getString(R.string.required_field)
            retVal = false
        }

        if(item?.expiryDate.isNullOrEmpty()){
            view?.let { Snackbar.make(it, getString(R.string.error_data), Snackbar.LENGTH_SHORT).show() }
            edit_itemExpiryDate.error = getString(R.string.required_field)
            retVal = false
        }

        if(item?.location.isNullOrEmpty()){
            view?.let { Snackbar.make(it, getString(R.string.error_data), Snackbar.LENGTH_SHORT).show() }
            edit_itemLocation.error = getString(R.string.required_field)
            retVal = false
        }

        return retVal
    }
}
