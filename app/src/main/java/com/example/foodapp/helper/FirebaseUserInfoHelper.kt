package com.example.foodapp.helper

import android.content.Context
import com.example.foodapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseUserInfoHelper(private val context: Context) {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val reference: DatabaseReference = database.reference

    interface DataStatus {
        fun dataIsLoaded(user: User?)
        fun dataIsInserted()
        fun dataIsUpdated()
        fun dataIsDeleted()
    }

    fun readUserInfo(userId: String, dataStatus: DataStatus?) {
        reference.child("Users").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                dataStatus?.dataIsLoaded(user)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error if needed
            }
        })
    }
}
