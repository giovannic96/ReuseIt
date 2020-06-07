package it.polito.mad.mhackeroni.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.model.Profile
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import it.polito.mad.mhackeroni.utilities.ImageUtils
import it.polito.mad.mhackeroni.utilities.Validation
import it.polito.mad.mhackeroni.viewmodel.EditProfileFragmentViewModel
import it.polito.mad.mhackeroni.viewmodel.UserMapViewModel
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_show_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class EditProfileFragment : Fragment() {
    private val REQUEST_PICKIMAGE = 9002
    private val REQUEST_CREATEIMAGE = 9001
    private val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 9002
    private lateinit var currentPhotoPath: String
    private val rotationCount: MutableLiveData<Int> = MutableLiveData()
    private var startCamera = false
    private var originalPhotPath = ""
    private lateinit  var vm : EditProfileFragmentViewModel
    val logger: Logger = Logger.getLogger(EditProfileFragment::class.java.name)
    private var  profile : Profile? = null
    private var oldProfile : Profile? = null
    private var location : String = ""
    private var mapViewModel : UserMapViewModel = UserMapViewModel()
    private var lat: Double? = null
    private var lng: Double? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        activity?.run {
            mapViewModel = ViewModelProviders.of(requireActivity()).get(UserMapViewModel::class.java)
        }
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repo = FirebaseRepo.INSTANCE

        vm = ViewModelProvider(this).get(EditProfileFragmentViewModel::class.java)
        vm.uid = repo.getID(requireContext())

        edit_location.inputType = InputType.TYPE_NULL

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            edit_location.focusable = View.NOT_FOCUSABLE
        } else {
            edit_location.isFocusable = false
        }


        edit_location.setOnClickListener {
            view?.findNavController().navigate(R.id.action_nav_editProfile_to_mapFragment, bundleOf("user" to true))
        }

        vm.getProfile().observe(viewLifecycleOwner, androidx.lifecycle.Observer {profileVal ->
            var profileData = profileVal
            profile = profileVal
            oldProfile = profileVal



            if(vm.getLocalProfile() != null) {
                Log.d("MAD2020", "Saved profile ${Profile.toJSON(vm.getLocalProfile()!!)}")
                profileData = vm.getLocalProfile()
                currentPhotoPath = profileData.image.toString()
            } else {
                if(!profileData.image.isNullOrEmpty()){
                    try {
                        val imagePath: String = profileData.image!!

                        val ref = Firebase.storage.reference
                            .child("profiles_images")
                            .child(imagePath)

                        ref.downloadUrl.addOnCompleteListener {
                            if (it.isSuccessful && it.result != null) {
                                try {
                                    GlobalScope.launch {
                                        ImageUtils.downloadAndSaveImageTask(requireContext(),
                                            it.result.toString()
                                        ).let { if(!it.isNullOrEmpty()) currentPhotoPath = it}
                                    }
                                    rotationCount.value = 0
                                } catch(ex: IllegalStateException) {
                                    logger.log(Level.WARNING, "context not attached", ex)
                                }
                            }
                        }

                    } catch (e : Exception){
                        e.printStackTrace()
                    }
                }
            }

            try {
                if(!profileData.image.isNullOrEmpty()){
                    val imagePath: String = profileData.image!!
                    val ref = Firebase.storage.reference
                        .child("profiles_images")
                        .child(imagePath)

                    ref.downloadUrl.addOnCompleteListener {
                        if (it.isSuccessful) {
                            try {
                                Glide.with(requireContext())
                                    .load(it.result)
                                    .into(edit_showImageProfile)
                            } catch(ex: IllegalStateException) {
                                logger.log(Level.WARNING, "context not attached", ex)
                            }
                        } else {
                            try {
                                Glide.with(requireContext())
                                    .load(imagePath)
                                    .into(edit_showImageProfile)
                            } catch(ex: IllegalStateException) {
                                logger.log(Level.WARNING, "context not attached", ex)
                            }
                        }
                    }
                } else {
                    edit_showImageProfile.setImageResource(R.drawable.ic_avatar)
                }
            } catch (e: Exception) {
                Snackbar.make(view,R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
            }

            edit_fullname.setText(profileData.fullName)
            edit_bio.setText(profileData.bio)
            edit_nickname.setText(profileData.nickname)
            edit_mail.setText(profileData.email)
            edit_phoneNumber.setText(profileData.phoneNumber)

            if(location.isNullOrEmpty())
                edit_location.setText(profileData.location)
            else
                edit_location.setText(location)

        })

        rotationCount.observe(requireActivity(), androidx.lifecycle.Observer {
            val deg: Float = 90f * it
            if(edit_showImageProfile != null)
                edit_showImageProfile.animate().rotation(deg).interpolator =
                    AccelerateDecelerateInterpolator()
        })

        setupValidationListener()

        edit_imageProfile.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), edit_imageProfile)
            popupMenu.menuInflater.inflate(R.menu.context_menu_image, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.fromCamera -> {
                        startCamera = true
                        dispatchTakePictureIntent()
                    }
                    R.id.fromGallery -> {
                        startCamera = false
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

        btn_rotate_image.setOnClickListener {
            if(::currentPhotoPath.isInitialized
                && ImageUtils.canDisplayBitmap(currentPhotoPath, requireContext())
                && hasExStoragePermission()){
                rotationCount.value = rotationCount.value?.plus(1)
            } else if(!hasExStoragePermission()) {
                checkExStoragePermission()
            } else{
                Snackbar
                    .make(edit_main_container, resources.getString(R.string.rotate_error), Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        mapViewModel.position.observe(viewLifecycleOwner, androidx.lifecycle.Observer { position ->

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

                lat = position.latitude
                lng = position.longitude

            } catch (e: java.lang.IllegalStateException){
                Snackbar.make(view, getString(R.string.locationError), Snackbar.LENGTH_SHORT).show()
            } catch (e: Exception){
                Snackbar.make(view, getString(R.string.networkerror), Snackbar.LENGTH_SHORT)
                    .show()
            }
        })
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if(::currentPhotoPath.isInitialized)
            profile =
                profile?.totRating?.let {
                    profile?.numRating?.let { it1 ->
                        profile?.feedbacks?.let { it2 ->
                            Profile(
                                edit_fullname.text.toString(), edit_nickname.text.toString(),
                                edit_mail.text.toString(), edit_location.text.toString(),
                                currentPhotoPath, edit_bio.text.toString(), edit_phoneNumber.text.toString(),
                                it, it1, it2
                            )
                        }
                    }
                }
        else
            profile =
                profile?.totRating?.let {
                    profile?.numRating?.let { it1 ->
                        profile?.feedbacks?.let { it2 ->
                            Profile(
                                edit_fullname.text.toString(), edit_nickname.text.toString(),
                                edit_mail.text.toString(), edit_location.text.toString(),
                                profile?.image, edit_bio.text.toString(), edit_phoneNumber.text.toString(),
                                it, it1, it2
                            )
                        }
                    }
                }

        if(profile?.image.isNullOrEmpty() )
            profile?.image = ""


        Log.d("MAD2020", "Saving profile: ${vm.getLocalProfile()}")

        outState.putString("profile", vm.getLocalProfile()?.let { Profile.toJSON(
            it
        ).toString() })
        rotationCount.value?.let { outState.putInt("rotation", it) }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val savedProfileJSON = savedInstanceState?.getString("profile") ?: ""

        val savedProfile =
            Profile.fromStringJSON(
                savedProfileJSON
            )

        if(savedProfile != null){
            Log.d("MG", savedProfileJSON)
            vm.updateProfile(savedProfile)
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

                if(!checkData())
                    return false

                editprofile_progress_bar.visibility = View.VISIBLE

                val firebaseRepo = FirebaseRepo.INSTANCE

                if(::currentPhotoPath.isInitialized) {
                    profile = profile?.totRating?.let {
                        profile?.numRating?.let { it1 ->
                            profile?.feedbacks?.let { it2 ->
                                Profile(
                                    edit_fullname.text.toString().trim(),
                                    edit_nickname.text.toString(),
                                    edit_mail.text.toString(),
                                    edit_location.text.toString(),
                                    currentPhotoPath,
                                    edit_bio.text.toString(),
                                    edit_phoneNumber.text.toString(),
                                    it, it1, it2,
                                    lat = lat,
                                    lng = lng
                                )
                            }
                        }
                    }
                    vm.updateProfile(profile!!)
                }else {
                    profile = profile?.totRating?.let {
                        profile?.numRating?.let { it1 ->
                            profile?.feedbacks?.let { it2 ->
                                Profile(
                                    edit_fullname.text.toString().trim(),
                                    edit_nickname.text.toString(),
                                    edit_mail.text.toString(),
                                    edit_location.text.toString(),
                                    profile?.image,
                                    edit_bio.text.toString(),
                                    edit_phoneNumber.text.toString(),
                                    it, it1, it2,
                                    lat = lat,
                                    lng = lng
                                )
                            }
                        }
                    }
                    vm.updateProfile(profile!!)
                }

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
                    profile!!.image = currentPhotoPath
                }

                val repo : FirebaseRepo = FirebaseRepo.INSTANCE

                repo.checkNickname(edit_nickname.text.toString()).addOnCompleteListener {
                    if(it.isSuccessful) {
                        var idDoc = ""
                        var canUpdateDb = false
                        if(!it.result?.isEmpty!!) {
                            for (document in it.result!!) {
                                idDoc = document.id
                                break
                            }
                        } else {
                            canUpdateDb = true
                        }

                        //if username not exists OR it exists but it's mine -> can edit!
                        if(canUpdateDb || idDoc == repo.getID(requireContext())) {
                            if(vm.getLocalProfile() != null) {
                                vm.updateDB().addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        val bundle = bundleOf("old_profile" to oldProfile?.let { it1 ->
                                            Profile.toJSON(it1).toString()
                                        },
                                            "new_profile" to profile?.let {
                                                Profile.toJSON(it).toString()
                                            }
                                        )
                                        view?.findNavController()
                                            ?.navigate(R.id.action_nav_editProfile_to_nav_showProfile, bundle)
                                    } else {
                                        val bundle = bundleOf("error" to true,
                                            "new_profile" to profile?.let {
                                                Profile.toJSON(it).toString()
                                            }
                                        )
                                        view?.findNavController()
                                            ?.navigate(R.id.action_nav_editProfile_to_nav_showProfile, bundle)
                                    }
                                }
                            } else {
                                val bundle = bundleOf("old_profile" to oldProfile?.let { it1 ->
                                    Profile.toJSON(it1).toString()
                                },
                                    "new_profile" to profile?.let {
                                        Profile.toJSON(it).toString()
                                    }
                                )
                                view?.findNavController()
                                    ?.navigate(R.id.action_nav_editProfile_to_nav_showProfile, bundle)
                            }
                        } else {
                            editprofile_progress_bar.visibility = View.INVISIBLE
                            edit_nickname.setError(getString(R.string.nickname_already_exist))
                        }
                    }
                }
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
                    if(startCamera)
                        dispatchTakePictureIntent()
                } else {
                    Snackbar.make(edit_main_container, resources.getString(R.string.permission_err), Snackbar.LENGTH_SHORT)
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
            if(::currentPhotoPath.isInitialized){
                val oldPhoto = currentPhotoPath

                currentPhotoPath = ImageUtils.getBitmap(currentPhotoPath, requireContext())?.let {
                    ImageUtils.insertImage(requireActivity().contentResolver,
                        it
                    )
                }.toString()
                File(oldPhoto).delete()

                val drawable = ImageUtils.getBitmap(currentPhotoPath, requireContext())
                if(drawable != null)
                    edit_showImageProfile.setImageBitmap(drawable)
                else
                    edit_showImageProfile.setImageResource(R.drawable.ic_avatar)

                rotationCount.value = 0
            }
        }
        else if(requestCode == REQUEST_PICKIMAGE && resultCode == Activity.RESULT_OK) {
            val drawable = ImageUtils.getBitmap(data?.data.toString(), requireContext())
            if(drawable != null)
                edit_showImageProfile.setImageBitmap(drawable)
            else
                edit_showImageProfile.setImageResource(R.drawable.ic_avatar)

            currentPhotoPath = data?.data.toString()
            getPermissionOnUri(Uri.parse(currentPhotoPath))
            rotationCount.value = 0
        } else if((requestCode == REQUEST_CREATEIMAGE || requestCode == REQUEST_PICKIMAGE) && resultCode == AppCompatActivity.RESULT_CANCELED){
            currentPhotoPath = originalPhotPath ?: ""
            profile?.image = originalPhotPath

            if(originalPhotPath.isEmpty()){
                edit_showImageProfile.setImageResource(R.drawable.ic_avatar)
            }

        } else {
            currentPhotoPath = originalPhotPath ?: ""
            profile?.image = originalPhotPath


            if(originalPhotPath.isEmpty()){
                edit_showImageProfile.setImageResource(R.drawable.ic_avatar)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Used to solve lazy update issue
        if(::currentPhotoPath.isInitialized && !currentPhotoPath.isEmpty()){
            edit_showImageProfile.setImageBitmap(ImageUtils.getBitmap(currentPhotoPath, requireContext()))
        }

        //(re)set editLineColor when rotate device, according to current backgroundTintList of editText
        val detectColor = { v:TextView ->
            if(v.hasFocus())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    v.backgroundTintList
                } else {
                    ColorStateList.valueOf(Color.GRAY)
                }
            else
                ColorStateList.valueOf(Color.GRAY)
        }
        ViewCompat.setBackgroundTintList(edit_nickname, detectColor(edit_nickname))
        ViewCompat.setBackgroundTintList(edit_mail, detectColor(edit_mail))
        ViewCompat.setBackgroundTintList(edit_phoneNumber, detectColor(edit_phoneNumber))
        ViewCompat.setBackgroundTintList(edit_location, detectColor(edit_location))
    }

    private fun checkData() : Boolean {
        var retval = true

        // if at least one field has an error -> return false
        if(!edit_fullname.error.isNullOrEmpty() || !edit_mail.error.isNullOrEmpty() || !edit_nickname.error.isNullOrEmpty()
            || !edit_location.error.isNullOrEmpty() || !edit_phoneNumber.error.isNullOrEmpty() || !edit_bio.error.isNullOrEmpty()) {
            return false
        }

        // validate mandatory fields
        if(!Validation.isValidLocation(edit_location.text)) {
            edit_location.setError(getString(R.string.location_error))
            retval = false
        }

        if(!Validation.isValidNickname(edit_nickname.text)) {
            edit_nickname.setError(getString(R.string.nickname_error))
            retval = false
        }

        if(!Validation.isValidEmail(edit_mail.text)) {
            edit_mail.setError(getString(R.string.mail_error))
            retval = false
        }

        // if some fields are blank or null -> return false
        if(edit_fullname.text.isNullOrBlank()) {
            edit_fullname.setError(getString(R.string.required_field))
            retval = false
        }

        if(edit_mail.text.isNullOrBlank()) {
            edit_mail.setError(getString(R.string.required_field))
            retval = false
        }

        if(edit_nickname.text.isNullOrBlank()) {
            edit_nickname.setError(getString(R.string.required_field))
            retval = false
        }

        if(edit_location.text.isNullOrBlank()) {
            edit_location.setError(getString(R.string.required_field))
            retval = false
        }

        // validate optional fields
        if(!edit_phoneNumber.text.isNullOrBlank()) {
            if(!Validation.isValidPhoneNumber(edit_phoneNumber.text)) {
                edit_phoneNumber.setError(getString(R.string.phoneNumber_error))
                retval = false
            }
        }

        return retval
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
            currentPhotoPath = absolutePath
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
                val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())

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

                val dialog: androidx.appcompat.app.AlertDialog = builder.create()
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

    private fun setupValidationListener() {
        edit_mail.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                val c:Int = checkColor(s.toString(), Validation.isValidEmail)
                changeEditViewLineColor(edit_mail, c)
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        edit_mail.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            val s:String = edit_mail.text.toString()
            val c:Int

            c = checkColor(s, Validation.isValidEmail)

            if (!hasFocus) {
                changeEditViewLineColor(edit_mail, Color.GRAY)
                if(s.isEmpty() || !Validation.isValidEmail(s))
                    edit_mail.error = resources.getString(R.string.mail_error)
            } else {
                changeEditViewLineColor(edit_mail, c)
            }
        }

        edit_nickname.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                val c:Int = checkColor(s.toString(), Validation.isValidNickname)
                changeEditViewLineColor(edit_nickname, c)
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        edit_nickname.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            val s:String = edit_nickname.text.toString()
            val c:Int

            c = checkColor(s, Validation.isValidNickname)

            if (!hasFocus) {
                changeEditViewLineColor(edit_nickname, Color.GRAY)
                if(s.isEmpty() || !Validation.isValidNickname(s))
                    edit_nickname.error = resources.getString(R.string.nickname_error)
            } else {
                changeEditViewLineColor(edit_nickname, c)
            }
        }

        edit_phoneNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(s.isNullOrEmpty()) {
                    val defaultColor:Int = ContextCompat.getColor(requireContext(),
                        R.color.colorAccent
                    )
                    changeEditViewLineColor(edit_phoneNumber, defaultColor)
                } else {
                    val c:Int = checkColor(s.toString(), Validation.isValidPhoneNumber)
                    changeEditViewLineColor(edit_phoneNumber, c)
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        edit_phoneNumber.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            val s:String = edit_phoneNumber.text.toString()
            val c:Int
            val defaultColor:Int = ContextCompat.getColor(requireContext(),
                R.color.colorAccent
            )

            c = checkColor(s, Validation.isValidPhoneNumber)

            if (!hasFocus) {
                changeEditViewLineColor(edit_phoneNumber, Color.GRAY)
                if(s.isNotEmpty() && !Validation.isValidPhoneNumber(s)) {
                    edit_phoneNumber.error = resources.getString(R.string.phoneNumber_error)
                }
            } else {
                if(s.isEmpty()) {
                    changeEditViewLineColor(edit_phoneNumber, defaultColor)
                }
                else {
                    changeEditViewLineColor(edit_phoneNumber, c)
                }
            }
        }

        edit_location.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                val c:Int = checkColor(s.toString(), Validation.isValidLocation)
                changeEditViewLineColor(edit_location, c)
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        edit_location.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            val s:String = edit_location.text.toString()
            val c:Int

            c = checkColor(s, Validation.isValidLocation)

            if (!hasFocus) {
                changeEditViewLineColor(edit_location, Color.GRAY)
                if(s.isEmpty() || !Validation.isValidLocation(s))
                    edit_location.error = resources.getString(R.string.location_error)
            } else {
                changeEditViewLineColor(edit_location, c)
            }
        }
    }

    private fun checkColor(s:String?, f: (CharSequence?) -> Boolean): Int { //HOF
        return if(s.isNullOrEmpty() || !f(s)) {
            Color.RED
        } else {
            ContextCompat.getColor(requireContext(),
                R.color.colorAccent
            )
        }
    }

    private fun changeEditViewLineColor(editText: TextView, color:Int) {
        val colorStateList = ColorStateList.valueOf(color)
        ViewCompat.setBackgroundTintList(editText, colorStateList)
    }
}

