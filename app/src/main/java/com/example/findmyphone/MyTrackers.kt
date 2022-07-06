package com.example.findmyphone

import android.app.Activity
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_my_trackers.*
import java.lang.Math.abs

class MyTrackers : AppCompatActivity() {
    var adapter: ContactAdapter? = null
    var listOfContact = ArrayList<UserContact>()
    var userData: UserData? = null
    private var allNumLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val contactData = result.data?.data
                contactData?.let {
                    val cursor = contentResolver.query(contactData, null, null, null, null)
                    cursor?.let {
                        if (it.moveToFirst()) {
                            val name =
                                it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                            if (Integer.parseInt(it.getString(abs(it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)))) > 0) {
                                val id =
                                    it.getString(abs(it.getColumnIndex(ContactsContract.Contacts._ID)))
                                val phoneCursor = contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,
                                    null,
                                    null
                                )
                                phoneCursor?.let {
                                    while (phoneCursor.moveToNext()) {
                                        var phoneNumber = phoneCursor.getString(
                                            abs(
                                                phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                            )
                                        )
                                        val name = phoneCursor.getString(
                                            abs(
                                                it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                                            )
                                        )
                                        phoneNumber = UserData.formatPhoneNumber(phoneNumber)
                                        UserData.myTrackers.put(phoneNumber, name)
                                        refreshData()
                                        // save contact to shared reference
                                        userData!!.saveContactInfo()
                                        // save to realTime database
                                        val mDatabase = FirebaseDatabase.getInstance().reference
                                        val userData = UserData(applicationContext)
                                        userData.loadPhoneNumber()?.let {
                                            mDatabase.child("Users").child(phoneNumber)
                                                .child("Finders").child(it).setValue(true)
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    this@MyTrackers,
                                    "$name-No Numbers",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        cursor.close()
                    }


                }


            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_trackers)
        userData = UserData(this)
        adapter = ContactAdapter(this, listOfContact)
        lvContactlists.adapter = adapter
        // when you click on any contact it should delete that one and after that it should refresh
        lvContactlists.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, postion, id ->
                val userInfo = listOfContact[postion]
                UserData.myTrackers.remove(userInfo.phoneNumber)
                refreshData()
                // save to shared reference (after any change save)
                userData!!.saveContactInfo()
                // remove to realTime database
                val mDatabase = FirebaseDatabase.getInstance().reference
                val userData = UserData(applicationContext)
                userData.loadPhoneNumber()?.let {
                    userInfo.phoneNumber?.let { it1 ->
                        mDatabase.child("Users").child(it1).child("Finders").child(
                            it
                        ).removeValue()
                    }
                }


            }
        userData!!.loadContactInfo()
        refreshData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.tracker_menu, menu)
        return true
    }

    // generate dummy phone no
    fun dummyData() {
//         listOfContact.add(UserContact("Vidit","913549852"))
//         listOfContact.add(UserContact("Ekshita","91302933"))
//         listOfContact.add(UserContact("Yash","91905852"))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.finishActivity -> {
                finish()
            }
            R.id.addContact -> {
                checkPermission()
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    val Contact_Code = 123 // example or dummy
    fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), Contact_Code)
            return
        }

        pickContact()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Contact_Code -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickContact()
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
    val PICK_CODE = 1234
    private fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        allNumLauncher.launch(intent)
    }


    // refresh contacts
    fun refreshData() {
        listOfContact.clear()
        for ((key, value) in UserData.myTrackers) {
            listOfContact.add(UserContact(value, key))
        }
        adapter!!.notifyDataSetChanged()
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