package ru.vvdev.phonemask

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.MaskFormatWatcher


class MainActivity : AppCompatActivity() {

    private var lastFormat: Format? = null
    private var finded = false
    private var formatWatcher: MaskFormatWatcher? = null
    private val emptyFormat = Format("undef", "undef", "+9______________________________")
    var formats: List<Format> = listOf()
    private var blocked = false
    private val handler = Handler()
    private var lastValidText = ""

    val mainTextWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            p0?.let {
                if (it.isBlank()) etPhone.setText("+")
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            if (!blocked)
                p0?.let {
                    val text = it.toString()
                    Log.d("MaskTag", "text = $text")
                    if (text.isBlank()) {
                        removeFormat()
                    }
                    formats.forEach {
                        if (text == it.code) {
                            Log.d("MaskTag", "text == it.code")
                            if (it != lastFormat) {
                                setFormat(it)
                            } else removeFormat()

                        } else if (p0.contains(it.code) && lastFormat == null && p0.last() != ' ') {
                            removeAllSpace()
                            setFormat(it, p0.last())
                        }
                    }
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val formatsJson = assets.open("formats.json").bufferedReader().readText()
        formats = Gson().fromJson(formatsJson, object : TypeToken<List<Format>>() {}.type)

        etPhone.apply {
            setText("+")
            setSelection(1)
            addTextChangedListener(mainTextWatcher)
        }
    }

    private fun removeFormat() {
        lastFormat = null
        Handler().postDelayed({
            Log.d("MaskTag", "removeFormatWatcher")
            blocked = true
            etPhone.removeTextChangedListener(formatWatcher)
            formatWatcher = null
            etPhone.setText(etPhone.text.trim())
            etPhone.setSelection(etPhone.text.length)
            unblock(0)
        }, 0)
    }

    private fun setFormat(format: Format, append: Char = ' ') {
        Log.d("MaskTag", "setForma, append = $append")
        lastFormat = format
        val slots = UnderscoreDigitSlotsParser().parseSlots(format.format);
        formatWatcher = MaskFormatWatcher(MaskImpl.createTerminated(slots));
        blocked = true
        formatWatcher?.installOn(etPhone)
        if (append != ' ')
            etPhone.text.append(append)
        unblock(0)
        handler.postDelayed({ etPhone.setSelection(etPhone.text.length) }, 0)
    }

    private fun removeAllSpace() {
        blocked = true
        etPhone.setText(etPhone.text.toString().replace(" ", ""))
        unblock()
    }

    private fun unblock(delay: Long = 0L) {
        Log.d("MaskTag", "setMainTextWatcher")
        if (delay == 0L) blocked = false
        else handler.postDelayed({ blocked = false }, delay)
    }
}
