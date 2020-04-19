package it.polito.mad.mhackeroni

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment

class ItemDetailsFragment: Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item selection
        return when (item.itemId) {
            R.id.menu_edit -> {
                editItem()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun editItem(){
        //TODO with item
        //bundleOf("profile" to profile.value?.let { Profile.toJSON(it).toString() })
       /*
        fragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_placeholder, ItemEditFragment())
            ?.commit()
        */

    }
}