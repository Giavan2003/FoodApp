package com.example.foodapp.helper

import com.google.firebase.database.*
import com.example.foodapp.model.Cart
import com.example.foodapp.model.CartInfo

class FirebaseArtToCartHelper(
    private val userId: String = "",
    private val productId: String = ""
) {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val referenceCart: DatabaseReference = database.reference

    interface DataStatus {
        fun DataIsLoaded(cart: Cart, cartInfo: CartInfo, isExistsCart: Boolean, isExistsProduct: Boolean)
        fun DataIsInserted()
        fun DataIsUpdated()
        fun DataIsDeleted()
    }

    fun readCarts(dataStatus: DataStatus?) {
        referenceCart.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isExistsCart = false
                var isExistsProduct = false
                var cart = Cart()
                var cartInfo = CartInfo()

                for (keyNode in snapshot.child("Carts").children) {
                    val userIdFromDb = keyNode.child("userId").getValue(String::class.java)
                    if (userIdFromDb == userId) {
                        isExistsCart = true
                        cart = keyNode.getValue(Cart::class.java) ?: Cart()
                        break
                    }
                }

                if (isExistsCart) {
                    cart.cartId?.let { cartId ->
                        for (keyNode in snapshot.child("CartInfo's").child(cartId).children) {
                            val productIdFromDb =
                                keyNode.child("productId").getValue(String::class.java)
                            if (productIdFromDb == productId) {
                                isExistsProduct = true
                                cartInfo = keyNode.getValue(CartInfo::class.java) ?: CartInfo()
                                break
                            }
                        }
                    }
                }

                dataStatus?.DataIsLoaded(cart, cartInfo, isExistsCart, isExistsProduct)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })
    }

    fun addCarts(cart: Cart, cartInfo: CartInfo, dataStatus: DataStatus?) {
        val key = referenceCart.child("Carts").push().key ?: return
        cart.cartId = key
        referenceCart.child("Carts").child(key).setValue(cart).addOnSuccessListener {
            dataStatus?.DataIsInserted()
        }

        val keyInfo = referenceCart.child("CartInfo's").child(key).push().key ?: return
        cartInfo.cartInfoId = keyInfo
        referenceCart.child("CartInfo's").child(key).child(keyInfo).setValue(cartInfo).addOnSuccessListener {
            dataStatus?.DataIsInserted()
        }
    }

    fun updateCart(cart: Cart, cartInfo: CartInfo, isExistsProduct: Boolean, dataStatus: DataStatus?) {
        cart.cartId?.let { cartId ->
            referenceCart.child("Carts").child(cartId).setValue(cart).addOnSuccessListener {
                dataStatus?.DataIsUpdated()
            }

            if (isExistsProduct) {
                cartInfo.cartInfoId?.let { cartInfoId ->
                    referenceCart.child("CartInfo's").child(cartId).child(cartInfoId)
                        .setValue(cartInfo)
                        .addOnSuccessListener {
                            dataStatus?.DataIsUpdated()
                        }
                }
            } else {
                val key = referenceCart.child("CartInfo's").child(cartId).push().key ?: return
                cartInfo.cartInfoId = key
                referenceCart.child("CartInfo's").child(cartId).child(key).setValue(cartInfo)
                    .addOnSuccessListener {
                        dataStatus?.DataIsInserted()
                    }
            }
        }
    }
}
