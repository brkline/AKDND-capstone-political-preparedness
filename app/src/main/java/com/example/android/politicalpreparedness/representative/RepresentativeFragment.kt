package com.example.android.politicalpreparedness.representative

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class RepresentativeFragment : Fragment() {

    //Done: Add Constant for Location request
    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    private val TAG = RepresentativeFragment::class.java.simpleName

    val representativeViewModel: RepresentativeViewModel by viewModels {RepresentativeViewModelFactory()}
    private lateinit var binding: FragmentRepresentativeBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        //Done: Establish bindings
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_representative, container, false)
        binding.repViewModel = representativeViewModel
        binding.lifecycleOwner = this

        initRecyclerView()

        //Done: Establish button listeners for field and location search
        binding.buttonSearch.setOnClickListener {
            val addressLine1 = binding.addressLine1.text
            val addressLine2 = binding.addressLine2.text
            val city = binding.city.text
            val state = binding.state.getItemAtPosition(binding.state.selectedItemPosition)
            val zip = binding.zip.text

            representativeViewModel.findRepresentatives("$addressLine1 $addressLine2, $city, $state, $zip")
        }

        binding.buttonLocation.setOnClickListener {
            if (checkLocationPermissions()) {
                getLocation()
            }
        }

        return binding.root
    }

    private fun initRecyclerView() {

        //Done: Define and assign Representative adapter
        val adapter = RepresentativeListAdapter()
        binding.fragmentRepresentativeRecyclerView.adapter = adapter

        //Done: Populate Representative adapter
        representativeViewModel.representatives.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //Done: Handle location permission result to get location on permission granted
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getLocation()
                }
                return
            }
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            //Done: Request Location permissions
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            false
        }
    }

    private fun isPermissionGranted() : Boolean {
        //Done: Check if permission is already granted and return (true = granted, false = denied/other)
        return (ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {

        //Done: Get location from LocationServices
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity as Activity)

        //Done: The geoCodeLocation method is a helper function to change the lat/long location to a human readable street address
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (null != location) {
                try {
                    val address = geoCodeLocation(location)
                    representativeViewModel.setAddress(address)
                } catch (e: Exception) {
                    e.message?.let { Log.e(TAG, it) }
                }
            } else {
                Snackbar.make(
                    requireView(),
                    getString(R.string.location_failed),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
                .map { address ->
                    Address(address.thoroughfare, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
                }
                .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

}