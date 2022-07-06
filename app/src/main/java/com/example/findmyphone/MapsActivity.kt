package com.example.findmyphone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.findmyphone.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    var databaseRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bundel: Bundle? = intent.extras
        val phoneNumber = bundel!!.getString("phoneNumber")
        databaseRef = FirebaseDatabase.getInstance().reference
        if (phoneNumber != null) {
            databaseRef!!.child("Users").child(phoneNumber).child("location").addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(datasnapshot: DataSnapshot) {
                        try {
                            val td = datasnapshot.value as HashMap<String, Any>
                            val lat = td["latitude"].toString()
                            val log = td["longitude"].toString()
                            location = LatLng(lat.toDouble(), log.toDouble())
                            val lastOnline = td["lastOnline"].toString()
                            lastOnlines = td["lastOnline"].toString()
                            loadMap()
                        } catch (e: Exception) {

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                }
            )
        }
    }

    fun loadMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    companion object {
        var location = LatLng(-34.0, 151.0)
        var lastOnlines = "not_defined"
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        mMap.addMarker(MarkerOptions().position(location).title("Marker in $location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }
}