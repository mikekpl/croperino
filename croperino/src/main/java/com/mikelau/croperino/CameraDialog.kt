package com.mikelau.croperino

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

object CameraDialog {
    private var sBuilder: AlertDialog.Builder? = null

    fun getConfirmDialog(
        context: Context, title: String, message: String,
        positive: String, neutral: String, negative: String, color: Int,
        isCancelable: Boolean, target: WithNeutral
    ) {
        val i = LayoutInflater.from(context)
        val v = i.inflate(R.layout.dialog_camera, null)
        sBuilder = AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
        sBuilder?.setView(v)?.setCancelable(false)
            ?.setPositiveButton(positive) { dialog, id -> target.PositiveMethod(dialog, id) }
            ?.setNeutralButton(neutral) { dialog, id -> target.NeutralMethod(dialog, id) }
            ?.setNegativeButton(negative) { dialog, id -> target.NegativeMethod(dialog, id) }
        val tvTitle = v.findViewById<TextView>(R.id.tv_dialog_title)
        val tvMessage = v.findViewById<TextView>(R.id.tv_dialog_message)
        tvTitle.setBackgroundColor(color)
        tvTitle.text = title
        tvMessage.text = message
        val alert = sBuilder?.create()
        alert?.setCancelable(isCancelable)
        alert?.setCanceledOnTouchOutside(true)
        alert?.show()
        if (isCancelable) {
            alert?.setOnCancelListener { target.NegativeMethod(alert, 0) }
        }
    }
}
