package com.example.foodapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//object RetrofitClient {
//    private var retrofit: Retrofit? = null
//    fun getRetrofit(): Retrofit {
//        if (retrofit == null) {
//            retrofit = Retrofit.Builder()
//                .baseUrl("https://lucifer-so-sad-c2bad4c0e92d.herokuapp.com/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//        }
//        return retrofit!!
//    }
//}


object RetrofitClient {
    var retrofit: Retrofit? = null
        get() {
            if (field == null) {
                field = Retrofit.Builder()
                    .baseUrl("https://lucifer-so-sad-c2bad4c0e92d.herokuapp.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return field
        }
        private set
}