package com.dokiwei.basemvvm.util

/**
 * @author DokiWei
 * @date 2023/9/11 19:03
 */
object Constants {
    const val BASE_URL = "https://www.wanandroid.com/"
    const val API_PAGE_SIZE = 40

    enum class HomeApiMethod {
        Home, Square, Qa
    }

    sealed class HomeViewPage(val title: String) {
        data object Home : HomeViewPage("主页")
        data object Square : HomeViewPage("广场")
        data object Qa : HomeViewPage("问答")
    }
}