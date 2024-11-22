package com.example.foodapp.custom.CustomMessageBox

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import com.example.foodapp.databinding.LayoutFailToastBinding
import com.example.foodapp.databinding.LayoutSuccessfulToastBinding

class FailToast(mContext: Context?, content: String?) {
    private val binding: LayoutFailToastBinding = LayoutFailToastBinding.inflate(LayoutInflater.from(mContext))
    private val toast: Toast = Toast(mContext).apply {
        view = binding.root
        setGravity(Gravity.BOTTOM, 0, 10)
        duration = Toast.LENGTH_LONG
    }

    init {
        binding.layoutFailToast.translationX = -2000F
        binding.backgroundFailToast.translationX = -2000F
        binding.txtContentMessage.text = content
    }
    fun showToast() {
        toast.show()
        binding.layoutFailToast.animate().translationX(0F).setDuration(1000).setStartDelay(0)
        binding.backgroundFailToast.animate().translationX(0F).setDuration(800).setStartDelay(2500)
    }
}