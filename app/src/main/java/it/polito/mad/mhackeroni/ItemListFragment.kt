package it.polito.mad.mhackeroni

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import it.polito.mad.mhackeroni.ItemAdapter.MyAdapterListener

class ItemListFragment: Fragment() {

    private lateinit var items: MutableList<Item>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v = inflater.inflate(R.layout.fragment_itemlist, container, false)
        val itemList:RecyclerView = v.findViewById(R.id.item_list)
        items = getItems()

        itemList.adapter = ItemAdapter(items, object : MyAdapterListener {
            override fun editItemViewOnClick(item: Item) {
                navigateWithInfo(R.id.action_nav_itemList_to_nav_ItemDetailEdit, item)
            }

            override fun itemViewOnClick(item: Item) {
                navigateWithInfo(R.id.action_nav_itemList_to_nav_ItemDetail, item)
            }
        })
        itemList.layoutManager = if(items.size == 0)
                                    LinearLayoutManager(context)
                                else
                                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun getItems(): MutableList<Item> {
        //return mutableListOf() -> if we want to try empty list
        return mutableListOf(
            Item("Pane di grano tenero", 0.70),
            Item("iPhone 8 plus 64GB come nuovo", 1.20),
            Item("iphone x 256 giga più air pods pro", 6.70),
            Item("scarpe liu jo nuove originali", 14.20),
            Item("case coolermaster come da immagine", 30.70),
            Item("penna biro", 221.20),
            Item("libro harry potter e la camera dei segreti usato", 10.70),
            Item("tv philips 32'' fullHD", 165.20),
            Item("Hard disk 512Gb", 0.33),
            Item("SSD 256Gb", 1.20),
            Item("Shitsu max back and shoulder massager", 0.74),
            Item("Cartongesso a marmorino", 1.20),
            Item("Giochi playstation 2 in ottimo stato", 0.30),
            Item("Lievito di birra", 22.20))
    }

    private fun navigateWithInfo(layoutId: Int, item: Item) {
        val bundle = Bundle()
        bundle.putString("item", item.let { Item.toJSON(it).toString()})
        view?.findNavController()?.navigate(layoutId, bundle)
    }

}