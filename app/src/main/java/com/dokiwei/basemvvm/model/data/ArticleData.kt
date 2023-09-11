package com.dokiwei.basemvvm.model.data

/**
 * @author DokiWei
 * @date 2023/9/11 18:47
 */
data class Article(
    val data:ArticleData,
    val errorCode:Int,
    val errorMsg:String
)
data class ArticleData(
    val curPage:Int,
    val datas:List<ArticleDatas>,
    val size:Int
)
data class ArticleDatas(
    val id: Int,
    val title: String,
    val author: String,
    val shareUser: String,
    val superChapterName: String,
    val chapterName: String,
    val niceDate: String,
    val niceShareDate: String,
    val collect: Boolean,
    val link: String,
    val fresh: Boolean,
    val tags: List<TagData>,
    val userId:Int
)