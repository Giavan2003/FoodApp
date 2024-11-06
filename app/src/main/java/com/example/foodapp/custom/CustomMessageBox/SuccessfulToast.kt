package com.example.foodapp.custom.CustomMessageBox

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import com.example.foodapp.databinding.LayoutSuccessfulToastBinding

class SuccessfulToast(mContext: Context?, content: String?) {
    private val binding: LayoutSuccessfulToastBinding = LayoutSuccessfulToastBinding.inflate(LayoutInflater.from(mContext))
    private val toast: Toast = Toast(mContext).apply {
        view = binding.root
        setGravity(Gravity.BOTTOM, 0, 10)
        duration = Toast.LENGTH_LONG
    }

    init {
        binding.layoutSuccessfulToast.translationX = -2000F
        binding.backgroundToast.translationX = -2000F
        binding.txtContentMessage.text = content
    }

    fun showToast() {
        toast.show()
        binding.layoutSuccessfulToast.animate().translationX(0F).setDuration(1000).setStartDelay(0)
        binding.backgroundToast.animate().translationX(0F).setDuration(800).setStartDelay(2500)
    }
}
