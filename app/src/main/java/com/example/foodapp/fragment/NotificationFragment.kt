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

class NotificationFragment(private val userId: String) : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

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
                val adapter = NotificationListAdapter(requireContext(), notificationList, userId)
                binding.recNotification.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(requireContext())
                    this.adapter = adapter
                }
                binding.progressBarNotification.visibility = View.GONE
            }

            override fun DataIsInserted() {
                // Handle data insertion if needed
            }

            override fun DataIsUpdated() {
                // Handle data update if needed
            }

            override fun DataIsDeleted() {
                // Handle data deletion if needed
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
