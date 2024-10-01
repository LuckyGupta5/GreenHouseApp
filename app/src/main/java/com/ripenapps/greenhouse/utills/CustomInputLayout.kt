package com.ripenapps.greenhouse.utills

// CustomInputLayout.java

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.utills.CommonUtils.passWordModeOnOrOff

@Suppress("NAME_SHADOWING")
class CustomInputLayout : RelativeLayout {
    private lateinit var relativeLayout: RelativeLayout
    private lateinit var titleTextView: TextView
    private var backgroundTextView: TextView? = null
    private lateinit var inputEditText: EditText
    var placeholder: String? = null
    private lateinit var error: TextView
    private var validationInterface: ValidationInterface? = null
    private lateinit var eyeIcon: ImageView
    private var eyeOn: Boolean = false

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.custom_input_layout, this)

        titleTextView = findViewById(R.id.titleTextView)
        inputEditText = findViewById(R.id.inputEditText)
        backgroundTextView = findViewById(R.id.whiteBackground)
        relativeLayout = findViewById(R.id.editTextLayer)
        error = findViewById(R.id.error)
        eyeIcon = findViewById(R.id.eyeIcon)

        inputEditText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus || inputEditText.text.isNotEmpty()) {
                titleTextView.visibility = View.VISIBLE
                backgroundTextView?.visibility = View.VISIBLE
                inputEditText.setHint("")
                relativeLayout.elevation = 0f
//                error.setVisibility(true)

            } else {
                titleTextView.visibility = View.GONE
                backgroundTextView?.visibility = View.GONE
                inputEditText.setHint(placeholder)
                relativeLayout.elevation = 5f
                error.setVisibility(false)

            }
        }

        inputEditText.setOnClickListener { inputEditText.requestFocus() } //focusing on the edit text view

