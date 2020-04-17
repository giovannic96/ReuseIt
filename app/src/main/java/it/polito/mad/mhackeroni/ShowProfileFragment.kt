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


class ShowProfileFragment : Fragment(){
    var profile: MutableLiveData<Profile> = MutableLiveData()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_show_profile, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPref:SharedPreferences = requireContext().getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE)

        loadData(sharedPref)

        profile.observe(this, androidx.lifecycle.Observer {
            try {
                imageProfile.setImageBitmap(profile.value?.image?.let {
                    it1 -> ImageUtils.getBitmap(it1, context!!)
                })
            } catch (e: Exception) {
                Snackbar.make(view, R.string.image_not_found, Snackbar.LENGTH_SHORT).show()
            }

            fullname.text = profile.value?.fullName ?: resources.getString(R.string.defaultFullName)
            bio.text = profile.value?.bio ?: resources.getString(R.string.defaultNickname)
            nickname.text = profile.value?.nickname ?: resources.getString(R.string.defaultNickname)
            mail.text = profile.value?.email ?: resources.getString(R.string.defaultEmail)
            phone_number.text = profile.value?.phoneNumber ?: resources.getString(R.string.defaultLocation)
            location.text = profile.value?.location ?: resources.getString(R.string.defaultLocation)
        })
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

    fun editProfile(){
        val bundle = Bundle()
        bundle.putString("profile", profile.value?.let { Profile.toJSON(it).toString()})

        view?.findNavController()?.navigate(R.id.showToEdit, bundle)

    }

    private fun saveData(s: SharedPreferences, p:Profile) {
        with (s.edit()) {
            putString(getString(it.polito.mad.mhackeroni.R.string.profile_sharedPref), Profile.toJSON(p).toString())
            apply()
        }
    }

    private fun loadData(s: SharedPreferences){
        val JSONString : String? = s.getString(getString(R.string.profile_sharedPref), "")
        profile.value = JSONString?.let { Profile.fromStringJSON(it) }
    }


}