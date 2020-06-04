package it.polito.mad.mhackeroni.view


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.adapters.FavItemAdapter
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import it.polito.mad.mhackeroni.adapters.FavItemAdapter.MyAdapterListener
import it.polito.mad.mhackeroni.viewmodel.ItemsOfInterestListFragmentViewModel


class ItemsOfInterestListFragment: Fragment() {

    private lateinit var myAdapter: FavItemAdapter
    private lateinit var vm : ItemsOfInterestListFragmentViewModel
    private lateinit var itemList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       return inflater.inflate(R.layout.fragment_itemofinterest_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseRepo.INSTANCE.updateToken(FirebaseRepo.INSTANCE.getID(requireContext()))

        vm = ViewModelProvider(this).get(ItemsOfInterestListFragmentViewModel::class.java)
        vm.uid = FirebaseRepo.INSTANCE.getID(requireContext())

        itemList = view.findViewById(R.id.itemofinterest_list)

        myAdapter =
            FavItemAdapter(
                mutableListOf(),
                object : MyAdapterListener {
                    override fun itemViewOnClick(item: Item) {
                        navigateWithInfo(item)
                    }
                })

        itemList.adapter = myAdapter
        itemList.layoutManager = LinearLayoutManager(context)

        vm.getItemIds().observe(viewLifecycleOwner, Observer {
            vm.getItemsByIds(it).observe(viewLifecycleOwner, Observer {
                myAdapter.reload(it)
            })
        })
    }

    override fun onResume() {
        super.onResume()
        Log.d("KKKKK","Item count: ${myAdapter.itemCount}")
    }

    private fun navigateWithInfo(item: Item) {
        val bundle = Bundle()
        bundle.putString("item", item.let { Item.toJSON(it).toString()})
        bundle.putBoolean("fromList", true)
        bundle.putBoolean("allowModify", false)
        view?.findNavController()?.navigate(R.id.action_nav_itemOfInterestList_to_nav_ItemDetail, bundle)
    }
}