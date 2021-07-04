package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.network.CivicsApi

//Done: Create Factory to generate RepresentativeViewModel with provided representative from the API
class RepresentativeViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RepresentativeViewModel(CivicsApi.retrofitService) as T
    }

}
