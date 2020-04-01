package ru.vvdev.phonemask

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.app.ActivityCompat

fun Context.getDrawableForFormat(format: Format): Drawable? {
    return try {
        val name = format.name.toLowerCase().replace(" ", "_") + format.code.replace("+", "")
        val resID = this.resources.getIdentifier(name, "drawable", this.packageName)
        ActivityCompat.getDrawable(this, resID)
    } catch (e: Exception) {
        null
    }
}