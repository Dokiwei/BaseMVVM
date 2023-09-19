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

    sealed class AnimProperty(val name: String) {
        //透明度动画
        data object Alpha : AnimProperty("alpha")

        //旋转动画:围绕X轴旋转
        data object RotationX : AnimProperty("rotationX")

        //旋转动画:围绕Y轴旋转
        data object RotationY : AnimProperty("rotationY")

        //旋转动画:围绕z轴旋转
        data object Rotation : AnimProperty("rotation")

        //平移动画:在x轴上平移
        data object TranslationX : AnimProperty("translationX")

        //平移动画:在y轴上平移
        data object TranslationY : AnimProperty("translationY")

        //缩放动画：在x轴缩放
        data object ScaleX : AnimProperty("scaleX")

        //缩放动画：在y轴上缩放
        data object ScaleY : AnimProperty("scaleY")
    }

    sealed class LogTag(val tag:String){
        data object Default:LogTag("默认")
        data object Lifecycle:LogTag("Lifecycle")
    }
}