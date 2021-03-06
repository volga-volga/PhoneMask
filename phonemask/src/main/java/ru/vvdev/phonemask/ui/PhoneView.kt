package ru.vvdev.phonemask.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_pick_format.view.*
import ru.vvdev.phonemask.R
import ru.vvdev.phonemask.model.Format
import ru.vvdev.phonemask.util.getDrawableForFormat
import ru.vvdev.phonemask.watcher.PhoneTextWatcher


class PhoneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle), PhoneListener {

    private val editText: EditText
    private val countryFlag: ImageView
    private var listener: PhoneListener? = null
    private var phoneTextWatcher: PhoneTextWatcher

    init {
        orientation = HORIZONTAL

        var defaultCode = ""
        var flagEnabled = true
        var lineVisible = true
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

            flagEnabled = try {
                a.getBoolean(R.styleable.PhoneView_flagEnabled, true)
            } catch (e: Exception) {
                true
            }

            lineVisible = try {
                a.getBoolean(R.styleable.PhoneView_lineVisible, true)
            } catch (e: Exception) {
                true
            }
        }

        editText = initEditText(lineVisible)
        countryFlag = initFlag()

        phoneTextWatcher = PhoneTextWatcher(
            editText,
            this@PhoneView,
            defaultCode
        )
        editText.addTextChangedListener(phoneTextWatcher)
        if (flagEnabled) addView(countryFlag)
        addView(editText)
    }

    fun setListener(listener: PhoneListener) {
        this.listener = listener
    }

    fun getValue(withFormat: Boolean = false): String {
        return if (withFormat) editText.text.toString()
        else editText.text.toString()
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .trim()
    }

    fun setValue(value: String) {
        val text = value.replace(Regex("[^0-9.]"), "")
        editText.text.delete(1, editText.length())
        for (i in text.indices) editText.text.append(text[i].toString())
    }

    override fun formatChanged(format: Format) {
        context?.let {
            it.getDrawableForFormat(format)?.let {
                countryFlag.setImageDrawable(it)
            }
        }
    }

    private fun initEditText(lineVisible: Boolean) =
        AppCompatEditText(context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(16, 0, 0, 0)
            }
            if (!lineVisible) setBackgroundResource(android.R.color.transparent)
        }

    private fun initFlag() =
        ImageView(context).apply {
            layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                setMargins(0, 4, 0, 0)
            }
            gravity = Gravity.CENTER

            setOnClickListener {
                showPickDialog()
            }
        }

    private fun showPickDialog() {

        val dialog = AlertDialog.Builder(context)
            .create()


        val dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_pick_format, null)
        val formatsJson = context?.assets?.open("formats.json")?.bufferedReader()?.readText() ?: ""
        val formats: List<Format> =
            Gson().fromJson(formatsJson, object : TypeToken<List<Format>>() {}.type)
        dialogView.rvFormats.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = FormatAdapter(
                context,
                formats,
                object : FormatAdapter.Listener {
                    override fun formatClicked(format: Format) {
                        phoneTextWatcher.setFormatFromPicker(format)
                        dialog.dismiss()
                    }
                })
        }
        dialog.setView(dialogView)
        dialog.show()
    }
}
