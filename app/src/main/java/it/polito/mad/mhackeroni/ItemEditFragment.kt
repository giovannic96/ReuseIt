package it.polito.mad.mhackeroni

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_item_edit.*
import java.io.File
import java.io.IOException
import java.lang.NumberFormatException
import java.util.*

class ItemEditFragment: Fragment() {
    var item: MutableLiveData<Item> = MutableLiveData()
    private var oldItem: Item? = null
    private lateinit var currentItemPhotoPath: String
    private val rotationCount: MutableLiveData<Int> = MutableLiveData()
    private val REQUEST_PICKIMAGE=9002
    private val REQUEST_CREATEIMAGE=9001
    private val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE=9002
    private var isAddingItem: Boolean = false
    private var cat : String? = null
    private var subCat: String? = null
    private var cond : String? = null
    val c = Calendar.getInstance()
    val year = c.get(Calendar.YEAR)
    val month = c.get(Calendar.MONTH)
    val day = c.get(Calendar.DAY_OF_MONTH)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_item_edit, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("MAG", "CREATED")

        val rotationSaved = savedInstanceState?.getInt("rotation")
        if(rotationSaved != null)
            rotationCount.value = rotationSaved
        else
            rotationCount.value = 0

        var itemJSON= arguments?.getString("item", "")

        //NEW ITEM
        if(itemJSON.isNullOrEmpty()) {
            isAddingItem = true
        }
        //EDIT ITEM
        else {
            isAddingItem = false
            val savedItem = savedInstanceState?.getString("item")?.let { Item.fromStringJSON(it) }
            item.value = Item.fromStringJSON(itemJSON)
            oldItem = item.value
            currentItemPhotoPath = item.value?.image.toString()

            if (savedItem != null) {
                item.value = savedItem // Get saved value
                currentItemPhotoPath = savedItem.image.toString()
            }
        }

        val categories = resources.getStringArray(R.array.categories)
        val subcategories = resources.getStringArray(R.array.subcategories)
        val arts = resources.getStringArray(R.array.arts)
        val sports = resources.getStringArray(R.array.sports)
        val babies = resources.getStringArray(R.array.babies)
        val womens = resources.getStringArray(R.array.womens)
        val mens = resources.getStringArray(R.array.mens)
        val electronics = resources.getStringArray(R.array.electronics)
        val games = resources.getStringArray(R.array.games)
        val automotives = resources.getStringArray(R.array.electronics)
        val conditions = resources.getStringArray(R.array.conditions)
        var selectedCat = categories

