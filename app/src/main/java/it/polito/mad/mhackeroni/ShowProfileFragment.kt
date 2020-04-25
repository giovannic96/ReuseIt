package it.polito.mad.mhackeroni

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_show_profile.*


class ShowProfileFragment : Fragment() {
    var profile: MutableLiveData<Profile> = MutableLiveData()
    private lateinit var storageHelper:StorageHelper
    private var mListener: OnCompleteListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_show_profile, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPref:SharedPreferences = requireContext().getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE)

        storageHelper = StorageHelper(requireContext())
        profile.value = storageHelper.loadProfile(sharedPref)

        profile.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            try {
                imageProfile.setImageBitmap(profile.value?.image?.let {
                    it1 -> ImageUtils.getBitmap(it1, requireContext())
                })
            } catch (e: Exception) {
                Snackbar.make(view, R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
            }

            fullname.text = profile.value?.fullName ?: resources.getString(R.string.defaultFullName)
            bio.text = profile.value?.bio ?: resources.getString(R.string.defaultBio)
            nickname.text = profile.value?.nickname ?: resources.getString(R.string.defaultNickname)
            mail.text = profile.value?.email ?: resources.getString(R.string.defaultEmail)
            phone_number.text = profile.value?.phoneNumber ?: resources.getString(R.string.defaultLocation)
            location.text = profile.value?.location ?: resources.getString(R.string.defaultLocation)
        })

        getResult()

        imageProfile.setOnClickListener {
            val bundle=Bundle()
            try {
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
        view?.findNavController()?.navigate(R.id.action_nav_showProfile_to_nav_editProfile, bundle)
    }

    private fun getResult() {
        val newProfileJSON = arguments?.getString("new_profile", "")
        val sharedPref:SharedPreferences = requireContext().getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE)

        val oldProfile = profile.value

        if(!newProfileJSON.isNullOrEmpty() && newProfileJSON != oldProfile?.let { Profile.toJSON(it).toString() }){

            profile.value = newProfileJSON.let { Profile.fromStringJSON(it) }

            val snackbar = view?.let { Snackbar.make(it, getString(R.string.profile_update), Snackbar.LENGTH_LONG) }
            if (snackbar != null) {
                snackbar.setAction(getString(R.string.undo), View.OnClickListener {
                    profile.value = oldProfile
                    if (oldProfile != null) {
                        storageHelper.saveProfile(sharedPref, oldProfile)
                        mListener?.onComplete()
                    }
                })
                snackbar.show()
            }
        }
        profile.value?.let { storageHelper.saveProfile(sharedPref, it) }
        mListener?.onComplete()
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
        fun onComplete()
    }
}