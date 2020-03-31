package ru.vvdev.phonemask

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_pick_format.view.*


class PhoneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle), PhoneListener {

    private val editText: EditText
    private val countryName: TextView
    private var listener: PhoneListener? = null
    private var phoneTextWatcher: PhoneTextWatcher

    init {
        orientation = HORIZONTAL
        editText = initEditText()
        countryName = initCountry()

        var defaultCode = ""
        attrs?.let {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.PhoneView,
                0, 0
            )
            defaultCode = try {
                a.getString(R.styleable.PhoneView_defaultCode) ?: ""
            } catch (e: Exception) {
                ""
            }

        }

        phoneTextWatcher = PhoneTextWatcher(editText, this@PhoneView, defaultCode)
        editText.addTextChangedListener(phoneTextWatcher)
        addView(countryName)
        addView(editText)
    }

    fun setListener(listener: PhoneListener) {
        this.listener = listener
    }

    override fun formatChanged(format: Format) {
        countryName.text = format.name
    }

    private fun initEditText() =
        AppCompatEditText(context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(16, 0, 0, 0)
            }
        }

    private fun initCountry() =
        TextView(context).apply {
            layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            filters = filters.plus(InputFilter.LengthFilter(10))
            text = "country"

            setOnClickListener {
                showPickDialog()
            }
        }

    private fun showPickDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_pick_format, null)
        val formatsJson = context?.assets?.open("formats.json")?.bufferedReader()?.readText() ?: ""
        val formats: List<Format> =
            Gson().fromJson(formatsJson, object : TypeToken<List<Format>>() {}.type)
        dialogView.rvFormats.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = FormatAdapter(formats)
        }
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        dialog.show()
    }
}
