package com.example.foodapp.helper

import com.google.firebase.database.*

class FirebaseFavouriteInfoProductHelper {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val referenceFavourite: DatabaseReference = database.getReference("Favorites")

    interface DataStatus {
        fun DataIsLoaded(isFavouriteExists: Boolean, isFavouriteDetailExists: Boolean)
        fun DataIsInserted()
        fun DataIsUpdated()
        fun DataIsDeleted()
    }

    fun readFavourite(productId: String, userId: String, dataStatus: DataStatus?) {
        referenceFavourite.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isFavouriteExists = snapshot.child(userId).exists()
                val isFavouriteDetailExists = isFavouriteExists && snapshot.child(userId).child(productId).exists()

                dataStatus?.DataIsLoaded(isFavouriteExists, isFavouriteDetailExists)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })
    }

    fun addFavourite(userId: String, productId: String, dataStatus: DataStatus?) {
        referenceFavourite.child(userId).child(productId).setValue(true).addOnSuccessListener {
            dataStatus?.DataIsInserted()
        }
    }

    fun removeFavourite(userId: String, productId: String, dataStatus: DataStatus?) {
        referenceFavourite.child(userId).child(productId).removeValue().addOnSuccessListener {
            dataStatus?.DataIsDeleted()
        }
    }
}
