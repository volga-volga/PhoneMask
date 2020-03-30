package ru.vvdev.phonemask

import android.text.Editable
import ru.tinkoff.decoro.watchers.FormatWatcher

abstract class SafeFormatWatcher: FormatWatcher(){
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, insertedCount: Int) {
        try{
            super.onTextChanged(s, start, before, insertedCount)
        }catch (e: Exception){

        }
    }

    override fun afterTextChanged(newText: Editable?) {
        try{
            super.afterTextChanged(newText)
        }catch (e: Exception){

        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        try{
            super.beforeTextChanged(s, start, count, after)
        }catch (e: Exception){

        }
    }
}