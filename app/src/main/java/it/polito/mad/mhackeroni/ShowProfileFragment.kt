package it.polito.mad.mhackeroni

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.Person.fromBundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.mhackeroni.utilities.ImageUtils
import it.polito.mad.mhackeroni.utilities.StorageHelper
import kotlinx.android.synthetic.main.fragment_show_profile.*


class ShowProfileFragment : Fragment() {
    var profile: MutableLiveData<Profile> = MutableLiveData()
    private lateinit var storageHelper: StorageHelper
    private lateinit var db: FirebaseFirestore
    private var mListener: OnCompleteListener? = null
    private lateinit var vm : OnSaleListFragmentViewModel
    private lateinit var uid: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_show_profile, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        uid = arguments?.getString("uid")!!
        vm = ViewModelProvider(this).get(OnSaleListFragmentViewModel::class.java)
        vm.uid = uid
        storageHelper = StorageHelper(requireContext())

        vm.getProfile().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            profile.value = it
        })

        profile.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            try {
                if(profile.value?.image.isNullOrEmpty()) {
                    imageProfile.setImageResource(R.drawable.ic_avatar)
                } else {
                    imageProfile.setImageBitmap(profile.value?.image?.let { it1 ->
                        ImageUtils.getBitmap(it1, requireContext())
                    })
                }
            } catch (e: Exception) {
                Snackbar.make(view, R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
            }

            fullname.text = profile.value?.fullName ?: resources.getString(R.string.defaultFullName)
            bio.text = profile.value?.bio ?: resources.getString(R.string.defaultBio)
            nickname.text = profile.value?.nickname ?: resources.getString(R.string.defaultNickname)
            mail.text = profile.value?.email ?: resources.getString(R.string.defaultEmail)
            phone_number.text = profile.value?.phoneNumber ?: resources.getString(R.string.defaultPhoneNumber)
            location.text = profile.value?.location ?: resources.getString(R.string.defaultLocation)
        })

        getResult()

        imageProfile.setOnClickListener {
            val bundle=Bundle()
            try {
                if(!profile.value?.image.isNullOrEmpty()) {
                    if (profile.value?.image?.let { it1 ->
                            ImageUtils.canDisplayBitmap(
                                it1,
                                requireContext()
                            )
                        }!!) {

                        bundle.putString("uri", profile.value?.image.toString())
                        view.findNavController()
                            .navigate(R.id.action_nav_showProfile_to_showImageFragment, bundle)
                    }
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
                editProfile()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editProfile() {
        val bundle = Bundle()
        bundle.putString("profile", profile.value?.let { Profile.toJSON(it).toString()})
        bundle.putString("uid", uid)
        view?.findNavController()?.navigate(R.id.action_nav_showProfile_to_nav_editProfile, bundle)
    }

    private fun getResult() {
        val newProfileJSON = arguments?.getString("new_profile", "")
        //val sharedPref:SharedPreferences = requireContext().getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE)
        db = FirebaseFirestore.getInstance()

        val oldProfile = profile.value

        if(!newProfileJSON.isNullOrEmpty() && newProfileJSON != oldProfile?.let { Profile.toJSON(it).toString() }){

            profile.value = newProfileJSON.let { Profile.fromStringJSON(it) }

            val snackbar = view?.let { Snackbar.make(it, getString(R.string.profile_update), Snackbar.LENGTH_LONG) }
            if (snackbar != null) {
                snackbar.setAction(getString(R.string.undo), View.OnClickListener {
                    profile.value = oldProfile
                    if (oldProfile != null) {
                        storageHelper.saveProfile(db, oldProfile)
                        mListener?.onComplete(oldProfile)
                    }
                })
                snackbar.show()
            }
        }
        profile.value?.let { storageHelper.saveProfile(db, it) }
        profile.value?.let { mListener?.onComplete(it) }
        arguments?.clear() // Clear arguments
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as ShowProfileFragment.OnCompleteListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnCompleteListener")
        }
    }

    interface OnCompleteListener {
        fun onComplete(profile: Profile)
    }
}