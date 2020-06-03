package it.polito.mad.mhackeroni.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.viewmodel.MapViewModel
import it.polito.mad.mhackeroni.viewmodel.UserMapViewModel


class MapFragment: Fragment(), OnMapReadyCallback {
    private var mapViewModel: MapViewModel = MapViewModel()
    private var userMapViewModel: UserMapViewModel = UserMapViewModel()
    private var isUserPosition = false
    private var itemLocation : LatLng? = null
    private var profileLocation : LatLng? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_map, container, false)
        activity?.run {
            mapViewModel = ViewModelProviders.of(requireActivity()).get(MapViewModel::class.java)
            userMapViewModel = ViewModelProviders.of(requireActivity()).get(UserMapViewModel::class.java)
        }
        setHasOptionsMenu(true)
        return v
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.item_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        isUserPosition = arguments?.getBoolean("user") ?: false
        arguments?.clear()
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_map_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        // Handle menu item selection
        return when (menuItem.itemId) {
            R.id.menu_save -> {
                // Update shared view models
                if(profileLocation != null && isUserPosition){
                    userMapViewModel.position.value = profileLocation
                } else if(itemLocation != null){
                    mapViewModel.position.value = itemLocation
                }

                view?.findNavController()?.popBackStack()
                return true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        map?.setOnMapClickListener(OnMapClickListener { point ->
            if(isUserPosition)
                profileLocation = point
            else
                itemLocation = point

            map.clear()
            map.addMarker(MarkerOptions().position(point))
        })
    }

}
