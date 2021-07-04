package com.example.android.politicalpreparedness.election

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionRepository
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.representative.RepresentativeViewModel
import kotlinx.coroutines.launch

//Done: Construct ViewModel and provide election datasource
class ElectionsViewModel(private val api: CivicsApiService, private val repository: ElectionRepository): ViewModel() {

    private val TAG = ElectionsViewModel::class.java.simpleName

    //Done: Create live data val for upcoming elections
    private val _elections = MutableLiveData<List<Election>>()
    val elections: LiveData<List<Election>>
        get() = _elections

    //Done: Create live data val for saved elections
    private val _savedElections = MutableLiveData<List<Election>>()
    val savedElections: LiveData<List<Election>>
        get() = _savedElections

    //Done: Create val and functions to populate live data for upcoming elections from the API and
    // saved elections from local database
    fun fetchUpcomingElections() {
        viewModelScope.launch {
            val response = api.getElectionsAsync()
            try {
                val result = response.await()
                _elections.value = result.elections
            } catch (e: Exception) {
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }

    fun getSavedElections() {
        viewModelScope.launch {
            _savedElections.value = repository.getElections()
        }
    }
}