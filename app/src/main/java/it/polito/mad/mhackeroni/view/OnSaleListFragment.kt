package it.polito.mad.mhackeroni.view


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.adapters.ItemAdapter
import it.polito.mad.mhackeroni.model.Item
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import it.polito.mad.mhackeroni.utilities.ItemFilter
import it.polito.mad.mhackeroni.adapters.ItemAdapter.MyAdapterListener
import it.polito.mad.mhackeroni.viewmodel.OnSaleListFragmentViewModel
import java.util.ArrayList


class OnSaleListFragment: Fragment() {

    private lateinit var myAdapter: ItemAdapter
    private var searchFilter : ItemFilter = ItemFilter()
    private lateinit var vm : OnSaleListFragmentViewModel
    private lateinit var itemList: RecyclerView
    private lateinit var searchView: SearchView

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
        searchView = searchItem.actionView as SearchView
        searchView.isIconified = false

        searchView.maxWidth = Int.MAX_VALUE //set search menu as full width
        // searchView.setQueryHint()

        searchView.setOnQueryTextFocusChangeListener { _: View, hasFocus: Boolean ->
            searchView.isIconified = false
            hideKeyboard()
        }

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
        bundle.putString("item", item.let { Item.toJSON(it).toString()})
        bundle.putBoolean("fromList", true)
        bundle.putBoolean("allowModify", false)
        view?.findNavController()?.navigate(R.id.action_nav_itemListSale_to_nav_ItemDetail, bundle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_filter -> {
                showFilterDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateFilter() {
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("filter_key", searchFilter.name)
        outState.putDouble("filter_price_min", searchFilter.price_min)
        outState.putDouble("filter_price_max", searchFilter.price_max)
        outState.putStringArrayList("filter_category", searchFilter.category as ArrayList<String>)
        outState.putStringArrayList("filter_subcategory", searchFilter.subcategory as ArrayList<String>)
        outState.putString("filter_location", searchFilter.location)
        outState.putStringArrayList("filter_condition", searchFilter.condition as ArrayList<String>)
        outState.putString("filter_user", searchFilter.user)

    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if(savedInstanceState != null) {
            searchFilter.name = savedInstanceState.getString("filter_key") ?: ""
            searchFilter.price_min = savedInstanceState.getDouble("filter_price_min")
            searchFilter.price_max = savedInstanceState.getDouble("filter_price_max")

            if(searchFilter.price_max <= 0.0 || searchFilter.price_max < searchFilter.price_min){
                searchFilter.price_max = 9999.0
            }

            searchFilter.category.addAll(savedInstanceState.getStringArrayList("filter_category") ?: listOf())
            searchFilter.subcategory.addAll(savedInstanceState.getStringArrayList("filter_subcategory") ?: listOf())

            searchFilter.location = savedInstanceState.getString("filter_location") ?: ""
            searchFilter.condition.addAll(savedInstanceState.getStringArrayList("filter_condition") ?: listOf())
            searchFilter.user = savedInstanceState.getString("filter_user") ?: ""
        }

        updateFilter()
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

        // Radio Buttons
        val radioPrice1 = dialog.findViewById<RadioButton>(R.id.radioPrice1)
        val radioPrice2 = dialog.findViewById<RadioButton>(R.id.radioPrice2)
        val radioPrice3 = dialog.findViewById<RadioButton>(R.id.radioPrice3)
        val radioPrice4 = dialog.findViewById<RadioButton>(R.id.radioPrice4)
        val radioPrice5 = dialog.findViewById<RadioButton>(R.id.radioPrice5)
        val radioPrice6 = dialog.findViewById<RadioButton>(R.id.radioPrice6)

        // Edit text
        val locationEditText = dialog.findViewById<EditText>(R.id.filter_box_location)

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

            if(radioPrice1.isChecked){
                searchFilter.price_min = 0.0
                searchFilter.price_max = 20.0
            }
            else if(radioPrice2.isChecked){
                searchFilter.price_min = 20.0
                searchFilter.price_max = 50.0
            }
            else if(radioPrice3.isChecked){
                searchFilter.price_min = 50.0
                searchFilter.price_max = 100.0
            }
            else if(radioPrice4.isChecked){
                searchFilter.price_min = 100.0
                searchFilter.price_max = 250.0
            }
            else if(radioPrice5.isChecked){
                searchFilter.price_min = 250.0
                searchFilter.price_max = 1000.0
            }
            else if(radioPrice6.isChecked){
                searchFilter.price_min = 1000.0
                searchFilter.price_max = Double.POSITIVE_INFINITY
            }

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

    override fun onResume() {
        super.onResume()
        if(this::searchView.isInitialized)
            hideKeyboard()
    }

    override fun onPause() {
        super.onPause()
        if(this::searchView.isInitialized)
            hideKeyboard()
    }

    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}