package com.example.foodapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.adapter.NotificationListAdapter
import com.example.foodapp.databinding.FragmentNotificationBinding
import com.example.foodapp.helper.FirebaseNotificationHelper
import com.example.foodapp.model.Notification
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationFragment(private val userId: String) : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private var notificationListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)

        readNotification()

        return binding.root
    }

    private fun readNotification() {
        FirebaseNotificationHelper(requireContext()).readNotification(userId, object : FirebaseNotificationHelper.DataStatus {
            override fun DataIsLoaded(
                notificationList: List<Notification>,
                notificationListToNotify: List<Notification>
            ) {
                if (!isAdded) return // Đảm bảo Fragment còn hoạt động

                binding.let {
                    val adapter = NotificationListAdapter(requireContext(), notificationList, userId)
                    it.recNotification.apply {
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(requireContext())
                        this.adapter = adapter
                    }
                    it.progressBarNotification.visibility = View.GONE
                }
            }

            override fun DataIsInserted() {}
            override fun DataIsUpdated() {}
            override fun DataIsDeleted() {}
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        notificationListener?.let {
            FirebaseDatabase.getInstance().reference.removeEventListener(it)
        }
        _binding = null
    }
}
