package com.example.foodapp.adapter.Home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.foodapp.fragment.Home.LoginFragment
import com.uteating.foodapp.fragment.Home.SignUpFragment


class LoginSignUpAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            LoginFragment()
        } else {
            SignUpFragment()
        }
    }

    override fun getItemCount(): Int = 2
}
