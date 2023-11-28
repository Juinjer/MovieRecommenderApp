package com.example.movierecommender;

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.View;
import android.view.inputmethod.InputMethodManager
import kotlin.random.Random

object UtilsM {
    private var names = listOf(
        "Whispering Fox", "Silent Phoenix", "Mystery Hawk", "Shadowed Tiger",
        "Unknown Sparrow", "Veiled Cobra", "Masked Griffin", "Ghostly Pegasus",
        "Cloaked Raven", "Enigma Lynx"
    )

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun getEndPoint(context: Context): String {
        val ai: ApplicationInfo = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData.getString("webServerUrl")
        return value.toString()
    }
}
