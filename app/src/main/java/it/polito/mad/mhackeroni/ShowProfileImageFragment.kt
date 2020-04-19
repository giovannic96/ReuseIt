package it.polito.mad.mhackeroni

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_show_image.*
import kotlinx.android.synthetic.main.fragment_show_profile.*


class ShowProfileImageFragment : Fragment() {
    var profile: MutableLiveData<Profile> = MutableLiveData()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uri = arguments?.getString("uri")
        imageFullscreen.setImageURI(Uri.parse(uri))

    }
}