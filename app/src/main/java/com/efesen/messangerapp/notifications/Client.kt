package com.efesen.messangerapp.notifications

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Efe Şen on 20.09.2023.
 */
class Client {
    object Client {
        private var retrofit: Retrofit? = null

        fun getClient(url: String?): Retrofit? {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit
        }
    }

}