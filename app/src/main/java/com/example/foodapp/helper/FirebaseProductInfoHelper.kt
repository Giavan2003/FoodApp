package com.example.foodapp.helper

import com.example.foodapp.model.Comment
import com.example.foodapp.model.Product
import com.google.firebase.database.*


class FirebaseProductInfoHelper(private val productId: String) {

    private val mDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val mReference: DatabaseReference = mDatabase.reference
    private val comments: MutableList<Comment> = mutableListOf()

    interface DataStatus {
        fun DataIsLoaded(comments: List<Comment>, countRate: Int, keys: List<String>)
        fun DataIsInserted()
        fun DataIsUpdated()
        fun DataIsDeleted()
    }

    interface DataStatusCountFavourite {
        fun DataIsLoaded(countFavourite: Int)
        fun DataIsInserted()
        fun DataIsUpdated()
        fun DataIsDeleted()
    }

    interface DataStatusInformationOfProduct {
        fun DataIsLoaded(product: Product?)
        fun DataIsInserted()
        fun DataIsUpdated()
        fun DataIsDeleted()
    }

    fun readComments(dataStatus: DataStatus?) {
        mReference.child("Comments").child(productId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                comments.clear()
                val keys: MutableList<String> = mutableListOf()
                for (keyNode in snapshot.children) {
                    keyNode.getValue(Comment::class.java)?.let { comments.add(it) }
                    keyNode.key?.let { keys.add(it) }
                }
                val rate = comments.size
                dataStatus?.DataIsLoaded(comments, rate, keys)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error if needed
            }
        })
    }

    fun countFavourite(dataStatusCountFavourite: DataStatusCountFavourite?) {
        mReference.child("Favorites").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                for (keyNode in snapshot.children) {
                    if (keyNode.child(productId).exists()) {
                        count++
                    }
                }
                dataStatusCountFavourite?.DataIsLoaded(count)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error if needed
            }
        })
    }

    fun readInformationById(dataStatusInformationOfProduct: DataStatusInformationOfProduct?) {
        mReference.child("Products").child(productId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val item = snapshot.getValue(Product::class.java)
                dataStatusInformationOfProduct?.DataIsLoaded(item)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error if needed
            }
        })
    }
}
