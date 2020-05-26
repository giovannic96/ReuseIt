package it.polito.mad.mhackeroni.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.adapters.BoughtItemAdapter
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import it.polito.mad.mhackeroni.viewmodel.BoughtItemsListFragmentViewModel

class BoughtItemsListFragment: Fragment() {

    private lateinit var myAdapter: BoughtItemAdapter
    private lateinit var vm : BoughtItemsListFragmentViewModel
    private lateinit var itemList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       return inflater.inflate(R.layout.fragment_itembought_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseRepo.INSTANCE.updateToken(FirebaseRepo.INSTANCE.getID(requireContext()))

        vm = ViewModelProvider(this).get(BoughtItemsListFragmentViewModel::class.java)
        vm.uid = FirebaseRepo.INSTANCE.getID(requireContext())

        itemList = view.findViewById(R.id.itembought_list)

        myAdapter =
            BoughtItemAdapter(
                mutableListOf(),
                object : BoughtItemAdapter.MyAdapterListener {
                    override fun itemViewOnClick(item: Item) {
                        navigateWithInfo(item)
                    }
                })

        itemList.adapter = myAdapter
        itemList.layoutManager = LinearLayoutManager(context)

        vm.getBoughtItems().observe(viewLifecycleOwner, Observer {
            myAdapter.reload(it)
        })
    }

    private fun navigateWithInfo(item: Item) {
        val bundle = Bundle()
        bundle.putString("item", item.let { Item.toJSON(it).toString()})
        bundle.putBoolean("fromList", true)
        bundle.putBoolean("allowModify", false)
        view?.findNavController()?.navigate(R.id.action_nav_boughtItemsList_to_nav_ItemDetail, bundle)
    }
}