//        inputEditText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                regex("password")
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                setWidth()
//                if (!s.isNullOrEmpty() || inputEditText.hasFocus()) {
//                    titleTextView.visibility = View.VISIBLE
//                    backgroundTextView?.visibility = View.VISIBLE
//                    inputEditText.setHint("")
//                    relativeLayout.elevation = 0f
//                } else {
//                    titleTextView.visibility = View.GONE
//                    backgroundTextView?.visibility = View.GONE
//                    inputEditText.setHint(placeholder)
//                    relativeLayout.elevation = 5f
//                }
//            }
//        })
    }

    // Method to get text from EditText
    fun getText(): String {
        return inputEditText.text.toString()
    }

    // Method to set text to EditText
    fun setText(text: String) {
        inputEditText.setText(text)
    }

    // Method to set title
    fun setTitle(title: String) {
        titleTextView.text = title
    }

    fun hideTitle() {
        titleTextView.setVisibility(false)
    }

    fun setHint(hint: String) {
        inputEditText.hint = hint
        placeholder = hint
    }

    fun eyeIcon(status: Boolean) {
        inputEditText.setPaddingEnd(R.dimen.dimen_40)
        eyeIcon.setVisibility(status)
    }

    fun setTypeFace(mContext: Context, resId: Int) {
        inputEditText.typeface = ResourcesCompat.getFont(mContext, resId)
    }

    fun setInputType(isPassword: Boolean, type: Int) {
        if (!isPassword) {
            inputEditText.inputType = type
        } else {
            inputEditText.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        }
    }

    fun setImeAction(status: String) {
        when (status) {
            "next" -> {
                inputEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
            }

            else -> {
                inputEditText.imeOptions = EditorInfo.IME_ACTION_DONE
            }
        }
    }

    fun setTextColor(color: Int) {
        val color = ContextCompat.getColor(context, color)
        inputEditText.setTextColor(color)
    }

    fun isEnable(enable: Boolean) {
        inputEditText.isEnabled = enable
    }

    fun setMaxLength(length: Int) {
        inputEditText.filters = arrayOf(InputFilter.LengthFilter(length))
    }

    fun setMaxLines(lines: Int) {
        inputEditText.maxLines = lines
    }

    fun isFocusable(isFocused: Boolean) {
        inputEditText.isFocusable = isFocused
    }

    fun setWidth() {

        titleTextView.measure(0, 0)
        val width = titleTextView.measuredWidth

// Set the width of titleTextView2 to match titleTextView1
        val layoutParams = backgroundTextView?.layoutParams
        layoutParams?.width = width
        backgroundTextView?.layoutParams = layoutParams
        Log.d("TAG", "setWidth: $layoutParams")
    }

    fun setError(errorString: String) {
        error.text = errorString
        relativeLayout.setBackgroundResource(R.drawable.error_background)
        error.setVisibility(true)
    }

    @SuppressLint("SetTextI18n")
    fun regex(useCase: String, confirmPass: String? = null) {
        when (useCase) {
            "password" -> {
                if (inputEditText.text.toString().isEmpty()) {
                    relativeLayout.setBackgroundResource(R.drawable.error_background)
                    error.setVisibility(true)
                    isValidated.value = false
                    error.text = inputEditText.context.getString(R.string.enter_password)
                    return
                }
                if (CommonUtils.isValidPassword(inputEditText.text.toString())) {
                    validationInterface?.validation(true)
                    relativeLayout.setBackgroundResource(R.drawable.ebebeb_border)
                    error.setVisibility(false)
                    isValidated.value = true
                } else if (!CommonUtils.isValidPassword(inputEditText.text.toString())) {
                    relativeLayout.setBackgroundResource(R.drawable.error_background)
                    error.text =
                        inputEditText.context.getString(R.string.error_message)
                    error.setVisibility(true)
                    isValidated.value = false
                } else if (inputEditText.text.toString() != confirmPass) {
                    error.text = inputEditText.context.getString(R.string.password_does_not_match)
                    relativeLayout.setBackgroundResource(R.drawable.error_background)
                    error.setVisibility(true)
                    isValidated.value = false
                }
            }

            "email" -> {
                if (CommonUtils.isEmailValid(inputEditText.text.toString())) {
                    error.setVisibility(false)
                    relativeLayout.setBackgroundResource(R.drawable.ebebeb_border)
                    isValidated.value = true
                } else {
                    error.text = inputEditText.context.getString(R.string.enter_valid_email)
                    error.setVisibility(true)
                    relativeLayout.setBackgroundResource(R.drawable.error_background)
                    isValidated.value = false
                }

            }

            "mobile" -> {
                CommonUtils.isPhoneNumberValid(inputEditText.text.toString())
            }

            "other" -> {
                if (inputEditText.text.isEmpty()) {
                    error.text =
                        inputEditText.context.getString(R.string.enter) + " ${titleTextView.text.ifEmpty { inputEditText.context.getString(R.string.trade_license_number)} }"
                    relativeLayout.setBackgroundResource(R.drawable.error_background)
                    error.setVisibility(true)
                    isValidated.value = false
                } else {
                    isValidated.value = true
                    relativeLayout.setBackgroundResource(R.drawable.ebebeb_border)
                    error.setVisibility(false)
                }
            }
        }
    }

    fun initListener(useCase: String, confirmPass: String? = null) {

        eyeIcon.setOnClickListener {
            eyeOn = !eyeOn
            inputEditText.passWordModeOnOrOff(eyeOn, eyeIcon)
        }

        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                regex(useCase, confirmPass ?: "")
                setWidth()
                if (!s.isNullOrEmpty() || inputEditText.hasFocus()) {
                    titleTextView.visibility = View.VISIBLE
                    backgroundTextView?.visibility = View.VISIBLE
                    inputEditText.setHint("")
                    relativeLayout.elevation = 0f
//                    error.setVisibility(true)
                } else {
                    titleTextView.visibility = View.GONE
                    backgroundTextView?.visibility = View.GONE
                    inputEditText.setHint(placeholder)
                    relativeLayout.elevation = 5f
                    relativeLayout.setBackgroundResource(R.drawable.ebebeb_border)
                    error.setVisibility(false)
                }
            }
        })
    }

    private var isValidated: MutableLiveData<Boolean> = MutableLiveData(false)

    fun setValidationValue(value: Boolean) {
        isValidated.value = value
    }

    fun getValidatedValue(): MutableLiveData<Boolean> {
        return isValidated
    }
// Add more methods as needed for customization
}

