package it.polito.mad.mhackeroni.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class UserMapViewModel : ViewModel() {

    val position: MutableLiveData<LatLng> by lazy {
        MutableLiveData<LatLng>()
    }
}