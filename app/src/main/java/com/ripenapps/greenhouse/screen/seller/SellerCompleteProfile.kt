package com.ripenapps.greenhouse.screen.seller

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivitySellerCompleteProfileBinding
import com.ripenapps.greenhouse.screen.PrivacyPolicy
import com.ripenapps.greenhouse.screen.SignUp
import com.ripenapps.greenhouse.screen.TermCondition
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.Companion.Companion.USER_CONFIRM_PASSWORD
import com.ripenapps.greenhouse.utills.Companion.Companion.USER_MAIL
import com.ripenapps.greenhouse.utills.Companion.Companion.USER_NAME
import com.ripenapps.greenhouse.utills.Companion.Companion.USER_NUMBER
import com.ripenapps.greenhouse.utills.Companion.Companion.USER_PASSWORD

class SellerCompleteProfile : BaseActivity() {

    private lateinit var sellerCompleteProfileBinding: ActivitySellerCompleteProfileBinding
    private var isNameValidated = false
    private var isPasswordValidated = false
    private var isConfirmValidated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@SellerCompleteProfile, R.color.status_bar)
        sellerCompleteProfileBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_seller_complete_profile)

        initSpannable()
        initListener()
        validationObserver()
        val number = intent.getStringExtra("NUMBER") ?: ""
        Log.d("TAG", "onCreate: $number")
        Log.d("TAG", "binding: ${sellerCompleteProfileBinding.sellerMobileNumberComplete}")
        sellerCompleteProfileBinding.sellerMobileNumberComplete.setText(number)
        sellerCompleteProfileBinding.sellerMobileNumberComplete.textDirection(View.TEXT_DIRECTION_LTR)
        sellerCompleteProfileBinding.apply {
            sellerFullName.setTitle(getString(R.string.full_name))
            sellerFullName.setHint(getString(R.string.enter_full_name))
            sellerFullName.setInputType(false, InputType.TYPE_TEXT_FLAG_CAP_WORDS)
            sellerFullName.setWidth()
            sellerFullName.setTypeFace(
                this@SellerCompleteProfile, R.font.euclid_circular_regular
            )
            sellerFullName.setValidationValue(isNameValidated)
            sellerFullName.setImeAction("next")
            sellerMobileNumberComplete.setTitle(getString(R.string.mobile_number))
            sellerMobileNumberComplete.setHint(getString(R.string.please_enter_a_mobile_number))
            sellerMobileNumberComplete.setInputType(
                false, InputType.TYPE_CLASS_NUMBER
            )
            sellerMobileNumberComplete.setWidth()
            sellerMobileNumberComplete.setTypeFace(
                this@SellerCompleteProfile, R.font.euclid_circular_medium
            )
            sellerMobileNumberComplete.isEnable(false)
            sellerMobileNumberComplete.setImeAction("next")

            sellerEmail?.setTitle(getString(R.string.email))
            sellerEmail?.setHint(getString(R.string.email_address_optional))
            sellerEmail?.setInputType(
                false, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            )
            sellerEmail?.setWidth()
            sellerEmail?.setTypeFace(
                this@SellerCompleteProfile, R.font.euclid_circular_regular
            )
            sellerEmail?.setImeAction("next")

            sellerPassword?.setTitle(getString(R.string.password))
            sellerPassword?.setHint(getString(R.string.enter_password))
            sellerPassword?.setInputType(true, InputType.TYPE_TEXT_VARIATION_PASSWORD)
            sellerPassword?.setWidth()
            sellerPassword?.setTypeFace(
                this@SellerCompleteProfile, R.font.euclid_circular_regular
            )
            sellerPassword?.eyeIcon(true)
            sellerPassword?.setValidationValue(isPasswordValidated)
            sellerPassword?.setImeAction("next")

            sellerConfirmPassword?.setTitle(getString(R.string.confirm_password))
            sellerConfirmPassword?.setHint(getString(R.string.enter_confirm_password))
            sellerConfirmPassword?.setInputType(true, InputType.TYPE_TEXT_VARIATION_PASSWORD)
            sellerConfirmPassword?.setWidth()
            sellerConfirmPassword?.setTypeFace(
                this@SellerCompleteProfile, R.font.euclid_circular_regular
            )
            sellerConfirmPassword?.eyeIcon(true)
            sellerConfirmPassword?.setValidationValue(isConfirmValidated)
            sellerConfirmPassword?.setImeAction("done")
        }
    }

    private fun initSpannable() {
        val terms1 = sellerCompleteProfileBinding.termAndCondition
        val textLine = getString(R.string.terms_and_condition_privacy_policy)
        val ss = SpannableString(textLine)

        val termsText = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val toTermCondition = Intent(this@SellerCompleteProfile, TermCondition::class.java)
                startActivity(toTermCondition)
            }
        }

        val dataText = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val toPrivacyPolicy = Intent(this@SellerCompleteProfile, PrivacyPolicy::class.java)
                startActivity(toPrivacyPolicy)
            }
        }
        // Adjust indices based on your actual string content
        ss.setSpan(termsText, 33, 52, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(dataText, 56, 70, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        terms1.movementMethod = LinkMovementMethod.getInstance()
        terms1.text = ss
    }

    private fun validationObserver() {
        sellerCompleteProfileBinding.apply {
            sellerFullName?.getValidatedValue()?.observe(this@SellerCompleteProfile) { validated ->
                isNameValidated = validated
                checkValidation()
            }
            sellerPassword?.getValidatedValue()?.observe(this@SellerCompleteProfile) { validated ->
                isPasswordValidated = validated
                checkValidation()
            }
            sellerConfirmPassword?.getValidatedValue()
                ?.observe(this@SellerCompleteProfile) { validated ->
                    isConfirmValidated = validated
                    checkValidation()
                }
        }
    }

    private fun checkValidation() {
        if (isNameValidated && isPasswordValidated && isConfirmValidated) {
            sellerCompleteProfileBinding.continueBtn.isEnabled = true
            changeButtonColor()
        } else {
            sellerCompleteProfileBinding.continueBtn.isEnabled = false
        }
    }

    private fun changeButtonColor() {
        sellerCompleteProfileBinding.apply {
            continueBtn.setBackgroundColor(
                ContextCompat.getColor(
                    this@SellerCompleteProfile, R.color.greenhousetheme
                )
            )
        }
    }

    private fun initListener() {

        sellerCompleteProfileBinding.apply {
            sellerFullName?.initListener("other")
            sellerEmail?.initListener("email")
            sellerPassword?.initListener("password")
            sellerConfirmPassword?.initListener("password", sellerPassword?.getText())
        }

        sellerCompleteProfileBinding.cross.setOnClickListener {
            val backToSignUp = Intent(this, SignUp::class.java)
            startActivity(backToSignUp)
            finish()
        }
        sellerCompleteProfileBinding.continueBtn.setOnClickListener {
            val pass = sellerCompleteProfileBinding.sellerPassword?.getText()
            val confirmPass = sellerCompleteProfileBinding.sellerConfirmPassword?.getText()

            if (pass != confirmPass) {
                sellerCompleteProfileBinding.sellerConfirmPassword?.setError(getString(R.string.password_does_not_match))
            } else {
                val toSellerCompleteProfile2 =
                    Intent(this@SellerCompleteProfile, SellerCompleteProfile2::class.java)
                toSellerCompleteProfile2.putExtra(
                    USER_NAME, sellerCompleteProfileBinding.sellerFullName?.getText()
                )
                toSellerCompleteProfile2.putExtra(
                    USER_NUMBER, sellerCompleteProfileBinding.sellerMobileNumberComplete?.getText()
                )
                toSellerCompleteProfile2.putExtra(
                    USER_MAIL, sellerCompleteProfileBinding.sellerEmail?.getText()
                )
                toSellerCompleteProfile2.putExtra(
                    USER_PASSWORD, sellerCompleteProfileBinding.sellerPassword?.getText()
                )
                toSellerCompleteProfile2.putExtra(
                    USER_CONFIRM_PASSWORD,
                    sellerCompleteProfileBinding.sellerConfirmPassword?.getText()
                )
                startActivity(toSellerCompleteProfile2)
            }
        }
    }
}