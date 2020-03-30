package ru.vvdev.phonemask

import android.content.Context
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.Log
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
            keyListener = DigitsKeyListener.getInstance("0123456789+ ()-")
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
            p0?.let { p0 ->
                val text = p0.toString()
                val clearedText = getClearedNumber()
                if (text.isBlank()) removeFormat()
                Log.d("MaskTag", "text = $text")
                formats.forEach {
                    if (text == it.code) {
                        if (it != lastFormat) {
                            Log.d("MaskTag", "it != lastFormat setFormat")
                            setFormat(it)
                        } else {
                            Log.d("MaskTag", "it == lastFormat removeFormat")
                            removeFormat()
                        }
                    } else if (p0.contains(it.code) && lastFormat == null && p0.last() != ' ') {
                        Log.d("MaskTag", "removeAllSpace setFormat")
                        removeAllSpace()
                        setFormat(it, p0.last())
                    } else if(p0.contains(it.code) && lastFormat != null && p0.last() == '('){
                        Log.d("MaskTag", "here")
                        removeFormat()
                    } else if(clearedText.contains(it.code) && lastFormat != null && it.code.length > lastFormat!!.code.length){
                        Log.d("MaskTag", "here2, ${it.code}, last = ${lastFormat?.code}")
                        removeFormat(it.code)
                        return
                    }
                }
            }
    }

    private fun removeFormat(code: String = "") {
        lastFormat = null
        blocked = true
        handler.postDelayed({
            editText?.removeTextChangedListener(formatWatcher)
            formatWatcher = null
            if(code.isBlank()) editText?.setText(getClearedNumber())
            else editText?.setText(code)
            editText?.setSelection(editText.text.length)
            unblock(100)
        }, 0)
    }

    private fun setFormat(format: Format, append: Char = ' ') {
        lastFormat = format
        val slots = UnderscoreDigitSlotsParser().parseSlots(format.format);
        formatWatcher = MaskFormatWatcher(MaskImpl.createTerminated(slots));
        blocked = true
        formatWatcher?.installOn(editText)
        if (append != ' ') editText.text.append(append)
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
}