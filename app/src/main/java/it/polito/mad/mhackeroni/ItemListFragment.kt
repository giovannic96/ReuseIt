package it.polito.mad.mhackeroni

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class ItemListFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v = inflater.inflate(R.layout.fragment_itemlist, container, false)
        val itemList:RecyclerView = v.findViewById(R.id.item_list)

        val items = arrayOf(
            Item("Pane di grano tenero ma non troppo tenero, comunque non duro", 0.70f),
            Item("Farina 00 Barilla a buon prezzo", 1.20f),
            Item("Acqua sale minerale che fa male al cuore", 6.70f),
            Item("sale", 14.20f),
            Item("cellulare onetouchplus 3+ AFFARONE", 30.70f),
            Item("penna biro che non bira!", 221.20f),
            Item("PC senza sistema operativo, operati tu al suo posto", 10.70f),
            Item("DVD sicilia best hit 1996", 165.20f),
            Item("Hard disk più lento di tua nonna", 0.33f),
            Item("SSD", 1.20f),
            Item("Tastiera", 0.74f),
            Item("Pastiera senza iera solo pasta", 1.20f),
            Item("Pasta mulino bianco ma non cosi nero come sembra", 0.30f),
            Item("Lievito di birra", 22.20f)
        )
        itemList.adapter = ItemAdapter(items)
        itemList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}