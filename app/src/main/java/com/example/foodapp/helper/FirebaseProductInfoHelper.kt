package com.example.foodapp.helper

import com.google.firebase.database.*
import com.example.foodapp.model.Comment
import com.example.foodapp.model.Product

class FirebaseProductInfoHelper(private val productId: String) {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val reference: DatabaseReference = database.reference
    private val comments = mutableListOf<Comment>()

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
        reference.child("Comments").child(productId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                comments.clear()
                val keys = mutableListOf<String>()
                for (keyNode in snapshot.children) {
                    val comment = keyNode.getValue(Comment::class.java)
                    comment?.let { comments.add(it) }
                    keys.add(keyNode.key ?: "")
                }
                val rate = comments.size
                dataStatus?.DataIsLoaded(comments, rate, keys)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })
    }

    fun countFavourite(dataStatusCountFavourite: DataStatusCountFavourite?) {
        reference.child("Favorites").addValueEventListener(object : ValueEventListener {
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
                // Handle error if needed
            }
        })
    }

    fun readInformationById(dataStatusInformationOfProduct: DataStatusInformationOfProduct?) {
        reference.child("Products").child(productId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val item = snapshot.getValue(Product::class.java)
                dataStatusInformationOfProduct?.DataIsLoaded(item)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })
    }
}
