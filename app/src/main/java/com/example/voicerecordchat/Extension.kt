package com.example.voicerecordchat

import android.content.Context
import java.util.concurrent.TimeUnit


fun Long.toMinutes(): Int {

    return TimeUnit.MILLISECONDS.toMinutes(this).toInt()

}

fun Long.toSeconds(): Int {

    return TimeUnit.MILLISECONDS.toSeconds(this).toInt()

}

fun Long.toFormattedDuration(context: Context):String{

    val minutes = this / 1000 / 60
    val seconds = this / 1000 % 60

    return context.getString(
        R.string.text_record_time,
        minutes,
        seconds
    )
}