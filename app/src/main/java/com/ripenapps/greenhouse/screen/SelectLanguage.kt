package com.ripenapps.greenhouse.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivitySelectLanguageBinding
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.Constants.SELECTED_LANGUAGE
import com.ripenapps.greenhouse.utills.Preferences

class SelectLanguage : BaseActivity() {
    private lateinit var languageSelectionBinding: ActivitySelectLanguageBinding
    private var language: String = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        changeStatusBarColor(
            this@SelectLanguage, R.color.greenhousetheme
        )
        super.onCreate(savedInstanceState)
        languageSelectionBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_select_language)

        if (Preferences.getStringPreference(this@SelectLanguage, "language") == "ar") {
            languageSelectionBinding.apply {
                btnLangArabic.setImageResource(R.drawable.ic_lang_selected)
                btnLangEnglish.setImageResource(R.drawable.ic_lang_unselected)
            }
        } else {
            languageSelectionBinding.apply {
                btnLangEnglish.setImageResource(R.drawable.ic_lang_selected)
                btnLangArabic.setImageResource(R.drawable.ic_lang_unselected)
            }
        }
        initOnClickListener()
    }

    @SuppressLint("InflateParams")
    private fun initOnClickListener() {
        languageSelectionBinding.apply {
            englishLangCard.setOnClickListener {
                btnLangEnglish.setImageResource(R.drawable.ic_lang_selected)
                btnLangArabic.setImageResource(R.drawable.ic_lang_unselected)
                language = "en"
                Preferences.setStringPreference(this@SelectLanguage, "language", language)
                SELECTED_LANGUAGE = language
            }
            arabicLangCard.setOnClickListener {
                btnLangArabic.setImageResource(R.drawable.ic_lang_selected)
                btnLangEnglish.setImageResource(R.drawable.ic_lang_unselected)
                language = "ar"
                Preferences.setStringPreference(this@SelectLanguage, "language", language)
                SELECTED_LANGUAGE = language
            }
            btnContinue.setOnClickListener {
                val toOnBoardingScreen = Intent(this@SelectLanguage, OnBoarding::class.java)
                toOnBoardingScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(toOnBoardingScreen)
                finish()
            }
        }
    }
}
