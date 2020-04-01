package ru.vvdev.phonemask.ui

import ru.vvdev.phonemask.model.Format

interface PhoneListener {
    fun formatChanged(format: Format)
}