package ru.vvdev.phonemask

import ru.tinkoff.decoro.MaskImpl

class SafeMaskFormatWatcher(var maskOriginal: MaskImpl) : SafeFormatWatcher() {

    init {
        setMask(maskOriginal)
    }

    override fun createMask() = MaskImpl(maskOriginal)

    fun setMask(maskOriginal: MaskImpl) {
        this.maskOriginal = maskOriginal
        refreshMask()
    }

    fun swapMask(newMask: MaskImpl) {
        maskOriginal = MaskImpl(newMask)
        maskOriginal.clear()
        refreshMask(newMask.toString())
    }
}