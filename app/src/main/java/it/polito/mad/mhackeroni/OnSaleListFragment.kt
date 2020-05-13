package it.polito.mad.mhackeroni


import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.mhackeroni.ItemAdapter.MyAdapterListener
import it.polito.mad.mhackeroni.utilities.StorageHelper



class OnSaleListFragment: Fragment() {

    private var items: MutableList<Item> = mutableListOf()
    private lateinit var myAdapter:ItemAdapter
    private var searchFilter : ItemFilter = ItemFilter()
    private lateinit var vm : OnSaleListFragmentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_itemlist_sale, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm = ViewModelProvider(this).get(OnSaleListFragmentViewModel::class.java)
        val itemList:RecyclerView = view.findViewById(R.id.item_list_sale)

        myAdapter = ItemAdapter(mutableListOf(), object : MyAdapterListener {

            override fun editItemViewOnClick(item: Item) {
                // TODO: REMOVE ALL EDIT REFERENCES
                // navigateWithInfo(R.id.action_nav_itemList_to_nav_ItemDetailEdit, item)
            }

            override fun itemViewOnClick(item: Item) {
                navigateWithInfo(R.id.action_nav_itemListSale_to_nav_ItemDetail, item)
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

        vm.getItems().observe(viewLifecycleOwner, Observer {
            myAdapter.reload(it)
        })


        // getResultAndUpdateList(itemList)
    }

    override fun onResume() {
        super.onResume()
        // myAdapter.refresh(storageHelper.loadItemList(sharedPref));

    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_filter_menu, menu)

        // TODO: Handle search - filters


        val searchItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem.actionView as SearchView
        // searchView.setQueryHint()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                searchFilter.name = query
                updateFilter()
                return false
            }

        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun navigateWithInfo(layoutId: Int, item: Item) {
        val bundle = Bundle()

        // TODO pass whole item or just document ID

        bundle.putString("item", item.let { Item.toJSON(it).toString()})
        bundle.putBoolean("fromList", true)
        view?.findNavController()?.navigate(layoutId, bundle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.menu_filter -> {
                showFilterDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)

    }

    private fun navigateWithoutInfo(layoutId: Int) {
        val bundle = Bundle()
        bundle.putString("item", null)
        view?.findNavController()?.navigate(layoutId, bundle)
    }

    private fun getResultAndUpdateList(recyclerView: RecyclerView) {

        // TODO: Check this

        val newItemJSON = arguments?.getString("new_item", "")
        if(!newItemJSON.isNullOrEmpty()) {
            insertSingleItem(newItemJSON.let { Item.fromStringJSON(it) }) //update list
            recyclerView.layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
                else
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        } else {
            val editedItemJSON = arguments?.getString("edited_item")
            val oldItemJSON = arguments?.getString("old_item")

            if (editedItemJSON != null && oldItemJSON != null) {
                handleEditItem(editedItemJSON, oldItemJSON)
            }

        }

        arguments?.clear()
    }

    private fun handleEditItem(editedItemJSON: String, oldItem: String) {

        // TODO: not necessary
        val storageHelper =
            StorageHelper(requireContext())
        val sharedPref:SharedPreferences = requireContext()
            .getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE)

        if(editedItemJSON != oldItem) {

            val item = editedItemJSON.let { Item.fromStringJSON(it) }

            if (item != null) {
                storageHelper.editItem(sharedPref, item)
                val snackbar = view?.let { Snackbar.make(it, getString(R.string.item_update), Snackbar.LENGTH_LONG) }

                if (snackbar != null) {
                    snackbar.setAction(getString(R.string.undo), View.OnClickListener {

                        Item.fromStringJSON(oldItem)?.let { it1 ->
                            storageHelper.editItem(sharedPref,
                                it1
                            )
                        }

                        items = storageHelper.loadItemList(sharedPref)
                        myAdapter.refresh(items)

                    })
                    snackbar.show()
                }
            }
        }
    }

    private fun insertSingleItem(newItem: Item?) {
        if(newItem != null) {
            items.add(0, newItem) //TODO set correct position (maybe sort items by date)
            // storageHelper.saveItemList(sharedPref, items)
            myAdapter.notifyItemInserted(0)
        }
    }

    private fun updateFilter(){

        vm.getItems().removeObservers(viewLifecycleOwner)
        vm.getItems().observe(viewLifecycleOwner, Observer {

            myAdapter.reload(it.filter { item -> searchFilter.match(item) })
        })
    }

    private fun showFilterDialog() {
        val dialog = Dialog(requireActivity())

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.filter_dialog_box)

        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        // TODO: Handle the selected values - fix the layout

        // Buttons
        val okBtn = dialog.findViewById<Button>(R.id.filter_ok_btn)
        val cancelBtn = dialog.findViewById<Button>(R.id.filter_cancel_btn)

        // Edit text
        val locationEditText = dialog.findViewById<EditText>(R.id.filter_box_location)
        val minPriceEditText = dialog.findViewById<EditText>(R.id.filter_box_min_price)
        val maxPriceEditText = dialog.findViewById<EditText>(R.id.filter_box_max_price)

        // Check boxes
        val newCheckBox = dialog.findViewById<CheckBox>(R.id.filter_cond_new)
        val asNewCheckBox = dialog.findViewById<CheckBox>(R.id.filter_cond_as_new)
        val optCheckBox = dialog.findViewById<CheckBox>(R.id.filter_cond_opt)
        val goodCheckBox = dialog.findViewById<CheckBox>(R.id.filter_cond_good)
        val accCheckBox = dialog.findViewById<CheckBox>(R.id.filter_cond_acc)

        // Auto complete text views
        val categoryAutoComplete = dialog.findViewById<AutoCompleteTextView>(R.id.filter_box_category)
        val subcategoryAutoComplete = dialog.findViewById<AutoCompleteTextView>(R.id.filter_box_subcategory)

        // Cetegories and subcategories
        val categories = resources.getStringArray(R.array.categories)
        val subcategories = resources.getStringArray(R.array.subcategories)
        val arts = resources.getStringArray(R.array.arts)
        val sports = resources.getStringArray(R.array.sports)
        val babies = resources.getStringArray(R.array.babies)
        val womens = resources.getStringArray(R.array.womens)
        val mens = resources.getStringArray(R.array.mens)
        val electronics = resources.getStringArray(R.array.electronics)
        val games = resources.getStringArray(R.array.games)
        val automotives = resources.getStringArray(R.array.automotives)
        var selectedCat = categories

        var cat : String? = null
        var subCat : String? = null

        val adapterCat: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        categoryAutoComplete.setAdapter(adapterCat)

        var adapterSubcat: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            subcategories
        )

