package com.example.findmyphone

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.*
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    var adapter: ContactAdapter? = null
    val listOfContact = ArrayList<UserContact>()
    var databaseRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val userData = UserData(this)
        userData.isFirstTimeLoad()

        databaseRef = FirebaseDatabase.getInstance().reference
        // for debugging only
        //dummyData()
        adapter = ContactAdapter(this, listOfContact)
//        val idOfListView = findViewById<ListView>(R.id.lvContactlist)
        lvContactlist.adapter = adapter
        // when you click on any contact it should delete that one and after that it should refresh
        lvContactlist.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, postion, id ->
                val userInfo = listOfContact[postion]
                // get date time
                val df = SimpleDateFormat("yyyy/MMM/dd HH:MM:ss")
                val date = Date()
                // save to database
                userInfo.phoneNumber?.let {
                    databaseRef!!.child("Users").child(it).child("request")
                        .setValue(df.format(date).toString())
                }
                val intent = Intent(applicationContext, MapsActivity::class.java)
                intent.putExtra("phoneNumber", userInfo.phoneNumber)
                startActivity(intent)
            }
        refreshUser()
    }

    override fun onResume() {
        super.onResume()
        val userData = UserData(this)
        if (userData.loadPhoneNumber() == "empty") return
        refreshUser()
        if (MyService.IsServiceRunning) return // do not run again
        checkContactPermission()
        checkLocationPermission()

    }

    fun refreshUser() {
        val userData = UserData(this)
        userData.loadPhoneNumber()?.let {
            databaseRef!!.child("Users").child(it).child("Finders")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(datasnapshot: DataSnapshot) {
                        val td = datasnapshot.value as HashMap<String, Any>
                        listOfContact.clear()
                        // if there is no user in firebase
                        if (td == null) {
                            listOfContact.add(UserContact("No_Users", "nothing"))
                            adapter!!.notifyDataSetChanged()
                            return
                        }
                        for (key in td.keys) {
                            val name = listOfContacts[key]
                            listOfContact.add(UserContact(name.toString(), key))

                        }
                        adapter!!.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

    }

    // generate dummy first time - for debugging process
    fun dummyData() {
        listOfContact.add(UserContact("Vidit", "913549852"))
        listOfContact.add(UserContact("Ekshita", "91302933"))
        listOfContact.add(UserContact("Yash", "91905852"))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addTracker -> {
                val intent = Intent(this, MyTrackers::class.java)
                startActivity(intent)
            }
            R.id.help -> {
                //TODO: as k for help from friend
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    class ContactAdapter(context: Context, var listOfContact: ArrayList<UserContact>) :
        BaseAdapter() {
        var context: Context? = context
        override fun getCount(): Int {
            return listOfContact.size
        }

        override fun getItem(p0: Int): Any {
            return listOfContact[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val userContact = listOfContact[p0]
            if (userContact.name.equals("No_Users")) {
                val inflater =
                    context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val contactTicketView = inflater.inflate(R.layout.no_user, null)
                return contactTicketView
            } else {
                val inflater =
                    context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val contactTicketView = inflater.inflate(R.layout.contact_ticket, null)
                contactTicketView.findViewById<TextView>(R.id.tvName).text = userContact.name
                contactTicketView.findViewById<TextView>(R.id.tvPhoneNumber).text =
                    userContact.phoneNumber
                return contactTicketView
            }
        }

    }

    val Contact_Code = 123 // example or dummy
    fun checkContactPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), Contact_Code)
            return
        }

        loadContact()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Contact_Code -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadContact()
                } else {
                    Toast.makeText(
                        this,
                        "Cannot access to contact, please permit",
                        Toast.LENGTH_LONG
                    ).show()

                }
            }
            Location_Code -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserLocation()
                } else {
                    Toast.makeText(
                        this,
                        "Cannot access to contact, please permit",
                        Toast.LENGTH_LONG
                    ).show()

                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    // pick  contact from contacts
    var listOfContacts = HashMap<String, String>()
    private fun loadContact() {
        try {
            listOfContacts.clear()
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            cursor!!.moveToFirst()
            do {
                val name =
                    cursor.getString(abs(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)))
                val phoneNumber =
                    cursor.getString(abs(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)))
            } while (cursor!!.moveToNext())
        } catch (e: Exception) {

        }
    }

    val Location_Code = 124 // example or dummy
    fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                Contact_Code
            )
            return
        }

        getUserLocation()
    }

    fun getUserLocation() {
        // start service, if not running
        if (!MyService.IsServiceRunning) {
            val intent = Intent(baseContext, MyService::class.java)
            startService(intent)   // to stop sevice just send same intent and pass to stopService(intent)
        }
    }

    //Used for receiving notifications when the device location has changed. These methods are called when the listener has been registered with the LocationManager.
}
