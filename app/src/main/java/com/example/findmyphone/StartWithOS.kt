package com.example.findmyphone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StartWithOs:BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
         if(intent!!.action.equals("android.intent.action.BOOT_COMPLETED")){
             // start the service
             val intent=Intent(context,MyService::class.java)
             context!!.startService(intent)
         }
    }

}