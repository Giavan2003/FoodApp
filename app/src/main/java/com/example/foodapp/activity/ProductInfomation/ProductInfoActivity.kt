package com.example.foodapp.activity.ProductInformation

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.activity.Home.ChatDetailActivity
import com.example.foodapp.activity.MyShop.AddFoodActivity
import com.example.foodapp.adapter.CommentRecyclerViewAdapter
import com.example.foodapp.adapter.ProductInfoImageAdapter
import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ActivityProductInfoBinding
import com.example.foodapp.helper.*
import com.example.foodapp.model.*
import java.util.*

class ProductInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductInfoBinding
    private lateinit var productId: String
    private lateinit var productName: String
    private var productPrice = 0
    private lateinit var productDescription: String
    private var ratingStar: Double = 0.0
    private lateinit var productImage1: String
    private lateinit var productImage2: String
    private lateinit var productImage3: String
    private lateinit var productImage4: String
    private lateinit var userName: String
    private lateinit var userId: String
    private lateinit var publisherId: String
    private var sold = 0
    private lateinit var productType: String
    private var remainAmount = 0
    private var ratingAmount = 0
    private lateinit var state: String
    private var own = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("result", "OK")
        if (requestCode == 10 && resultCode == 10) {
            data?.let {
                productId = it.getStringExtra("productId") ?: ""
                productName = it.getStringExtra("productName") ?: ""
                productPrice = it.getIntExtra("productPrice", 0)
                productImage1 = it.getStringExtra("productImage1") ?: ""
                productImage2 = it.getStringExtra("productImage2") ?: ""
                productImage3 = it.getStringExtra("productImage3") ?: ""
                productImage4 = it.getStringExtra("productImage4") ?: ""
                ratingStar = it.getDoubleExtra("ratingStar", 0.0)
                userName = it.getStringExtra("userName") ?: ""
                productDescription = it.getStringExtra("productDescription") ?: ""
                publisherId = it.getStringExtra("publisherId") ?: ""
                userId = it.getStringExtra("userId") ?: ""
                sold = it.getIntExtra("sold", 0)
                productType = it.getStringExtra("productType") ?: ""
                remainAmount = it.getIntExtra("remainAmount", 0)
                ratingAmount = it.getIntExtra("ratingAmount", 0)
                state = it.getStringExtra("state") ?: ""

                setupDefaultValues()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")

        intent?.let {
            productId = it.getStringExtra("productId") ?: ""
            productName = it.getStringExtra("productName") ?: ""
            productPrice = it.getIntExtra("productPrice", 0)
            productImage1 = it.getStringExtra("productImage1") ?: ""
            productImage2 = it.getStringExtra("productImage2") ?: ""
            productImage3 = it.getStringExtra("productImage3") ?: ""
            productImage4 = it.getStringExtra("productImage4") ?: ""
            ratingStar = it.getDoubleExtra("ratingStar", 0.0)
            userName = it.getStringExtra("userName") ?: ""
            productDescription = it.getStringExtra("productDescription") ?: ""
            publisherId = it.getStringExtra("publisherId") ?: ""
            userId = it.getStringExtra("userId") ?: ""
            sold = it.getIntExtra("sold", 0)
            productType = it.getStringExtra("productType") ?: ""
            remainAmount = it.getIntExtra("remainAmount", 0)
            ratingAmount = it.getIntExtra("ratingAmount", 0)
            state = it.getStringExtra("state") ?: ""

            setupDefaultValues()
        }

        setupImageSlider()
        loadFavouriteData()
        setupCommentRecyclerView()
        setupCartButtons()
    }

    private fun setupDefaultValues() {
        binding.txtNameProduct.text = productName
        binding.txtPriceProduct.text = CurrencyFormatter.format(productPrice.toDouble())
        binding.txtDesciption.text = productDescription
        binding.txtSell.text = sold.toString()
        binding.ratingBar.rating = ratingStar.toFloat()
        binding.txtRemainAmount.text = remainAmount.toString()

        if (publisherId == userId) {
            own = true
            binding.btnAddToCart.visibility = View.INVISIBLE
            binding.btnCancelFavourite.visibility = View.INVISIBLE
            binding.btnAddFavourite.visibility = View.INVISIBLE
            binding.btnChat.visibility = View.INVISIBLE
        } else {
            own = false
            binding.btnEditProduct.visibility = View.INVISIBLE
        }
    }


    private fun setupImageSlider() {
        val dsImage = arrayListOf<String>().apply {
            if (productImage1.isNotEmpty()) add(productImage1)
            if (productImage2.isNotEmpty()) add(productImage2)
            if (productImage3.isNotEmpty()) add(productImage3)
            if (productImage4.isNotEmpty()) add(productImage4)
        }
        val imageAdapter = ProductInfoImageAdapter(this, dsImage)
        binding.pagerProductImage.adapter = imageAdapter
        binding.tabDots.attachTo(binding.pagerProductImage)
    }

    private fun setCommentRecView() {
        FirebaseProductInfoHelper(productId).readComments(object : FirebaseProductInfoHelper.DataStatus {
            override fun DataIsLoaded(commentList: List<Comment>, count: Int, keys: List<String>) {
                val adapter = CommentRecyclerViewAdapter(this@ProductInfoActivity, commentList, keys)
                binding.recComment.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this@ProductInfoActivity)
                    adapter = adapter
                }
                binding.txtRate.text = "($count)"
            }

            override fun DataIsInserted() {}
            override fun DataIsUpdated() {}
            override fun DataIsDeleted() {}
        })
    }



    private fun setupCartButtons() {
        val isCartExists = booleanArrayOf(false)
        val isProductExists = booleanArrayOf(false)
        val currentCart = arrayOf(Cart())
        val currentCartInfo = arrayOf(CartInfo())

        FirebaseArtToCartHelper(userId, productId).readCarts(object : FirebaseArtToCartHelper.DataStatus {
            override fun DataIsLoaded(cart: Cart, cartInfo: CartInfo, isExistsCart: Boolean, isExistsProduct: Boolean) {
                isCartExists[0] = isExistsCart
                isProductExists[0] = isExistsProduct
                currentCart[0] = cart
                currentCartInfo[0] = cartInfo
            }

            override fun DataIsInserted() {}
            override fun DataIsUpdated() {}
            override fun DataIsDeleted() {}
        })

        binding.btnAddToCart.setOnClickListener {
            updateCart(isCartExists[0], isProductExists[0], currentCart[0], currentCartInfo[0], 1)
        }
    }

    private fun loadFavouriteData() {
        FirebaseFavouriteInfoProductHelper().readFavourite(productId, userId, object : FirebaseFavouriteInfoProductHelper.DataStatus {
            override fun DataIsLoaded(isFavouriteExists: Boolean, isFavouriteDetailExists: Boolean) {
                if (!own) {
                    if (isFavouriteDetailExists) {
                        binding.btnAddFavourite.visibility = View.GONE
                        binding.btnCancelFavourite.visibility = View.VISIBLE
                    } else {
                        binding.btnAddFavourite.visibility = View.VISIBLE
                        binding.btnCancelFavourite.visibility = View.GONE
                    }
                }
                binding.progressBarProductInfo.visibility = View.GONE
            }

            override fun DataIsInserted() {}
            override fun DataIsUpdated() {}
            override fun DataIsDeleted() {}
        })

        binding.btnAddFavourite.setOnClickListener {
            FirebaseFavouriteInfoProductHelper().addFavourite(userId, productId, object : FirebaseFavouriteInfoProductHelper.DataStatus {
                override fun DataIsInserted() {
                    SuccessfulToast(this@ProductInfoActivity, "Added to your favourite list").showToast()
                    pushNotificationFavourite()
                }

                override fun DataIsLoaded(isFavouriteExists: Boolean, isFavouriteDetailExists: Boolean) {}
                override fun DataIsUpdated() {}
                override fun DataIsDeleted() {}
            })
        }

        binding.btnCancelFavourite.setOnClickListener {
            FirebaseFavouriteInfoProductHelper().removeFavourite(userId, productId, object : FirebaseFavouriteInfoProductHelper.DataStatus {
                override fun DataIsDeleted() {
                    SuccessfulToast(this@ProductInfoActivity, "Removed from your favourite list").showToast()
                }

                override fun DataIsLoaded(isFavouriteExists: Boolean, isFavouriteDetailExists: Boolean) {}
                override fun DataIsInserted() {}
                override fun DataIsUpdated() {}
            })
        }
    }

    fun updateCart(isCartExists: Boolean, isProductExists: Boolean, currentCart: Cart, currentCartInfo: CartInfo, amount: Int) {
        // trường hợp user mới tạo chưa có giỏ hàng
        if (!isCartExists) {
            val cart = Cart().apply {
                totalPrice = productPrice * amount
                totalAmount = amount
                userId = userId
            }
            val cartInfo = CartInfo().apply {
                this.amount = amount
                productId = productId
            }
            FirebaseArtToCartHelper().addCarts(cart, cartInfo, object : FirebaseArtToCartHelper.DataStatus {
                override fun DataIsLoaded(cart: Cart, cartInfo: CartInfo, isExistsCart: Boolean, isExistsProduct: Boolean) {}

                override fun DataIsInserted() {
                    SuccessfulToast(this@ProductInfoActivity, "Added to your favourite list").showToast()
                }

                override fun DataIsUpdated() {}

                override fun DataIsDeleted() {}
            })
        } else {
            // trường hợp chưa có sản phẩm hiện tại trong giỏ hàng
            if (!isProductExists) {
                if (amount <= remainAmount) {
                    val cartInfo = CartInfo().apply {
                        productId = this@ProductInfoActivity.productId
                        this.amount = amount
                    }
                    currentCart.apply {
                        totalAmount += amount
                        totalPrice += amount * productPrice
                    }
                    FirebaseArtToCartHelper().updateCart(currentCart, cartInfo, false, object : FirebaseArtToCartHelper.DataStatus {
                        override fun DataIsLoaded(cart: Cart, cartInfo: CartInfo, isExistsCart: Boolean, isExistsProduct: Boolean) {}

                        override fun DataIsInserted() {
                            SuccessfulToast(this@ProductInfoActivity, "Added to your cart").showToast()
                        }

                        override fun DataIsUpdated() {}

                        override fun DataIsDeleted() {}
                    })
                }
            } else {  // trường hợp đã có sản phẩm hiện tại trong giỏ hàng
                FailToast(this@ProductInfoActivity, "This product has already been in the cart!").showToast()
            }
        }
    }


    private fun pushNotificationFavourite() {
        val title = "Favourite product"
        val content = "$userName liked your product: $productName. Go to Product Information to check it."
        val notification = FirebaseNotificationHelper.createNotification(
            title, content, productImage1, productId, "None", "None", null
        )
        FirebaseNotificationHelper(this).addNotification(publisherId, notification, object : FirebaseNotificationHelper.DataStatus {
            override fun DataIsInserted() {}
            override fun DataIsLoaded(notificationList: List<Notification>, notificationListToNotify: List<Notification>) {}
            override fun DataIsUpdated() {}
            override fun DataIsDeleted() {}
        })
    }
}