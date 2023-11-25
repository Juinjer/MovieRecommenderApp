package com.example.movierecommender;

import android.app.Activity
import android.content.Context
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

    fun getName(namesUsed: List<String>): String { // TODO put in server but just for testing in client
        val templist = names.toMutableList()
        for (i in 0..names.size) {
            val idx = Random.nextInt(0, templist.size)
            if (templist[idx] !in namesUsed) {
                return templist[idx]
            } else {
                templist.removeAt(idx)
            }
        }
        return names[0] + "1" //TODO
    }
}
