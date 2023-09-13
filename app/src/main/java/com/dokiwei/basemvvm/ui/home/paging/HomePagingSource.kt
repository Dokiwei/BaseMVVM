package com.dokiwei.basemvvm.ui.home.paging

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dokiwei.basemvvm.model.data.ArticleData
import com.dokiwei.basemvvm.model.data.ArticleDatas
import com.dokiwei.basemvvm.network.client.RetrofitClient
import com.dokiwei.basemvvm.util.Constants
import retrofit2.HttpException
import java.io.IOException

/**
 * @author DokiWei
 * @date 2023/9/11 18:54
 */
class HomePagingSource(private val flag: Constants.HomeApiMethod) : PagingSource<Int, ArticleDatas>() {
    override fun getRefreshKey(state: PagingState<Int, ArticleDatas>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleDatas> {
        return try{
            val currentPage = params.key ?: 0
            val data = when(flag){
                Constants.HomeApiMethod.Home ->
                    RetrofitClient.homeApi.homeArticle(currentPage)
                Constants.HomeApiMethod.Square ->
                    RetrofitClient.homeApi.squareArticle(currentPage)
                Constants.HomeApiMethod.Qa ->
                    RetrofitClient.homeApi.qaArticle(currentPage)
            }
            val prevKey = if (currentPage != 0) data.data.curPage - 1 else null
            val nextKey = data.data.curPage + 1
            LoadResult.Page(data = data.data.datas, prevKey = prevKey, nextKey = nextKey)
        }catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }
}