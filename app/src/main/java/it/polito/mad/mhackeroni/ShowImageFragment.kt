package it.polito.mad.mhackeroni

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_show_image.*


class ShowImageFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        load_image_progessbar.visibility = View.VISIBLE

        val profilePic = arguments?.getBoolean("profile_image", false) ?: false

        if(profilePic){
            val uri = arguments?.getString("uri")
            val ref = uri?.let {
                Firebase.storage.reference
                    .child("profiles_images")
                    .child(it)
            }

            if (ref != null) {
                ref.downloadUrl.addOnCompleteListener {
                    if(it.isSuccessful) {
                        context?.let { it1 ->
                            Glide.with(it1)
                                .load(it.result)
                                .into(imageFullscreen)
                        }
                    }
                    load_image_progessbar.visibility = View.INVISIBLE
                }
            }
        } else {
            val uri = arguments?.getString("uri")
            val ref = uri?.let {
                Firebase.storage.reference
                    .child("items_images")
                    .child(it)
            }

            if (ref != null) {
                ref.downloadUrl.addOnCompleteListener {
                    if(it.isSuccessful) {
                        context?.let { it1 ->
                            Glide.with(it1)
                                .load(it.result)
                                .into(imageFullscreen)
                        }
                    }
                    load_image_progessbar.visibility = View.INVISIBLE
                }
            }
        }


    }
}