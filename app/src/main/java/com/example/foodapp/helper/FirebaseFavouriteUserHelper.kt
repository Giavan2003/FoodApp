package com.example.foodapp.helper

import com.example.foodapp.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseFavouriteUserHelper {
    private val mDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val mReferenceFavourite: DatabaseReference = mDatabase.reference
    private lateinit var keyProducts: ArrayList<String>
    private lateinit var favouriteList: ArrayList<Product>

    interface DataStatus {
        fun DataIsLoaded(favouriteProducts: ArrayList<Product>, keys: ArrayList<String>)
        fun DataIsInserted()
        fun DataIsUpdated()
        fun DataIsDeleted()
    }

    fun readFavouriteList(userId: String, dataStatus: DataStatus?) {
        mReferenceFavourite.child("Favorites").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    keyProducts = ArrayList()
                    favouriteList = ArrayList()
                    for (keyNode in snapshot.children) {
                        keyProducts.add(keyNode.key ?: "")
                    }
                    readProductInfo(keyProducts, dataStatus)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle cancellation
                }
            })
    }

    private fun readProductInfo(keys: ArrayList<String>, dataStatus: DataStatus?) {
        mReferenceFavourite.child("Products").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (key in keys) {
                    val product = snapshot.child(key).getValue(Product::class.java)
                    if (product != null) {
                        favouriteList.add(product)
                    }
                }

                dataStatus?.DataIsLoaded(favouriteList, keyProducts)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle cancellation
            }
        })
    }
}
