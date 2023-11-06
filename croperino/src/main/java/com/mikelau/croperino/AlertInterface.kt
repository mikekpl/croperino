package com.mikelau.croperino

import android.content.DialogInterface

interface WithNeutral {
    fun PositiveMethod(dialog: DialogInterface?, id: Int)
    fun NeutralMethod(dialog: DialogInterface?, id: Int)
    fun NegativeMethod(dialog: DialogInterface?, id: Int)
}

