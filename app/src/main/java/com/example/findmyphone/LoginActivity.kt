package com.example.findmyphone

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {

    var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        var sharedPref = getSharedPreferences("userdata", Context.MODE_PRIVATE)
        mAuth = FirebaseAuth.getInstance()
        signInAnonymously()
    }

    fun signInAnonymously() {
        mAuth!!.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(
                        applicationContext, "Authentication Successful.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val user = mAuth!!.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        applicationContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
    }

    // store mobile no in firebase realtime database and in shared preference(allow you to save and retrieve data in the form of key,value pair)
    fun buRegisterEvent(view: View) {
        val userData = UserData(this)
        userData.savePhoneNo(findViewById<EditText>(R.id.etPhoneNumber).text.toString())
        // get date time
        val df = SimpleDateFormat("yyyy/MMM/dd HH:MM:ss")
        val date = Date()
        // save to database
        val mDatabase = FirebaseDatabase.getInstance().reference
        mDatabase.child("Users").child(findViewById<EditText>(R.id.etPhoneNumber).text.toString())
            .child("request").setValue(df.format(date).toString())
        mDatabase.child("Users").child(findViewById<EditText>(R.id.etPhoneNumber).text.toString())
            .child("Finders").setValue(df.format(date).toString())
        finish() // finish the activity because main activity is running in background
    }

}