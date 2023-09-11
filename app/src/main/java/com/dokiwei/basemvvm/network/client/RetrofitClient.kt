package com.dokiwei.basemvvm.network.client

import android.util.Log
import com.dokiwei.basemvvm.network.api.HomeApi
import com.dokiwei.basemvvm.util.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * @author DokiWei
 * @date 2023/9/11 19:42
 */
object RetrofitClient {
    //    private val cookieJar = Cookie
    private val client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS).retryOnConnectionFailure(true)
        .addInterceptor(LoggingInterceptor())
//        .cookieJar(cookieJar)
        .build()

    private val retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL).client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()


    val homeApi: HomeApi = retrofit.create(HomeApi::class.java)
}

class LoggingInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response: Response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            e.printStackTrace()
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(500)
                .message("网络错误").body("网络错误".toResponseBody(null)).build()
        }
        Log.d(
            "网络日志", """
        host:${request.url}
        客户端请求:head-${request.headers}  body-${request.bodyString()}
        服务器回应:code-${response.code}
        耗时:${response.elapsedTime()}ms
    """.trimIndent()
        )
        return response
    }

    // 扩展函数，获取请求的字符串
    private fun Request.bodyString(): String {
        val requestBody = this.body
        if (requestBody != null) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val charset = requestBody.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
            return buffer.readString(charset)
        }
        return ""
    }

    // 扩展函数，获取响应的字符串
    private fun Response.bodyString(): String {
        val responseBody = this.body
        val source = responseBody.source()
        source.request(Long.MAX_VALUE)
        val buffer = source.buffer
        val charset = responseBody.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
        return buffer.clone().readString(charset)
    }

    // 扩展函数，获取响应的耗时
    private fun Response.elapsedTime(): Long {
        val sentRequestAtMillis = this.sentRequestAtMillis
        val receivedResponseAtMillis = this.receivedResponseAtMillis
        return receivedResponseAtMillis - sentRequestAtMillis
    }

}