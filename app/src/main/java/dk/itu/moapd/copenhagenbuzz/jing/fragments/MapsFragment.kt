package dk.itu.moapd.copenhagenbuzz.jing.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.database
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event
import dk.itu.moapd.copenhagenbuzz.jing.services.LocationService

class MapsFragment : Fragment(), OnMapReadyCallback {

    private var map: GoogleMap? = null
    private var locationReceiver: BroadcastReceiver? = null
    private var isReceiverRegistered = false

    private lateinit var eventsListener: ValueEventListener
    // Add this as a class-level property
    private lateinit var infoWindowAdapter: GoogleMap.InfoWindowAdapter

    companion object {
        const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
        private const val TAG = "MapsFragment"
    }

    @Suppress("DEPRECATION")
    private inner class LocationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == LocationService.ACTION_LOCATION_UPDATE) {
                val location = intent.getParcelableExtra<Location>(LocationService.EXTRA_LOCATION)
                location?.let {
                    Log.d(TAG, "New location update: (${it.latitude}, ${it.longitude})")
                    updateMapLocation(it)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Initializing map and location services")
        initializeMap()
        setupLocationReceiver()
        setupEventsListener()
    }

    private fun initializeMap() {
        (childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.let {
            it.getMapAsync(this)
        } ?: run {
            Log.w(TAG, "Creating new map fragment instance")
            SupportMapFragment.newInstance().also { fragment ->
                childFragmentManager.beginTransaction()
                    .replace(R.id.map, fragment)
                    .commit()
                fragment.getMapAsync(this)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        try {
            // Basic map setup
            googleMap.uiSettings.apply {
                isZoomControlsEnabled = true
                isCompassEnabled = true
                isMyLocationButtonEnabled = true
            }

            // Default Copenhagen view
            val copenhagen = LatLng(55.6761, 12.5683)
            googleMap.addMarker(MarkerOptions().position(copenhagen).title("Copenhagen"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(copenhagen, 12f))

            googleMap.setOnMarkerClickListener { marker ->
                // Show info window when marker is clicked
                marker.showInfoWindow()
                true
            }

            enableMyLocation()
            Log.d(TAG, "Map initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Map initialization failed", e)
            Toast.makeText(context, "Map error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLocationReceiver() {
        locationReceiver = LocationReceiver()
        if (checkPermission()) {
            startLocationService()
        } else {
            Log.d(TAG, "Requesting location permission")
            requestUserPermissions()
        }
        registerReceiver()
    }

    private fun updateMapLocation(location: Location) {
        map?.let {
            val latLng = LatLng(location.latitude, location.longitude)
            it.clear()
            it.addMarker(MarkerOptions().position(latLng).title("Current Location"))
            it.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            try {
                ContextCompat.registerReceiver(
                    requireActivity(),
                    locationReceiver,
                    IntentFilter(LocationService.ACTION_LOCATION_UPDATE),
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
                isReceiverRegistered = true
                Log.d(TAG, "Location receiver registered")
            } catch (e: Exception) {
                Log.e(TAG, "Receiver registration failed", e)
            }
        }
    }

    private fun unregisterReceiver() {
        locationReceiver?.let {
            try {
                requireActivity().unregisterReceiver(it)
                isReceiverRegistered = false
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Receiver already unregistered")
            }
        }
    }

    private fun startLocationService() {
        try {
            ContextCompat.startForegroundService(
                requireContext(),
                Intent(requireContext(), LocationService::class.java)
            )
            Log.d(TAG, "Location service started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start location service", e)
        }
    }

    private fun stopLocationService() {
        try {
            requireContext().stopService(Intent(requireContext(), LocationService::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop location service", e)
        }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Location permission granted")
            enableMyLocation()
            startLocationService()
        } else {
            Toast.makeText(
                requireContext(),
                "Location features disabled",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                map?.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                Log.e(TAG, "Location layer enablement failed", e)
            }
        }
    }

    override fun onDestroyView() {
        Log.d(TAG, "Cleaning up resources")
        database.getReference("events").removeEventListener(eventsListener)
        unregisterReceiver()
        stopLocationService()
        super.onDestroyView()
    }

    private fun checkPermission() = ActivityCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestUserPermissions() {
        if (!checkPermission()) {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setupEventsListener() {
        val eventsRef = database.getReference("events")

        eventsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = mutableListOf<Event>()
                for (eventSnapshot in snapshot.children) {
                    eventSnapshot.getValue(Event::class.java)?.let { event ->
                        events.add(event)
                    }
                }
                Log.d(TAG, "Loaded ${events.size} events from database")
                displayEventsOnMap(events)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load events: ${error.message}")
            }
        }
        eventsRef.addValueEventListener(eventsListener)
    }

    private fun displayEventsOnMap(events: List<Event>) {
        map?.let { googleMap ->
            googleMap.clear()

            for (event in events) {
                try {
                    if (event.eventLocation.latitude != 0.0 && event.eventLocation.longitude != 0.0) {
                        val location = LatLng(event.eventLocation.latitude, event.eventLocation.longitude)

                        // Create marker with fallback to default if icon fails
                        try {
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(location)
                                    .title(event.eventName)
                                    .snippet("${event.eventType}\n${event.eventStartDate}")
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to create custom marker, using default", e)
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(location)
                                    .title(event.eventName)
                                    .snippet("${event.eventType}\n${event.eventStartDate}")
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing event: ${event.eventName}", e)
                }
            }
        }
    }

}