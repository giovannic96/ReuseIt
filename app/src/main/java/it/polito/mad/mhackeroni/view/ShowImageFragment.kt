package it.polito.mad.mhackeroni.view

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.mhackeroni.R
import kotlinx.android.synthetic.main.fragment_item_edit.*
import kotlinx.android.synthetic.main.fragment_show_image.*
import java.lang.IllegalStateException
import java.util.logging.Level
import java.util.logging.Logger


class ShowImageFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        load_image_progessbar.visibility = View.VISIBLE

        val profilePic = arguments?.getBoolean("profile_image", false) ?: false

        if(profilePic) {
            val uri = arguments?.getString("uri")
            val ref = uri?.let {
                Firebase.storage.reference
                    .child("profiles_images")
                    .child(it)
            }

            ref?.downloadUrl?.addOnCompleteListener {
                if(it.isSuccessful) {
                    try {
                        context?.let { it1 ->
                            Glide.with(it1)
                                .load(it.result)
                                .into(imageFullscreen)
                        }
                    } catch (ex: IllegalStateException) {
                        val logger: Logger = Logger.getLogger(ShowImageFragment::class.java.name)
                        logger.log(Level.WARNING, "context not attached", ex)
                    }
                }
                load_image_progessbar?.visibility = View.INVISIBLE
            }
        } else {
            val uri = arguments?.getString("uri")
            try {
                Glide.with(requireContext())
                    .load(uri)
                    .into(imageFullscreen)
            } catch (ex: IllegalStateException) {
                val logger: Logger = Logger.getLogger(ShowImageFragment::class.java.name)
                logger.log(Level.WARNING, "context not attached", ex)
            }
            load_image_progessbar?.visibility = View.INVISIBLE
        }


    }
}