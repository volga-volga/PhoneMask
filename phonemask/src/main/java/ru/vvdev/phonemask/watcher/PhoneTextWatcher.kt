package ru.vvdev.phonemask.watcher

import android.content.Context
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.widget.EditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.vvdev.phonemask.model.Format
import ru.vvdev.phonemask.ui.PhoneListener
import java.util.*

open class PhoneTextWatcher(
    private val editText: EditText, var listener: PhoneListener? = null,
    private val defaultFormat: String = "+1"
) : TextWatcher {

    private var lastFormat: Format? = null
    private var formatWatcher: SafeMaskFormatWatcher? = null
    private var formats: List<Format> = listOf()
    private var blocked = false
    private val handler = Handler()
    private var context: Context? = null

    init {
        context = editText.context
        val formatsJson = context?.assets?.open("formats.json")?.bufferedReader()?.readText() ?: ""
        formats = Gson().fromJson(formatsJson, object : TypeToken<List<Format>>() {}.type)

        swapElements()

        editText.apply {
            keyListener = DigitsKeyListener.getInstance("0123456789+ ()-")
            val default = if (defaultFormat.isNotBlank() && defaultFormat.first() == '+')
                defaultFormat else "+1"

            setText(default)
            formats.find { it.code == default }?.let {
                listener?.formatChanged(it)
            }
            setSelection(editText.text.length)
        }
    }

    override fun afterTextChanged(p0: Editable?) {
        p0?.let {
            if (it.isBlank()) {
                editText.setText("+")
                editText.setSelection(1)
            }
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        if (!blocked)
            p0?.let { p0 ->
                val text = p0.toString()
                val clearedText = getClearedNumber()
                if (text.isBlank()) removeFormat()
                formats.forEach {
                    if (text == it.code) {
                        if (it != lastFormat) {
                            setFormat(it)
                            return
                        } else {
                            removeFormat()
                            return
                        }
                    } else if (p0.contains(it.code) && lastFormat == null && p0.last() != ' ') {
                        removeAllSpace()
                        setFormat(it, p0.last())
                        return
                    } else if (p0.contains(it.code) && lastFormat != null && p0.last() == '(') {
                        removeFormat()
                        return
                    } else if (clearedText.contains(it.code) && lastFormat != null && it.code.length > lastFormat?.code?.length ?: 0) {
                        removeFormat(it.code)
                        handler.postDelayed({setFormat(it)}, 100)
                        return
                    }
                }
            }
    }

    fun setFormatFromPicker(format: Format) {
        removeFormat(format.code)
        handler.postDelayed({ setFormat(format, ' ', true) }, 100)
    }

    private fun removeFormat(code: String = "") {
        lastFormat = null
        blocked = true
        handler.postDelayed({
            editText?.removeTextChangedListener(formatWatcher)
            formatWatcher = null
            if (code.isBlank()) editText?.setText(getClearedNumber())
            else editText?.setText(code)
            editText?.setSelection(editText.text.length)
            unblock(100)
        }, 0)
    }

    private fun setFormat(format: Format, append: Char = ' ', force: Boolean = false) {
        lastFormat = format
        listener?.formatChanged(format)
        val slots = UnderscoreDigitSlotsParser().parseSlots(format.format);
        formatWatcher = SafeMaskFormatWatcher(
            MaskImpl.createTerminated(slots)
        );
        blocked = true
        formatWatcher?.installOn(editText)
        if (append != ' ' || force) editText.text.append(append)
        unblock(100)
        handler.postDelayed({ editText?.setSelection(editText?.text?.length ?: 0) }, 0)
    }

    private fun removeAllSpace() {
        blocked = true
        editText.setText(
            editText.text.toString()
                .replace(" ", "")
                .replace("(", "")
                .replace(")", "")
                .trim()
        )
        unblock()
    }

    private fun unblock(delay: Long = 0L) {
        handler.postDelayed({ blocked = false }, delay)
    }

    private fun getClearedNumber() = editText.text.toString()
        .replace(" ", "")
        .replace("(", "")
        .replace(")", "")

    private fun swapElements() {
        formats.find { it.name == "United States" }?.let {
            Collections.swap(formats, formats.indexOf(it), 0)
        }
        formats.find { it.name == "Russia" }?.let {
            Collections.swap(formats, formats.indexOf(it), 1)
        }

    }
}