package it.polito.mad.mhackeroni

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import kotlinx.android.synthetic.main.fragment_item_details.*


class ItemDetailsFragment: Fragment(){
    var item: MutableLiveData<Item> = MutableLiveData()
    private var mListener: ShowProfileFragment.OnCompleteListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_item_details, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //val sharedPref:SharedPreferences = requireContext().getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE)

        val itemJSON=arguments?.getString("item")
        val jsonObject=JSONObject(itemJSON)
        val name: String = jsonObject.getString("name")
        val price: Double = jsonObject.getDouble("price")

        itemTitle.text = name
        itemPrice.text = "$price €"

        //getResult()

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
        val bundle = Bundle()
        bundleOf("item" to item.value?.let { Item.toJSON(it).toString() })

        view?.findNavController()?.navigate(R.id.action_nav_ItemDetail_to_nav_ItemDetailEdit, bundle)
    }

    fun getResult(){
        val newItemJSON = arguments?.getString("new_item", "")
        val sharedPref: SharedPreferences= requireContext().getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE)
        val oldItem = item.value

        if(!newItemJSON.isNullOrEmpty() && newItemJSON != oldItem?.let { Item.toJSON(it).toString() }){

            item.value = newItemJSON.let { Item.fromStringJSON(it) }

            val snackbar = view?.let { Snackbar.make(it, getString(R.string.item_update), Snackbar.LENGTH_LONG) }
            if (snackbar != null) {
                snackbar.setAction(getString(R.string.undo), View.OnClickListener {
                    item.value = oldItem
                    if (oldItem != null) {
                        saveData(sharedPref, oldItem)
                    }
                })

                snackbar.show()
            }
        }

        item.value?.let { saveData(sharedPref, it) }

        arguments?.clear() // Clear arguments
    }

    private fun saveData(s: SharedPreferences, p:Item) {
        with (s.edit()) {
            putString(getString(it.polito.mad.mhackeroni.R.string.item_sharedPref), Item.toJSON(p).toString())
            apply()
        }
        mListener?.onComplete();
    }

    private fun loadData(s: SharedPreferences){
        val JSONString : String? = s.getString(getString(R.string.item_sharedPref), "")
        item.value = JSONString?.let { Item.fromStringJSON(it) }
    }

    interface OnCompleteListener {
        fun onComplete()
    }

}