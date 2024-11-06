package com.example.foodapp.helper


import android.content.Context
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.*
import com.example.foodapp.model.Notification
import com.example.foodapp.model.User
import java.text.SimpleDateFormat
import java.util.*

class FirebaseNotificationHelper(private val mContext: Context) {

    private val mDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val mReference: DatabaseReference = mDatabase.reference
    private val notificationList: MutableList<Notification> = ArrayList()
    private val notificationListToNotify: MutableList<Notification> = ArrayList()

    interface DataStatus {
        fun DataIsLoaded(notificationList: List<Notification>, notificationListToNotify: List<Notification>)
        fun DataIsInserted()
        fun DataIsUpdated()
        fun DataIsDeleted()
    }

    fun readNotification(userId: String, dataStatus: DataStatus?) {
        mReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notificationList.clear()
                notificationListToNotify.clear()
                val snapchild = snapshot.child("Notification").child(userId)
                for (snap in snapchild.children) {
                    val notification = snap.getValue(Notification::class.java)
                    notification?.let {
                        notificationList.add(it)
                    }
                }

                // Sort notifications by date
                notificationList.sortWith { o1, o2 ->
                    if (o1.time == null || o2.time == null) 0
                    else o2.time!!.compareTo(o1.time!!)
                }

                for (i in notificationList.indices) {
                    if (!notificationList[i].isNotified) {
                        notificationListToNotify.add(notificationList[i])
                        notificationList[i].notificationId?.let {
                            mReference.child("Notification").child(userId)
                                .child(it)
                                .child("notified")
                                .setValue(true)
                                .addOnSuccessListener {
                                    // Handle success if needed
                                }
                        }
                    }
                }

                dataStatus?.DataIsLoaded(notificationList, notificationListToNotify)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun addNotification(userId: String, notification: Notification, dataStatus: DataStatus?) {
        val key = mReference.child("Notification").child(userId).push().key
        notification.notificationId = key
        mReference.child("Notification").child(userId).child(key!!).setValue(notification)
            .addOnSuccessListener {
                dataStatus?.DataIsInserted()
            }
    }

    fun updateNotification(userId: String, notification: Notification, dataStatus: DataStatus?) {
        notification.notificationId?.let {
            mReference.child("Notification").child(userId).child(it)
                .setValue(notification)
                .addOnSuccessListener {
                    dataStatus?.DataIsUpdated()
                }
        }
    }

    companion object {
        fun createNotification(
            title: String,
            content: String,
            imageNotificationURL: String,
            productId: String,
            billId: String,
            confirmId: String,
            publisher: User?
        ): Notification {
            val notification = Notification()
            notification.isRead = false
            notification.isNotified = false
            notification.content = content
            notification.imageURL = imageNotificationURL
            notification.title = title
            notification.productId = productId
            notification.billId = billId
            notification.confirmId = confirmId
            notification.setPublisher(publisher)
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
            val currentDateAndTime = sdf.format(Date())
            notification.time = currentDateAndTime
            return notification
        }
    }
}