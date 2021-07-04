package com.example.android.politicalpreparedness.election

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.database.ElectionDataSource
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.launch

class VoterInfoViewModel(private val api: CivicsApiService, private val dataSource: ElectionDataSource) : ViewModel() {

    private val TAG = VoterInfoViewModel::class.java.simpleName

    //Done: Add live data to hold voter info
    private val _election = MutableLiveData<Election>()
    val election: LiveData<Election>
        get() = _election

    private val _isFollowed = MutableLiveData<Boolean>(false)
    val isFollowed: LiveData<Boolean>
        get() = _isFollowed

    //Done: Add var and methods to populate voter info
    private val _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo: LiveData<VoterInfoResponse>
        get() = _voterInfo

    //Done: Add var and methods to support loading URLs
    private val _url = MutableLiveData<String>()
    val url: LiveData<String>
        get() = _url

    fun checkElection(election: Election) {
        viewModelScope.launch {
            _election.value = election
            val saveElection = dataSource.getElectionById(election.id)
            _isFollowed.value = saveElection != null
        }
    }

    //Done: Add var and methods to save and remove elections to local database
    fun followElection() {
        viewModelScope.launch {
            _isFollowed.value?.let { electionIsFollowed ->
                val election = election.value
                election?.let {
                    if (electionIsFollowed) {
                        dataSource.deleteElectionById(election.id)
                    } else {
                        dataSource.saveElection(election)
                    }
                    _isFollowed.value = !electionIsFollowed
                }
            }
        }
    }

    fun getVoterInfo(election: Election) {
        viewModelScope.launch {
            val address = if (election.division.state.isEmpty()) election.division.country else "${election.division.country} - ${election.division.state}"
            val response = api.getVoterInfoAsync(address, election.id)

            try {
                val result = response.await()
                _voterInfo.value = result
            } catch (e: Exception) {
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }

    fun setIntentUrl(url: String) {
        _url.value = url
    }
}