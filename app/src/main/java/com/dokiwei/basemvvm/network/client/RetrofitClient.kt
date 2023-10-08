package com.dokiwei.basemvvm.network.client

import com.dokiwei.basemvvm.network.api.HomeApi
import com.dokiwei.basemvvm.ui.app.MyApplication
import com.dokiwei.basemvvm.util.Constants
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @author DokiWei
 * @date 2023/9/11 19:42
 */
object RetrofitClient {
    private val cookieJar =
        PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(MyApplication.context))
    private val client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS).retryOnConnectionFailure(true)
        .addInterceptor(LoggingInterceptor())
        .cookieJar(cookieJar)
        .build()

    private val retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL).client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()


    val homeApi: HomeApi = retrofit.create(HomeApi::class.java)
}