package ru.vvdev.phonemask

import android.content.Context
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.MaskFormatWatcher

open class PhoneTextWatcher(private val editText: EditText) : TextWatcher {

    private var lastFormat: Format? = null
    private var formatWatcher: MaskFormatWatcher? = null
    private var formats: List<Format> = listOf()
    private var blocked = false
    private val handler = Handler()
    private var context: Context? = null

    init {
        context = editText.context
        val formatsJson = context?.assets?.open("formats.json")?.bufferedReader()?.readText() ?: ""
        formats = Gson().fromJson(formatsJson, object : TypeToken<List<Format>>() {}.type)

        editText.apply {
            setText("+")
            setSelection(1)
        }
    }

    override fun afterTextChanged(p0: Editable?) {
        p0?.let {
            if (it.isBlank()) editText.setText("+")
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        if (!blocked)
            p0?.let {
                val text = it.toString()
                if (text.isBlank()) removeFormat()
                formats.forEach {
                    if (text == it.code) {
                        if (it != lastFormat) setFormat(it)
                        else removeFormat()
                    } else if (p0.contains(it.code) && lastFormat == null && p0.last() != ' ') {
                        removeAllSpace()
                        setFormat(it, p0.last())
                    }
                }
            }
    }

    private fun removeFormat() {
        lastFormat = null
        handler.postDelayed({
            blocked = true
            editText?.removeTextChangedListener(formatWatcher)
            formatWatcher = null
            editText?.setText(editText.text.trim())
            editText?.setSelection(editText.text.length)
            unblock(0)
        }, 0)
    }

    private fun setFormat(format: Format, append: Char = ' ') {
        lastFormat = format
        val slots = UnderscoreDigitSlotsParser().parseSlots(format.format);
        formatWatcher = MaskFormatWatcher(MaskImpl.createTerminated(slots));
        blocked = true
        formatWatcher?.installOn(editText)
        if (append != ' ') editText.text.append(append)
        unblock(0)
        handler.postDelayed({ editText?.setSelection(editText?.text?.length ?: 0) }, 0)
    }

    private fun removeAllSpace() {
        blocked = true
        editText.setText(editText.text.toString().replace(" ", ""))
        unblock()
    }

    private fun unblock(delay: Long = 0L) {
        if (delay == 0L) blocked = false
        else handler.postDelayed({ blocked = false }, delay)
    }
}