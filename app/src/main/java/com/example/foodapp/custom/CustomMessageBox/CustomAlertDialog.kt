package com.example.foodapp.custom.CustomMessageBox

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.example.foodapp.R
import com.example.foodapp.databinding.LayoutAlertDialogBinding


class CustomAlertDialog(mContext: Context, content: String) {

    companion object {
        lateinit var alertDialog: AlertDialog
        lateinit var binding: LayoutAlertDialogBinding

        fun showAlertDialog() {
            alertDialog.show()
        }
    }

    init {
        val builder = AlertDialog.Builder(mContext, R.style.AlertDialogTheme)
        binding = LayoutAlertDialogBinding.inflate(LayoutInflater.from(mContext))
        builder.setView(binding.root)

        binding.txtContentMessage.text = content
        alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(0))
    }
}