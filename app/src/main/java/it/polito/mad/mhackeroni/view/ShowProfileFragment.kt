package it.polito.mad.mhackeroni.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.mhackeroni.model.Profile
import it.polito.mad.mhackeroni.viewmodel.ProfileFragmentViewModel
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import kotlinx.android.synthetic.main.fragment_show_profile.*


class ShowProfileFragment : Fragment() {
    private var mListener: OnCompleteListener? = null
    private lateinit var vm : ProfileFragmentViewModel
    private var canEdit = true
    private var profile : Profile =
        Profile()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_show_profile, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lateinit var uid : String

        var passingUID= arguments?.getString(getString(R.string.uid), "") ?: ""
        getNavigationInfo()
        arguments?.clear()

        vm = ViewModelProvider(this).get(ProfileFragmentViewModel::class.java)

        // Show personal profile
        if(passingUID.isNullOrEmpty() || passingUID.equals("null") && vm.uid.isEmpty()) {
            val repo = FirebaseRepo.INSTANCE
            uid = repo.getID(requireContext())
        } else { // Show another profile
            uid = passingUID

            canEdit = false
            requireActivity().invalidateOptionsMenu()
        }


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
                        if (it.isSuccessful) {
                            Glide.with(requireContext())
                                .load(it.result)
                                .into(imageProfile)
                        }

                        profile_progress_bar.visibility = View.INVISIBLE
                     }
                }
            } catch (e: Exception) {
                Snackbar.make(view,
                    R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
            }

            fullname.text = it?.fullName ?: resources.getString(R.string.defaultFullName)
            bio.text = it?.bio ?: resources.getString(R.string.defaultBio)
            nickname.text = it?.nickname ?: resources.getString(R.string.defaultNickname)
            mail.text = it?.email ?: resources.getString(R.string.defaultEmail)
            phone_number.text = it?.phoneNumber ?: resources.getString(R.string.defaultPhoneNumber)
            location.text = it?.location ?: resources.getString(R.string.defaultLocation)
        })

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
        bundle.putString("profile", profile.let { Profile.toJSON(
            it
        ).toString()})
        view?.findNavController()?.navigate(R.id.action_nav_showProfile_to_nav_editProfile, bundle)
    }

    private fun getNavigationInfo(){
        val oldProfileJSON = arguments?.getString("old_profile") ?: ""
        val newProfileJSON = arguments?.getString("new_profile") ?: ""

        if(!newProfileJSON.isEmpty() && !oldProfileJSON.isEmpty() && oldProfileJSON != newProfileJSON){
            val snackbar = view?.let { Snackbar.make(it, getString(R.string.profile_update), Snackbar.LENGTH_LONG) }
            if (snackbar != null) {
                snackbar.setAction(getString(R.string.undo), View.OnClickListener {

                    val repo : FirebaseRepo = FirebaseRepo.INSTANCE
                    val prevProfile = Profile.fromStringJSON(
                        oldProfileJSON
                    )!!

                    if(prevProfile != null)
                        repo.updateProfile(prevProfile, repo.getID(requireContext()))

                })
                snackbar.show()
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
}