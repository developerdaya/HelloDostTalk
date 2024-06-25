package com.example.ittalk.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScreenStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                // Perform your action here for screen off
                Log.d("ScreenStateReceiver", "Screen turned off")
            }
            Intent.ACTION_SCREEN_ON -> {
                // Perform your action here for screen on
                Log.d("ScreenStateReceiver", "Screen turned on")
            }
        }
    }
}
