package com.example.foodapp.Interface


import com.example.foodapp.model.Product
import com.example.foodapp.model.UserDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface APIService {
    @POST("api/auth/signup")
    fun signUp(@Body user: UserDTO): Call<String>

    @GET("/api/products")
    fun getAllProducts(): Call<List<Product>>

    @GET("/api/products/filter")
    fun getProductsByType(@Query("type") type: String): Call<List<Product>>

    @GET("/api/products/search")
    fun searchProduct(@Query("keyword") keyword: String): Call<List<Product>>

    @GET("/api/products/{productId}")
    fun getProductInfor(@Path("productId") productId: String): Call<Product>

//    @GET("/api/user/{userId}")
//    fun getUserByUserId(@Path("userId") userId: String): Call<User>

//    @PUT("/api/user/update")
//    fun updateUser(@Body user: User): Call<User>

    @POST("/api/user/product/add")
    fun addProduct(@Body product: Product): Call<Product>

    @PUT("/api/user/product/edit")
    fun updateProduct(@Body product: Product): Call<Product>

    @GET("/api/user/products")
    fun getProductsPublisherId(@Query("publisherId") publisherId: String): Call<List<Product>>

    @PUT("/api/user/feedback")
    fun addComment(
        @Query("ratingAmount") ratingAmount: Int,
        @Query("ratingStar") ratingStar: Double,
        @Query("productId") productId: String
    ): Call<Product>

//    @GET("/api/cart/productCart")
//    fun getProductCart(@Query("idProduct") idProduct: String): Call<CartProduct>

    @PUT("/api/admin/product/check")
    fun checkProduct(
        @Query("userId") userId: String,
        @Query("productId") productId: String
    ): Call<Product>
}