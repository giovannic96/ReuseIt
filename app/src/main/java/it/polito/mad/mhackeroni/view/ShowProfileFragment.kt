package it.polito.mad.mhackeroni.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ListView
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.adapters.FeedbackAdapter
import it.polito.mad.mhackeroni.model.Profile
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import it.polito.mad.mhackeroni.viewmodel.ProfileFragmentViewModel
import kotlinx.android.synthetic.main.fragment_show_profile.*
import java.util.logging.Level
import java.util.logging.Logger


class ShowProfileFragment : Fragment(), OnMapReadyCallback {
    private var mListener: OnCompleteListener? = null
    private lateinit var vm : ProfileFragmentViewModel
    private var canEdit = true
    val logger: Logger = Logger.getLogger(ShowProfileFragment::class.java.name)
    private var profile : Profile = Profile()
    private var googleMap : GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_show_profile, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lateinit var uid : String

        val mapFragment = childFragmentManager.findFragmentById(R.id.user_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        val passingUID= arguments?.getString(getString(R.string.uid), "") ?: ""
        val fromItem: Boolean = arguments?.getBoolean("fromItem", false) ?: false
        val repo = FirebaseRepo.INSTANCE
        val appUserID = repo.getID(requireContext())

        getNavigationInfo()
        arguments?.clear()

        vm = ViewModelProvider(this).get(ProfileFragmentViewModel::class.java)

        if((!passingUID.isNullOrEmpty() && passingUID != "null") && appUserID != passingUID){
            uid = passingUID
            canEdit = false
        } else if( !vm.uid.isNullOrEmpty() && vm.uid != appUserID){
            uid = passingUID
            canEdit = false
        } else {
            uid = appUserID
            vm.uid = uid
        }

        if(!canEdit){
            requireActivity().invalidateOptionsMenu()
            if(!fromItem){
                phone_number.visibility = View.GONE
                mail.visibility = View.GONE
            }
            else{
                phone_number.visibility = View.VISIBLE
                mail.visibility = View.VISIBLE
            }
        }

        // Show personal profile
        /*
        if((passingUID.isNullOrEmpty() || passingUID.equals("null")) && vm.) {
            val repo = FirebaseRepo.INSTANCE
            uid = repo.getID(requireContext())
        } else { // Show another profile
            uid = passingUID
            canEdit = false

            requireActivity().invalidateOptionsMenu()
            if(!fromItem){
                phone_number.visibility = View.GONE
                mail.visibility = View.GONE
            }
            else{
                phone_number.visibility = View.VISIBLE
                mail.visibility = View.VISIBLE
            }
        }
         */


        if(vm.uid.isNullOrEmpty())
            vm.uid = uid

        vm.getProfile().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            profile = it
            try {
                if(it.image.isNullOrEmpty()) {
                    imageProfile.setImageResource(R.drawable.ic_avatar)
                } else {

                    profile_progress_bar.visibility = View.VISIBLE
                    val imagePath: String = it.image!!

                    val ref = Firebase.storage.reference
                        .child("profiles_images")
                        .child(imagePath)

                    ref.downloadUrl.addOnCompleteListener {
                        if (it.isSuccessful && it.result != null) {
                            try {
                                Glide.with(requireContext())
                                    .load(it.result)
                                    .into(imageProfile)
                            } catch(ex: IllegalStateException) {
                                logger.log(Level.WARNING, "context not attached", ex)
                            }
                        }
                        profile_progress_bar?.visibility = View.INVISIBLE
                     }
                }
            } catch (e: Exception) {
                Snackbar.make(view,R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
            }

            if(it?.numRating == 0.0 || it?.totRating == 0.0){
                imageStar1.background = resources.getDrawable(R.drawable.ic_emptystar)
                imageStar2.background = resources.getDrawable(R.drawable.ic_emptystar)
                imageStar3.background = resources.getDrawable(R.drawable.ic_emptystar)
                imageStar4.background = resources.getDrawable(R.drawable.ic_emptystar)
                imageStar5.background = resources.getDrawable(R.drawable.ic_emptystar)
                rating.text = "(0)"
            }
            else{
                var ratingDiv: Double = it?.totRating?.div(it?.numRating!!) ?: 0.0

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

            fullname.text = it?.fullName ?: resources.getString(R.string.defaultFullName)
            bio.text = it?.bio ?: resources.getString(R.string.defaultBio)
            nickname.text = it?.nickname ?: resources.getString(R.string.defaultNickname)
            mail.text = it?.email ?: resources.getString(R.string.defaultEmail)
            phone_number.text = it?.phoneNumber ?: resources.getString(R.string.defaultPhoneNumber)
            location.text = it?.location ?: resources.getString(R.string.defaultLocation)


            if(it.lat != null && it.lng != null){
                val pos = LatLng(it.lat!!, it.lng!!)

                if(googleMap != null) {
                    googleMap!!.addMarker(
                        MarkerOptions().position(pos)
                    )
                    googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 5.0f))
                }
            }
        })

        val scroll: ScrollView = view.findViewById(R.id.detailProfile_scrollview)
        val transparent: ImageView = view.findViewById(R.id.imagetransparent)
        val transparent2: ImageView = view.findViewById(R.id.imagetransparent2)

        transparent.setOnTouchListener { _, event ->
            when (event.action) {
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

        transparent2.setOnTouchListener { v, event ->
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

        ratingLayout.setOnClickListener {
            if(profile.numRating!=0.0) {
                if(profile.feedbacks.size == 0)
                    Snackbar.make(view,R.string.noComment, Snackbar.LENGTH_SHORT).show()
                else{
                    showCommentsDialog(profile)
                }
            }
            else{
                Snackbar.make(view,R.string.noRating, Snackbar.LENGTH_SHORT).show()
            }
        }

        imageProfile.setOnClickListener {
            val bundle=Bundle()
            try {
                if(!profile.image.isNullOrEmpty()) {
                    bundle.putString("uri", profile.image.toString())
                    bundle.putBoolean("profile_image", true)

                    view.findNavController()
                        .navigate(R.id.action_nav_showProfile_to_showImageFragment, bundle)
                }
            } catch (e: Exception) {
                Snackbar.make(view,
                    R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if(canEdit)
            inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item selection
        return when (item.itemId) {
            R.id.menu_edit -> {
                editProfile()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editProfile() {
        val bundle = Bundle()
        bundle.putString("profile", profile.let { Profile.toJSON(it).toString()})
        try {
            view?.findNavController()?.navigate(R.id.action_nav_showProfile_to_nav_editProfile, bundle)
        } catch (ex: IllegalArgumentException) {
            logger.log(Level.WARNING, "multiple navigation not allowed", ex)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    private fun getNavigationInfo(){
        val oldProfileJSON = arguments?.getString("old_profile") ?: ""
        val newProfileJSON = arguments?.getString("new_profile") ?: ""

        if(!newProfileJSON.isEmpty() && !oldProfileJSON.isEmpty() && oldProfileJSON != newProfileJSON){
            val snackbar = view?.let { Snackbar.make(it, getString(R.string.profile_update), Snackbar.LENGTH_LONG) }
            if (snackbar != null) {
                /*
                snackbar.setAction(getString(R.string.undo), View.OnClickListener {

                    val repo : FirebaseRepo = FirebaseRepo.INSTANCE
                    val prevProfile = Profile.fromStringJSON(
                        oldProfileJSON
                    )!!

                    if(prevProfile != null)
                        repo.updateProfile(prevProfile, repo.getID(requireContext()))
                })
                */
                snackbar.show()
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if(vm != null && !vm.uid.isNullOrEmpty()) {
            val image = vm.getProfile().value?.image

            if(!image.isNullOrEmpty()) {
                profile_progress_bar.visibility = View.VISIBLE

                val ref = Firebase.storage.reference
                    .child("profiles_images")
                    .child(image)

                ref.downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        try {
                            Glide.with(requireContext())
                                .load(it.result)
                                .into(imageProfile)
                        } catch (ex: IllegalStateException) {
                            logger.log(Level.WARNING, "context not attached", ex)
                        }
                    }
                    profile_progress_bar?.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as OnCompleteListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnCompleteListener")
        }
    }

    interface OnCompleteListener {
        fun onComplete(profile: Profile)
    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map
    }

    private fun showCommentsDialog(profile: Profile) {
        val dialog = Dialog(requireActivity())

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.comments_dialog_box)

        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        var commentList: ListView = dialog.findViewById<ListView>(R.id.commentList)
        var comments: ArrayList<String> = profile.feedbacks

        val adapter: FeedbackAdapter<String> = FeedbackAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            comments
        )

        commentList.adapter = adapter
        commentList
        adapter.notifyDataSetChanged()


        dialog.show()
    }
}