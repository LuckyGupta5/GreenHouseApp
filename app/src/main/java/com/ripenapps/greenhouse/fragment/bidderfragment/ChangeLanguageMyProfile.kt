package com.ripenapps.greenhouse.fragment.bidderfragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.databinding.FragmentChangeLanguageMyProfileBinding
import com.ripenapps.greenhouse.screen.bidder.Home
import com.ripenapps.greenhouse.screen.seller.HomeSeller
import com.ripenapps.greenhouse.utills.Constants.SELECTED_LANGUAGE
import com.ripenapps.greenhouse.utills.Preferences

class ChangeLanguageMyProfile(val path: String) : BottomSheetDialogFragment() {

    private lateinit var changeLanguageMyProfileBinding: FragmentChangeLanguageMyProfileBinding
    private lateinit var mContext: Context
    private var language: String = "en"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        changeLanguageMyProfileBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_change_language_my_profile, container, false
        )

        if (Preferences.getStringPreference(mContext, "language") == "ar") {
            changeLanguageMyProfileBinding.apply {
                btnLangArabic.setImageResource(R.drawable.ic_lang_selected)
                btnLangEnglish.setImageResource(R.drawable.ic_lang_unselected)
            }
        } else {
            changeLanguageMyProfileBinding.apply {
                btnLangEnglish.setImageResource(R.drawable.ic_lang_selected)
                btnLangArabic.setImageResource(R.drawable.ic_lang_unselected)
            }
        }
        initListener()
        // Inflate the layout for this fragment
        isCancelable=false
        return changeLanguageMyProfileBinding.root
    }

    private fun initListener() {
        changeLanguageMyProfileBinding.apply {
            englishLangCard.setOnClickListener {
//                if (Preferences.getStringPreference(mContext, "language") != "en") {
                btnLangEnglish.setImageResource(R.drawable.ic_lang_selected)
                btnLangArabic.setImageResource(R.drawable.ic_lang_unselected)
                language = "en"
                Preferences.setStringPreference(mContext, "language", language)
                SELECTED_LANGUAGE = language
            }
//        }
        arabicLangCard.setOnClickListener {
//            if (Preferences.getStringPreference(mContext, "language") != "ar") {
                btnLangArabic.setImageResource(R.drawable.ic_lang_selected)
                btnLangEnglish.setImageResource(R.drawable.ic_lang_unselected)
                language = "ar"
                Preferences.setStringPreference(mContext, "language", language)
                SELECTED_LANGUAGE = language
            }
//        }

        btnSaveLanguage.setOnClickListener {
            Log.d("TAG", "changeLanguagePath: $path")
            if (path == "fromBidder") {
                val home = Intent(mContext, Home::class.java)
                home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(home)
                dismiss()
            } else if (path == "fromSeller") {
                val sellerHome = Intent(mContext, HomeSeller::class.java)
                sellerHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(sellerHome)
                dismiss()
            }
        }
    }
}
}