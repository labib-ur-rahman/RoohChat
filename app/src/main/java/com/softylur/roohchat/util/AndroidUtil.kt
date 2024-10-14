package com.softylur.roohchat.util

import android.content.Context
import android.widget.Toast

class AndroidUtil {

    companion object {
        fun lToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}