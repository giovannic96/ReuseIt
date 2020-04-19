package it.polito.mad.mhackeroni

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_edit_profile.*

class EditProfileFragment : Fragment() {
    var profile: MutableLiveData<Profile> = MutableLiveData()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileJSON = arguments?.getString("profile", "")
        val savedProfile = savedInstanceState?.getString("profile")?.let { Profile.fromStringJSON(it) }

        if(!profileJSON.isNullOrEmpty()){
            profile.value = Profile.fromStringJSON(profileJSON)
        }

        // Get saved value
        if(savedProfile != null) {
            Log.d("MAD2020", Profile.toJSON(savedProfile).toString())
            profile.value = savedProfile
        }

        profile.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            try {
                edit_imageProfile.setImageBitmap(profile.value?.image?.let {
                    it1 -> ImageUtils.getBitmap(it1, requireContext())
                })
            } catch (e: Exception) {
                Snackbar.make(view, R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
            }

            edit_fullname.setText(profile.value?.fullName ?: resources.getString(R.string.defaultFullName))
            edit_bio.setText(profile.value?.bio ?: resources.getString(R.string.defaultNickname))
            edit_nickname.setText(profile.value?.nickname ?: resources.getString(R.string.defaultNickname))
            edit_mail.setText(profile.value?.email ?: resources.getString(R.string.defaultEmail))
            edit_phoneNumber.setText(profile.value?.phoneNumber ?: resources.getString(R.string.defaultLocation))
            edit_location.setText(profile.value?.location ?: resources.getString(R.string.defaultLocation))
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("profile", profile.value?.let { Profile.toJSON(it).toString() })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item selection
        return when (item.itemId) {
            R.id.menu_save -> {

                var bundle = bundleOf("new_profile" to profile.value?.let { Profile.toJSON(it).toString() })

                view?.findNavController()?.navigate(R.id.action_nav_editProfile_to_nav_showProfile, bundle)

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}