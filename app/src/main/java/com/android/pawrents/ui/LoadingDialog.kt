package com.android.pawrents.ui

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.android.pawrents.R

class LoadingDialog(private val context: Context) {
    private var dialog: AlertDialog? = null

    fun startLoadingDialog() {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView: View = inflater.inflate(R.layout.custom_loading_dialog, null)

        builder.setView(dialogView)
        builder.setCancelable(false)

        dialog = builder.create()
        dialog?.show()
    }

    fun dismissDialog() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }
}