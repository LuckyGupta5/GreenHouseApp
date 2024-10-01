package com.ripenapps.greenhouse.abstracts

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ripenapps.greenhouse.utills.Preferences
import java.util.Locale


@Suppress("DEPRECATION")
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Preferences.getStringPreference(this@BaseActivity, "language") == "ar") {
            setLocale("ar")
        } else
            setLocale("en")
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        getResources().updateConfiguration(config, getResources().displayMetrics)
        // Example log to see locale change
        Log.d("Locale", "Locale changed to $languageCode")
    }
}