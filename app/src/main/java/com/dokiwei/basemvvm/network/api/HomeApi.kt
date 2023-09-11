package com.dokiwei.basemvvm.network.api

import com.dokiwei.basemvvm.model.data.Article
import com.dokiwei.basemvvm.util.Constants
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * @author DokiWei
 * @date 2023/9/11 19:01
 */
interface HomeApi {
    /**
     * 首页文章
     */
    @GET("article/list/{page}/json")
    suspend fun homeArticle(
        @Path("page") page: Int,
        @Query("page_size") pageSize: Int= Constants.API_PAGE_SIZE
    ): Article

    /**
     * 问答文章
     */
    @GET("wenda/list/{page}/json")
    suspend fun qaArticle(
        @Path("page") page:Int,
        @Query("page_size") pageSize: Int= Constants.API_PAGE_SIZE
    ): Article

    /**
     * 广场文章
     */
    @GET("user_article/list/{page}/json")
    suspend fun squareArticle(
        @Path("page") page: Int,
        @Query("page_size") pageSize: Int= Constants.API_PAGE_SIZE
    ): Article

}