package it.polito.mad.mhackeroni

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.beust.klaxon.Klaxon
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.mhackeroni.ItemAdapter.MyAdapterListener

class ItemListFragment: Fragment() {

    private var items: MutableList<Item> = mutableListOf()
    private lateinit var myAdapter:ItemAdapter
    private lateinit var sharedPref: SharedPreferences
    private var mListener: OnCompleteListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        sharedPref = requireContext().getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE)
        val v = inflater.inflate(R.layout.fragment_itemlist, container, false)
        val itemList:RecyclerView = v.findViewById(R.id.item_list)
        items = loadData(sharedPref)

        val fab:FloatingActionButton = v.findViewById(R.id.fab)
        fab.setOnClickListener {
            navigateWithoutInfo(R.id.action_nav_itemList_to_nav_ItemDetailEdit)
        }

        myAdapter = ItemAdapter(items, object : MyAdapterListener {
            override fun editItemViewOnClick(item: Item) {
                navigateWithInfo(R.id.action_nav_itemList_to_nav_ItemDetailEdit, item)
            }

            override fun itemViewOnClick(item: Item) {
                navigateWithInfo(R.id.action_nav_itemList_to_nav_ItemDetail, item)
            }
        })
        itemList.adapter = myAdapter
        itemList.layoutManager = if(items.size == 0)
                                    LinearLayoutManager(context)
                                else {
                                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                                        StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
                                    else
                                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                                }
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getResult()
    }

    override fun onResume() {
        super.onResume()
        myAdapter.refresh(loadData(sharedPref));
    }

    private fun getItems(): MutableList<Item> {
        Log.d("KKK", items.toString())
        //return mutableListOf() -> if we want to try empty list
        if(items.isEmpty())
            return mutableListOf(
                Item("Pane di grano tenero", 0.70, "Descrizione oggetto scriviamo qualche riga casuale per testare, senza glutine", "Alimenti", "25/05/2020", "Torino", "Ottimo",  "android.resource://it.polito.mad.mhackeroni/drawable/ic_box.png"),
                Item("iPhone 8 plus 64GB come nuovo", 1.20, "Descrizione oggetto scriviamo qualche riga casuale per testare", "Telefonia", "25/05/2020", "Torino", "Buono", "android.resource://it.polito.mad.mhackeroni/drawable/ic_box.png"),
                Item("iphone x 256 giga più air pods pro", 6.70, "Descrizione oggetto scriviamo qualche riga casuale per testare, arriva con mattone", "Telefonia e Accessori", "25/05/2020", "Torino","Ottimo", "android.resource://it.polito.mad.mhackeroni/drawable/ic_box.png"),
                Item("scarpe liu jo nuove originali", 14.20, "Descrizione oggetto scriviamo qualche riga casuale per testare, sono cinesi", "Scarpe", "25/05/2020", "Torino","Buono", "android.resource://it.polito.mad.mhackeroni/drawable/ic_box.png"),
                Item("case coolermaster come da immagine", 30.70, "Descrizione oggetto scriviamo qualche riga casuale per testare", "Informatica", "25/05/2020", "Torino","Nuovo", "android.resource://it.polito.mad.mhackeroni/drawable/ic_box.png"),
                Item("penna biro", 221.20, "Descrizione oggetto scriviamo qualche riga casuale per testare", "Scuola", "25/05/2020", "Torino","Come nuovo", "android.resource://it.polito.mad.mhackeroni/drawable/ic_box.png"),
                Item("libro harry potter e la camera dei segreti usato", 10.70, "Descrizione oggetto scriviamo qualche riga casuale per testare, silente muore", "Libri", "25/05/2020", "Torino","Nuovo", "android.resource://it.polito.mad.mhackeroni/drawable/ic_box.png"),
                Item("tv philips 32'' fullHD", 165.20, "Descrizione oggetto scriviamo qualche riga casuale per testare, questa tv è un pacco", "Tecnologia", "25/05/2020", "Torino", "Buono","android.resource://it.polito.mad.mhackeroni/drawable/ic_box.png"),
                Item("Hard disk 512Gb", 0.33, "Descrizione oggetto scriviamo qualche riga casuale per testare", "Informatica", "25/05/2020", "Torino","Buono", "android.resource://it.polito.mad.mhackeroni/drawable/ic_box.png"),
                Item("SSD 256Gb", 1.20, "Descrizione oggetto scriviamo qualche riga casuale per testare, SSD VELOCE", "Informatica", "25/05/2020", "Torino", "Buono","android.resource://it.polito.mad.mhackeroni/drawable/ic_box.png"),
                Item("Shitsu max back and shoulder massager", 0.74, "Descrizione oggetto scriviamo qualche riga casuale per testare", "Relax", "25/05/2020", "Torino","Buono", "android.resource://it.polito.mad.mhackeroni/drawable/ic_box.png"),
                Item("Cartongesso a marmorino", 1.20, "Descrizione oggetto scriviamo qualche riga casuale per testare", "Fai da te", "25/05/2020", "Torino","Buono", "android.resource://it.polito.mad.mhackeroni/drawable/ic_box.png"),
                Item("Giochi playstation 2 in ottimo stato", 0.30, "Descrizione oggetto scriviamo qualche riga casuale per testare", "Console", "25/05/2020", "Torino", "Ottimo","android.resource://it.polito.mad.mhackeroni/drawable/ic_box.png"),
                Item("Lievito di birra", 22.20, "Descrizione oggetto scriviamo qualche riga casuale per testare, per birra tre luppoli", "Alimenti", "25/05/2020", "Torino","Ottimo", "android.resource://it.polito.mad.mhackeroni/drawable/ic_box.png"))
        else
            return items
    }

    private fun navigateWithInfo(layoutId: Int, item: Item) {
        val bundle = Bundle()
        bundle.putString("item", item.let { Item.toJSON(it).toString()})
        view?.findNavController()?.navigate(layoutId, bundle)
    }

    private fun navigateWithoutInfo(layoutId: Int) {
        val bundle = Bundle()
        bundle.putString("item", null)
        view?.findNavController()?.navigate(layoutId, bundle)
    }

    private fun getResult() {
        val newItemJSON = arguments?.getString("new_item", "")
        if(!newItemJSON.isNullOrEmpty()) {
            insertSingleItem(newItemJSON.let { Item.fromStringJSON(it) }) //update list
        }
        arguments?.clear() // clear arguments
    }

    private fun insertSingleItem(newItem: Item?) {
        if(newItem != null) {
            items.add(0, newItem) //TODO set correct position (maybe sort items by date)
            saveData(sharedPref, items)
            myAdapter.notifyItemInserted(0)
        }
    }

    private fun saveData(s: SharedPreferences, l:MutableList<Item>) {
        with (s.edit()) {
            putString(getString(R.string.itemList_sharedPref), Klaxon().toJsonString(l))
            apply()
        }
        mListener?.onComplete();
    }

    private fun loadData(s: SharedPreferences):MutableList<Item> {
        val JSONString: String? = s.getString(getString(R.string.itemList_sharedPref), "")
        if(!JSONString.isNullOrEmpty()) {
            val items = Klaxon().parseArray<Item>(JSONString)?.toMutableList()
            if (items != null) {
                return items
            }
        }
        return mutableListOf()
    }

    //TODO add this later
    /*
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as ItemListFragment.OnCompleteListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnCompleteListener")
        }
    }
     */

    interface OnCompleteListener {
        fun onComplete()
    }
}