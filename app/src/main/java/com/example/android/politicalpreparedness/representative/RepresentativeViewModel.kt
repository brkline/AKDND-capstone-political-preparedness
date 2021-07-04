package com.example.android.politicalpreparedness.representative

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.representative.model.Representative
import com.example.android.politicalpreparedness.network.models.Address
import kotlinx.coroutines.launch


class RepresentativeViewModel(private val api: CivicsApiService): ViewModel() {

    private val TAG = RepresentativeViewModel::class.java.simpleName

    //Done: Establish live data for representatives and address
    private var _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    private var _address = MutableLiveData<Address>()
    val address: LiveData<Address>
        get() = _address

    //Done: Create function to fetch representatives from API from a provided address
    //Done: Create function to get address from individual fields
    fun findRepresentatives(address: String) {
        viewModelScope.launch {
            try {
                val (offices, officials) = api.getRepresentativesAsync(address).await()
                _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }
            } catch (e: Exception) {
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }

    //Done: Create function get address from geo location
    fun setAddress(address: Address) {
        _address.value = address
    }

}
