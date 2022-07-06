package com.example.findmyphone

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MyService : Service() {
    var databaseRef: DatabaseReference? = null
    override fun onBind(p0: Intent?): IBinder? {
        return null!!
    }

    // called when service is created
    override fun onCreate() {
        super.onCreate()
        databaseRef = FirebaseDatabase.getInstance().reference
        IsServiceRunning = true

    }

    // called when service is started
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // code run in background for long time
        var myLocation = MyLocationListener()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return START_STICKY_COMPATIBILITY
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 3f, myLocation)

        // listening to request
        var userData = UserData(this)
        val myPhoneNumber = userData.loadPhoneNumber()
        myPhoneNumber?.let {
            databaseRef!!.child("Users").child(it).child("request").addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (MyService.myLocation != null) {
                            val df = SimpleDateFormat("yyyy/MMM/dd HH:MM:ss")
                            val date = Date()
                            databaseRef!!.child("Users").child(myPhoneNumber).child("location")
                                .child("latitude").setValue(MyService.myLocation!!.latitude)
                            databaseRef!!.child("Users").child(myPhoneNumber).child("location")
                                .child("longitude").setValue(MyService.myLocation!!.longitude)
                            databaseRef!!.child("Users").child(myPhoneNumber).child("location")
                                .child("lastOnline").setValue(df.format(date).toString())
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }


                }
            )
        }

        return Service.START_NOT_STICKY
    }

    companion object {
        var myLocation: Location? = null
        var IsServiceRunning = false

    }

    inner class MyLocationListener() : LocationListener {
        init {
            myLocation = Location("me")
            myLocation!!.longitude = 0.0
            myLocation!!.latitude = 0.0
        }

        override fun onLocationChanged(location: Location) {
            myLocation =
                location // so whenever location is updated i will get it on i will update it on my location
        }

    }


}