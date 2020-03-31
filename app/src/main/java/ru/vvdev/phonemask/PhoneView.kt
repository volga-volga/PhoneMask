package ru.vvdev.phonemask

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText

class PhoneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val editText: EditText
    private val countryName: TextView

    init {
        orientation = HORIZONTAL
        editText = initEditText()
        countryName = initCountry()

        addView(countryName)
        addView(editText)
    }

    private fun initEditText() =
        AppCompatEditText(context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(16, 0, 0, 0)
            }
            addTextChangedListener(PhoneTextWatcher(this))
        }

    private fun initCountry() =
        TextView(context).apply {
            layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            filters = filters.plus(InputFilter.LengthFilter(10))
            text = "country"

            setOnClickListener {

            }
        }
}
