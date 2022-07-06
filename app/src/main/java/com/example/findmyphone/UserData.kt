package com.example.findmyphone

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class UserData(context: Context) {
    var context: Context? = context
    var sharedPRef: SharedPreferences? = null

    init {
        this.sharedPRef = context.getSharedPreferences("userData", Context.MODE_PRIVATE)
    }

    fun savePhoneNo(phoneNumber: String) {
        val editor = sharedPRef!!.edit()
        editor.putString("phoneNumber", phoneNumber)
        editor.apply()
    }

    fun loadPhoneNumber(): String? {
        val phoneNumber = sharedPRef!!.getString("phoneNumber", "empty")

        return phoneNumber
    }

    fun isFirstTimeLoad() {
        val phoneNumber = sharedPRef!!.getString("phoneNumber", "empty")
        if (phoneNumber.equals("empty")) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startActivity(intent)
        }
    }

    // save contact info in shared Reference
    fun saveContactInfo() {
        var listOfTracker = ""
        for ((key, value) in myTrackers) {
            if (listOfTracker.length == 0) { // first time called no
                listOfTracker = key + "%" + value
            } else {
                listOfTracker += "%" + key + "%" + value
            }
        }
        if (listOfTracker.length == 0) {
            listOfTracker =
                "empty" //  if user doesn't select any contact or not, use to check later on
        }
        val editor = sharedPRef!!.edit()
        editor.putString("mytrackers", listOfTracker)
        editor.apply()
    }

    fun loadContactInfo() {
        myTrackers.clear()
        val listOfTracker = sharedPRef!!.getString("listOfTracker", "empty")
        if (!listOfTracker.equals("empty")) {
            val usersInfo = listOfTracker?.split("%")?.toTypedArray()
            var i = 0
            if (usersInfo != null) {
                while (i < usersInfo.size) {
                    myTrackers.put(usersInfo[i], usersInfo[i + 1])
                    i += 2

                }
            }
        }
    }

    // save my tracker in shared reference
    companion object {
        var myTrackers: MutableMap<String, String> = HashMap()
        fun formatPhoneNumber(phoneNumber: String): String {
            var onlyNumber =
                phoneNumber.replace("[^0-9]".toRegex(), "") // it will remove non digit character
            if (phoneNumber[0] == '+') {
                onlyNumber = "+" + phoneNumber
            }
            return onlyNumber
        }

    }
}