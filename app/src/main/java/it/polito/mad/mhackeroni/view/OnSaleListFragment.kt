package it.polito.mad.mhackeroni.view


import android.app.Dialog
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
import it.polito.mad.mhackeroni.*
import it.polito.mad.mhackeroni.view.ItemAdapter.MyAdapterListener
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import it.polito.mad.mhackeroni.utilities.ItemFilter
import it.polito.mad.mhackeroni.viewmodel.OnSaleListFragmentViewModel


class OnSaleListFragment: Fragment() {

    private lateinit var myAdapter: ItemAdapter
    private var searchFilter : ItemFilter = ItemFilter()
    private lateinit var vm : OnSaleListFragmentViewModel
    private lateinit var itemList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_itemlist_sale, container, false)
        setHasOptionsMenu(true)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseRepo.INSTANCE.updateToken(FirebaseRepo.INSTANCE.getID(requireContext()))

        vm = ViewModelProvider(this).get(OnSaleListFragmentViewModel::class.java)
        vm.uid = FirebaseRepo.INSTANCE.getID(requireContext())

        itemList = view.findViewById(R.id.item_list_sale)

        myAdapter = ItemAdapter(
            mutableListOf(),
            object : MyAdapterListener {

                override fun editItemViewOnClick(item: Item) {}

                override fun itemViewOnClick(item: Item) {
                    navigateWithInfo(item)
                }
            })

        myAdapter.allow_modify = false
        itemList.adapter = myAdapter
        itemList.layoutManager = LinearLayoutManager(context)

        vm.getItems().observe(viewLifecycleOwner, Observer {
            myAdapter.reload(it)
            updateLayoutManager(it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_filter_menu, menu)

        val searchItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem.actionView as SearchView
        // searchView.setQueryHint()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                if(newText.isNullOrEmpty()) {
                    searchFilter.name = ""
                    updateFilter()
                }
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

    private fun navigateWithInfo(item: Item) {
        val bundle = Bundle()
        bundle.putString("item", item.let { Item.toJSON(
            it
        ).toString()})
        bundle.putBoolean("fromList", true)
        bundle.putBoolean("allowModify", false)
        view?.findNavController()?.navigate(R.id.action_nav_itemListSale_to_nav_ItemDetail, bundle)
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

    private fun updateFilter(){
        vm.getItems().removeObservers(viewLifecycleOwner)
        vm.getItems().observe(viewLifecycleOwner, Observer {
            var found = false
            myAdapter.reload(it.filter { item ->
                    if(searchFilter.match(item)) {
                        found = true
                        true
                    } else
                        false
            })
            if(!found)
                updateLayoutManager(listOf())
            else
                updateLayoutManager(it)
        })
    }

    private fun updateLayoutManager(list: List<Item>) {
        itemList.layoutManager = if(list.isEmpty())
            LinearLayoutManager(context)
        else {
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
            else
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    private fun showFilterDialog() {
        val dialog = Dialog(requireActivity())

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.filter_dialog_box)

        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
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