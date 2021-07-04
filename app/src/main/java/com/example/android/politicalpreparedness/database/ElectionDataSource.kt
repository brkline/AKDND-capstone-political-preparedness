package com.example.android.politicalpreparedness.database

import com.example.android.politicalpreparedness.network.models.Election

interface ElectionDataSource {
    suspend fun getElections(): List<Election>
    suspend fun saveElection(election: Election)
    suspend fun getElectionById(id: Int): Election?
    suspend fun deleteElectionById(id: Int)
}