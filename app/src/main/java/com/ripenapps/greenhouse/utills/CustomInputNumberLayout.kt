package com.ripenapps.greenhouse.utills

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
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import com.ripenapps.greenhouse.R

class CustomInputNumberLayout : RelativeLayout {
    private lateinit var relativeLayout: RelativeLayout
    private lateinit var titleTextView: TextView
    private var backgroundTextView: TextView? = null
    private lateinit var inputEditText: EditText
    var placeholder: String? = null
    private lateinit var error: TextView
    private lateinit var ccp: TextView


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
        inflater.inflate(R.layout.custom_input_number_layout, this)

        titleTextView = findViewById(R.id.numberTitleTextView)
        inputEditText = findViewById(R.id.number)
        backgroundTextView = findViewById(R.id.numberWhiteBackground)
        relativeLayout = findViewById(R.id.numberEditTextLayer)
        ccp = findViewById(R.id.ccp)
        error = findViewById(R.id.numberError)



        inputEditText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus || inputEditText.text.isNotEmpty()) {
                titleTextView.visibility = View.VISIBLE
                backgroundTextView?.visibility = View.VISIBLE
                inputEditText.setHint("")
                relativeLayout.elevation = 0f

            } else {
                titleTextView.visibility = View.GONE
                backgroundTextView?.visibility = View.GONE
                inputEditText.setHint(placeholder)
                relativeLayout.elevation = 5f

            }
        }

        inputEditText.setOnClickListener { inputEditText.requestFocus() } //focusing on the edit text view

//        inputEditText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
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

    fun initListener(useCase: String) {
        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                regex(useCase)
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

    // Method to get text from EditText
    fun getText(): String {
        Log.d("TAG", "getTextMobile: $inputEditText.text.toString()")
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

    fun setHint(hint: String) {
        inputEditText.hint = hint
        placeholder = hint
    }

    fun setTypeFace(mContext: Context, resId: Int) {
        inputEditText.typeface = ResourcesCompat.getFont(mContext, resId)
    }

    fun isEnable(enable: Boolean) {
        inputEditText.isEnabled = enable
    }

    fun setTextColor(color: Int) {
        val color = ContextCompat.getColor(context, color)
        inputEditText.setTextColor(color)
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


    fun setInputType(isPassword: Boolean, type: Int) {
        if (!isPassword) {
            inputEditText.inputType = type
        } else {
            inputEditText.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        }
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

    private var isValidated: MutableLiveData<Boolean> = MutableLiveData(false)

    @SuppressLint("SetTextI18n")
    fun regex(useCase: String) {
        when (useCase) {
            "mobile" -> {
                if (inputEditText.text.isEmpty()) {
                    error.text =
                        inputEditText.context.getString(R.string.enter) + " ${titleTextView.text}"
                    error.setVisibility(true)
                    relativeLayout.setBackgroundResource(R.drawable.error_background)
                    isValidated.value = false
                } else if (!CommonUtils.isPhoneNumberValid(inputEditText.text.toString())) {
                    error.text =
                        inputEditText.context.getString(R.string.enter_valid) + " ${titleTextView.text}"
                    error.setVisibility(true)
                    relativeLayout.setBackgroundResource(R.drawable.error_background)
                    isValidated.value = false
                } else {
                    isValidated.value = true
                    relativeLayout.setBackgroundResource(R.drawable.ebebeb_border)
                    error.setVisibility(false)
                }
            }
        }
    }

    fun setValidationValue(value: Boolean) {
        isValidated.value = value
    }

    fun setMaxLength(length: Int) {
        inputEditText.filters = arrayOf(InputFilter.LengthFilter(length))
    }

    fun getValidatedValue(): MutableLiveData<Boolean> {
        return isValidated
    }

    fun textDirection(direction: Int) {
        ccp.textDirection = direction
    }

    fun clearText(boolean: Boolean) {
        if (boolean)
            inputEditText.setText("")
    }
    // Add more methods as needed for customization
}