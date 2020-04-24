package it.polito.mad.mhackeroni

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
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_item_edit.*
import java.io.File
import java.io.IOException
import java.util.*

class ItemEditFragment: Fragment() {
    var item: MutableLiveData<Item> = MutableLiveData()
    private val REQUEST_PICKIMAGE=9002
    private val REQUEST_CREATEIMAGE=9001
    private val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE=9002

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_item_edit, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemJSON=arguments?.getString("item", "")
        val savedItem=savedInstanceState?.getString("item")?.let { Item.fromStringJSON(it) }
        Log.d("ITEM JSON", itemJSON.toString())

        if (!itemJSON.isNullOrEmpty()) {
            item.value=Item.fromStringJSON(itemJSON)
        }

        // Get saved value
        if (savedItem != null) {
            item.value=savedItem
        }

        item.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            edit_itemTitle.setText(item.value?.name ?: resources.getString(R.string.defaultTitle))
            edit_itemPrice.setText(
                item.value?.price.toString() ?: resources.getString(R.string.defaultPrice)
            )
            edit_itemDesc.setText(item.value?.desc ?: resources.getString(R.string.defaultTitle))
            edit_itemCategory.setText(item.value?.category ?: resources.getString(R.string.defaultCategory))
            edit_itemExpiryDate.setText(item.value?.expiryDate ?: resources.getString(R.string.defaultExpire))
            edit_itemLocation.setText(item.value?.location ?: resources.getString(R.string.defaultLocation))
            edit_itemCondition.setText(item.value?.condition ?: resources.getString(R.string.defaultCondition))

            try {
                edit_itemImage.setImageBitmap(item.value?.image?.let {
                        it1 -> ImageUtils.getBitmap(it1, requireContext())
                })
            } catch (e: Exception) {
                Snackbar.make(view, R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
            }

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
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item selection
        return when (item.itemId) {
            R.id.menu_save -> {

                /*if(::currentPhotoPath.isInitialized)
                    item.value = Item(edit_fullname.text.toString(), edit_nickname.text.toString(),
                        edit_mail.text.toString(), edit_location.text.toString(),
                        currentPhotoPath, edit_bio.text.toString(), edit_phoneNumber.text.toString())
                else
                    profile.value = Profile(edit_fullname.text.toString(), edit_nickname.text.toString(),
                        edit_mail.text.toString(), edit_location.text.toString(),
                        profile.value?.image, edit_bio.text.toString(), edit_phoneNumber.text.toString())


                val nRotation = rotationCount.value
                if(::currentPhotoPath.isInitialized){
                    if (nRotation != null) {
                        if(nRotation != 0 && nRotation.rem(4) != 0
                            && ImageUtils.canDisplayBitmap(currentPhotoPath, requireContext())){ // Save the edited photo
                            ImageUtils.rotateImageFromUri(
                                Uri.parse(currentPhotoPath),
                                90.0F* nRotation,
                                requireContext()
                            )?.let {
                                currentPhotoPath = ImageUtils.insertImage(requireActivity().contentResolver,
                                    it
                                ).toString()
                            }
                        }
                    }
                    profile.value!!.image = currentPhotoPath
                }

                val bundle = bundleOf("new_profile" to profile.value?.let { Profile.toJSON(it).toString() })
                view?.findNavController()?.navigate(R.id.action_nav_editProfile_to_nav_showProfile, bundle)
                */
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
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

        /*if (requestCode == REQUEST_CREATEIMAGE && resultCode == AppCompatActivity.RESULT_OK) {
            if(::currentPhotoPath.isInitialized){
                val oldPhoto = currentPhotoPath

                currentPhotoPath = ImageUtils.getBitmap(currentPhotoPath, requireContext())?.let {
                    ImageUtils.insertImage(requireActivity().contentResolver,
                        it
                    )
                }.toString()
                File(oldPhoto).delete()

                edit_showImageProfile.setImageBitmap(ImageUtils.getBitmap(currentPhotoPath, requireContext()))
            }
        }
        else if(requestCode == REQUEST_PICKIMAGE && resultCode == Activity.RESULT_OK) {
            edit_showImageProfile.setImageBitmap(ImageUtils.getBitmap(data?.data.toString(), requireContext()))
            currentPhotoPath = data?.data.toString()
            getPermissionOnUri(Uri.parse(currentPhotoPath))
            rotationCount.value = 0
        }*/
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
                    startActivityForResult(takePictureIntent, REQUEST_CREATEIMAGE)
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
            //currentPhotoPath = absolutePath
        }
    }

    private fun hasExStoragePermission(): Boolean{
        /*return (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permisson.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)*/
        return false;
    }

    private fun checkExStoragePermission(): Boolean{
        /*if (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Not granted

            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user
                val builder = AlertDialog.Builder(requireActivity())

                // Set the alert dialog title
                builder.setTitle(R.string.warning_dialog)

                // Display a message on alert dialog
                builder.setMessage(resources.getString(R.string.permission_expl))

                // Set a positive button and its click listener on alert dialog
                builder.setPositiveButton("Ok"){ dialog, which ->
                    ActivityCompat.requestPermissions(requireActivity(),
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
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
            }
            return false
        } else {
            // Permission has already been granted
            return true
        }*/
        return false;
    }

    private fun getPermissionOnUri(uri:Uri){
        val contentResolver = requireActivity().contentResolver
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, takeFlags)
    }
}