        var adapterCat = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_item, categories)
        edit_itemCategory.adapter = adapterCat

        var adapterSubcat = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_item, subcategories)
        edit_itemSubCategory.adapter = adapterSubcat

        val adapterCond = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_item, conditions)
        edit_itemCondition.adapter = adapterCond

        edit_itemCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position){
                    0-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, arts)
                        edit_itemSubCategory.adapter = adapterSubcat
                        selectedCat = arts
                    }
                    1-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, sports)
                        edit_itemSubCategory.adapter = adapterSubcat
                        selectedCat = sports
                    }
                    2-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, babies)
                        edit_itemSubCategory.adapter = adapterSubcat
                        selectedCat = babies
                    }
                    3-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, womens)
                        edit_itemSubCategory.adapter = adapterSubcat
                        selectedCat = womens
                    }
                    4-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, mens)
                        edit_itemSubCategory.adapter = adapterSubcat
                        selectedCat = mens
                    }
                    5-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, electronics)
                        edit_itemSubCategory.adapter = adapterSubcat
                        selectedCat = electronics
                    }
                    6-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, games)
                        edit_itemSubCategory.adapter = adapterSubcat
                        selectedCat = games
                    }
                    7-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, automotives)
                        edit_itemSubCategory.adapter = adapterSubcat
                        selectedCat = automotives
                    }
                }
                cat = categories[position]
            }
        }

        edit_itemSubCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                subCat = selectedCat[position]
            }
        }

        if(item.value?.category!=null){
            setSpinText(edit_itemCategory, item.value?.category)
        }
        if(item.value?.condition!=null){
            setSpinText(edit_itemCondition, item.value?.condition)
        }
        if(item.value?.subcategory!=null){
            setSpinText(edit_itemSubCategory, item.value?.subcategory)
        }


        edit_itemCondition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                cond = conditions[position]
            }
        }

        edit_itemExpiryDate.setOnClickListener(){

            val dpd = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                edit_itemExpiryDate.setText("$year/$monthOfYear/$dayOfMonth")
            }, year, month, day)

            dpd.show()

        }

        item.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            edit_itemTitle.setText(item.value?.name ?: resources.getString(R.string.defaultTitle))
            edit_itemPrice.setText(
                item.value?.price.toString() ?: resources.getString(R.string.defaultPrice)
            )
            edit_itemDesc.setText(item.value?.desc ?: resources.getString(R.string.defaultTitle))
            edit_itemExpiryDate.setText(item.value?.expiryDate ?: resources.getString(R.string.defaultExpire))
            edit_itemLocation.setText(item.value?.location ?: resources.getString(R.string.defaultLocation))

            try {
                edit_itemImage.setImageBitmap(item.value?.image?.let {
                        it1 -> ImageUtils.getBitmap(it1, requireContext())
                })
            } catch (e: Exception) {
                Snackbar.make(view, R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
            }

        })

        rotationCount.observe(requireActivity(), androidx.lifecycle.Observer {
            val deg: Float = 90f * it
            edit_itemImage.animate().rotation(deg).interpolator =
                AccelerateDecelerateInterpolator()
        })

        edit_itemCamera.setOnClickListener {
            val popupMenu=PopupMenu(requireContext(), edit_itemImage)
            popupMenu.menuInflater.inflate(R.menu.context_menu_image, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.fromCamera -> {
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
            if(::currentItemPhotoPath.isInitialized
                && ImageUtils.canDisplayBitmap(currentItemPhotoPath, requireContext())
                && hasExStoragePermission()){
                rotationCount.value = rotationCount.value?.plus(1)
            } else if(!hasExStoragePermission()) {
                checkExStoragePermission()
            } else{
                Snackbar
                    .make(edit_item_container, resources.getString(R.string.rotate_error), Snackbar.LENGTH_SHORT)
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
                var price:Double

                if(edit_itemPrice.text.toString().isNullOrBlank()){
                    price = 0.0
                } else {
                    if(edit_itemPrice.text.toString().equals("null")){
                        price = 0.0
                    } else {
                        try {
                            price = edit_itemPrice.toString().toDouble()
                        }catch(e: NumberFormatException){
                            price = 0.0
                        }
                    }
                }

                if(::currentItemPhotoPath.isInitialized)
                    item.value = cat?.let {
                        cond?.let { it1 ->
                            subCat?.let { it2 ->
                                Item(-1, edit_itemTitle.text.toString(), price,
                                    edit_itemDesc.text.toString(), it, it2,
                                    edit_itemExpiryDate.text.toString(), edit_itemLocation.text.toString(),
                                    it1, currentItemPhotoPath
                                )
                            }
                        }
                    }
                else
                    item.value = cat?.let {
                        cond?.let { it1 ->
                            subCat?.let { it2 ->
                                Item(-1, edit_itemTitle.text.toString(), price,
                                    edit_itemDesc.text.toString(), it, it2,
                                    edit_itemExpiryDate.text.toString(), edit_itemLocation.text.toString(),
                                    it1, item.value?.image)
                            }
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
                    item.value!!.image = currentItemPhotoPath
                }

                if(isAddingItem) {
                    item.value!!.id = IDGenerator.getNextID(requireContext())

                    val bundle =
                        bundleOf("new_item" to item.value?.let { Item.toJSON(it).toString() })

                    view?.findNavController()
                        ?.navigate(R.id.action_nav_ItemDetailEdit_to_nav_itemList, bundle)

                }else {
                    item.value!!.id = oldItem?.id ?: -1
                    val fromList = arguments?.getBoolean("fromList", false)


                    if(fromList!!){
                        val bundle =
                            bundleOf("edited_item" to item.value?.let { Item.toJSON(it).toString()},
                                "old_item" to oldItem?.let { Item.toJSON(it).toString() })
                        view?.findNavController()
                            ?.navigate(R.id.action_nav_ItemDetailEdit_to_nav_itemList, bundle)
                    } else {
                        val bundle =
                            bundleOf("new_item" to item.value?.let { Item.toJSON(it).toString()},
                                "old_item" to oldItem?.let { Item.toJSON(it).toString() })
                        view?.findNavController()
                            ?.navigate(R.id.action_nav_ItemDetailEdit_to_nav_ItemDetail, bundle)
                    }
                }
                return true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    override fun onResume() {
        super.onResume()
        // Used to solve lazy update issue
        if(::currentItemPhotoPath.isInitialized){
            edit_itemImage.setImageBitmap(ImageUtils.getBitmap(currentItemPhotoPath, requireContext()))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        when (requestCode) {
            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {

                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted
                    dispatchTakePictureIntent()
                } else {
                    Snackbar.make(edit_item_container, resources.getString(R.string.permission_err), Snackbar.LENGTH_SHORT)
                        .show()
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

                currentItemPhotoPath = ImageUtils.getBitmap(currentItemPhotoPath, requireContext())?.let {
                    ImageUtils.insertImage(requireActivity().contentResolver,
                        it
                    )
                }.toString()
                File(oldPhoto).delete()

                edit_itemImage.setImageBitmap(ImageUtils.getBitmap(currentItemPhotoPath, requireContext()))
                rotationCount.value = 0
            }
        }
        else if(requestCode == REQUEST_PICKIMAGE && resultCode == Activity.RESULT_OK) {
            edit_itemImage.setImageBitmap(ImageUtils.getBitmap(data?.data.toString(), requireContext()))
            currentItemPhotoPath = data?.data.toString()
            getPermissionOnUri(Uri.parse(currentItemPhotoPath))
            rotationCount.value = 0
        }
    }

    private fun dispatchPickImageIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_PICKIMAGE)
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

        if(this.isVisible) {
            val price: Double = if (edit_itemPrice.text.toString().isEmpty() || edit_itemPrice.text == null)
                0.0
            else
                edit_itemPrice.text.toString().toDouble()

            if (::currentItemPhotoPath.isInitialized)
                item.value = cat?.let {
                    cond?.let { it1 ->
                        subCat?.let { it2 ->
                            item.value?.id?.let { it3 ->
                                Item(
                                    it3,
                                    edit_itemTitle.text.toString(),
                                    price,
                                    edit_itemDesc.text.toString(),
                                    it,
                                    it2,
                                    edit_itemExpiryDate.text.toString(),
                                    edit_itemLocation.text.toString(),
                                    it1,
                                    currentItemPhotoPath
                                )
                            }
                        }
                    }
                }
            else
                item.value = cat?.let {
                    cond?.let { it1 ->
                        subCat?.let { it2 ->
                            item.value?.id?.let { it3 ->
                                Item(
                                    it3,
                                    edit_itemTitle.text.toString(),
                                    price,
                                    edit_itemDesc.text.toString(),
                                    it,
                                    it2,
                                    edit_itemExpiryDate.text.toString(),
                                    edit_itemLocation.text.toString(),
                                    it1,
                                    item.value?.image
                                )
                            }
                        }
                    }
                }

            Log.d("MAG", "SAVING")
            outState.putString("item", item.value?.let { Item.toJSON(it).toString() })

            rotationCount.value?.let { outState.putInt("rotation", it) }
        }
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d("MAG", "RESTORE")
        if (savedInstanceState != null) {
            val savedItem  = savedInstanceState.getString("item")?.let { Item.fromStringJSON(it) }
            val rotation = savedInstanceState.getInt("rotation")

            rotationCount.value = rotation

            if(savedItem != null)
                item.value = savedItem
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


    private fun getPermissionOnUri(uri:Uri){
        val contentResolver = requireActivity().contentResolver
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, takeFlags)
    }

    fun setSpinText(spin: Spinner, text: String?) {
        for (i in 0 until spin.getAdapter().getCount()) {
            if (text?.let { spin.getAdapter().getItem(i).toString().contains(it) }!!) {
                spin.setSelection(i)
            }
        }
    }
}