        subcategoryAutoComplete.setAdapter(adapterSubcat)

        categoryAutoComplete.onItemClickListener = object : AdapterView.OnItemSelectedListener,
            AdapterView.OnItemClickListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
            }

            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                subcategoryAutoComplete.setText(R.string.selectSubcat)

                when(position){
                    0-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, arts)
                        selectedCat = arts
                    }
                    1-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, sports)
                        selectedCat = sports
                    }
                    2-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, babies)
                        selectedCat = babies
                    }
                    3-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, womens)
                        selectedCat = womens
                    }
                    4-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, mens)
                        selectedCat = mens
                    }
                    5-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, electronics)
                        selectedCat = electronics
                    }
                    6-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, games)
                        selectedCat = games
                    }
                    7-> {adapterSubcat = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, automotives)
                        selectedCat = automotives
                    }

                }

                subcategoryAutoComplete.setAdapter(adapterSubcat)
                cat = categories[position]
            }
        }

        subcategoryAutoComplete.onItemClickListener = object : AdapterView.OnItemSelectedListener,
            AdapterView.OnItemClickListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
            }

            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                subCat = selectedCat[position]
            }
        }


        okBtn.setOnClickListener {
            searchFilter = ItemFilter()

            searchFilter.location = locationEditText.text.toString()
            searchFilter.price_min = minPriceEditText.text.toString().toDoubleOrNull() ?: 0.0
            searchFilter.price_max = maxPriceEditText.text.toString().toDoubleOrNull() ?: Double.POSITIVE_INFINITY

            // Setup cat and subcategory if setted
            cat?.let { it -> searchFilter.category.add(it)}
            subCat?.let { it -> searchFilter.subcategory.add(it)}

            if(newCheckBox.isChecked)
                searchFilter.condition.add(getString(R.string.cond_new))

            if(asNewCheckBox.isChecked)
                searchFilter.condition.add(getString(R.string.cond_as_new))


            if(optCheckBox.isChecked)
                searchFilter.condition.add(getString(R.string.cond_opt))


            if(goodCheckBox.isChecked)
                searchFilter.condition.add(getString(R.string.cond_good))


            if(accCheckBox.isChecked)
                searchFilter.condition.add(getString(R.string.cond_acc))

            dialog.dismiss()
            updateFilter()
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }
}