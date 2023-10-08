# BaseMVVM

[![Github][Dokiwei]][Dokiwei-Url]
[![BaseMVVM][Dokiwei-BaseMVVM]][Dokiwei-BaseMVVM-Url]


## 简介

基于 Setruth 的 BaseMVVM 进行构建,其实原本只是想随便写点示例方便以后使用,但是看到`Media3`后随即有了一个做音乐播放器的念头,因为播放器还不够完善,所以以后再单独提出来

Home页利用 `TabLayout`+`ViewPager2`+`SwiperRefrshLayout`+`RecyclerView` 进行数据流的分页与展示

Music页利用 `Media3`+`ExoPlayer`+`Service`+`RecyclerView` 进行数据的获取以及展示,并且利用`palette`支持自动拾色

Account界面按钮包括几个小功能:测试通知,协程作用域异常捕捉器,全局异常捕捉器

|                            首页                            |                            播放器                            |                            播放页                            |                            歌词页                            |
| :--------------------------------------------------------: | :----------------------------------------------------------: | :----------------------------------------------------------: | :----------------------------------------------------------: |
| <img src="/img/首页.png" alt="图片1" style="zoom: 25%;" /> | <img src="/img/播放器.png" alt="播放器" style="zoom:25%;" /> | <img src="/img/音乐播放页.png" alt="播放页" style="zoom:25%;" /> | <img src="/img/歌词页.png" alt="播放页" style="zoom:25%;" /> |

|                       横屏-音乐播放页                        |                         横屏-播放器                          |
| :----------------------------------------------------------: | :----------------------------------------------------------: |
| <img src="/img/音乐播放页-横屏.png" alt="播放页" style="zoom:25%;" /> | <img src="/img/播放器-横屏.png" alt="播放页" style="zoom:25%;" /> |

## 目录

* [简介](#简介)
* [详细](#详细)
* [遇到的问题](#遇到的问题)
* [关键代码](#关键代码)
* [鸣谢](#鸣谢)

## 详细

#### 主页

这个就不讲了,就是利用`TabLayout`+`ViewPager2`+`SwiperRefrshLayout`+`RecyclerView`进行展示,`paging`进行数据获取,上一个项目用的多数据源,这次用的是单数据源即继承`PagingSource`,因为首页的三个pager使用的数据类都是一样的,所以没有做`PagingSource`的基类,而是直接使用枚举来改变`ApiService`,代码看 [关键代码](#1)

#### 播放器

其实播放器的列表页做的并不好,当时我是准备随便做做,所以直接照着主页的item改了一下,找起音乐来可能并不方便,以后会进行重新设计

其实我最喜欢的点就是根据歌词专辑图片来设置Ui的颜色即使用`AndroidX`的`Palette`,这一点看起来非常的赏心悦目,并且即使是图片背景,我们也可以去根据图片的颜色来判断该在上面使用什么颜色,虽然最后我决定在背景上添加了一个半透明遮罩来降低亮度以获取更好的显示效果,如果想完全使用`Palette`,可能需要去测量组件所在图片的位置,来限定扫描的颜色区域 [关键代码](#2)

另外一点就是主动适配了墨状态栏歌词的api,可以看下面图片 [歌词API](https://github.com/xiaowine/Lyric-Getter-Api) [墨·状态栏歌词](https://github.com/Block-Network/StatusBarLyric) 因为player只能在主线程中使用,所以不能用协程,没办法我只能使用handler [关键代码](#3)

| <img src="/img/状态栏歌词.png" alt="图片1" style="zoom:25%;" /> | <img src="/img/状态栏歌词1.png" alt="图片1" style="zoom:25%;" /> | <img src="/img/状态栏歌词2.png" alt="图片1" style="zoom:25%;" /> |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |

其他功能

| <img src="/img/播放器-筛选功能.png" alt="图片1" style="zoom:25%;" /> | <img src="/img/播放器-搜索.png" alt="图片1" style="zoom:25%;" /> | <img src="/img/播放器-当前播放列表.png" alt="图片1" style="zoom:25%;" /> |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |



### 设置页

协程作用域异常拦截实际上就是去继承`CoroutineExceptionHandler`,然后启动协程时在`context`中加入,并且最好配合`supervisorScope`一起使用,这个是当`supervisorScope`作用域中的任务出现异常取消时,不会取消作用域,也就是其他任务还会正常执行,不然默认情况下当一个任务出现异常,整个作用域都会被关闭

全局异常拦截的话其实就是继承`Thread.UncaughtExceptionHandler`并重写`uncaughtException`,这个太长了简单展示一点,其实实现它主要就是为了在应用强行退出之前去保存或者是上传错误日志,因为有的时候发生错误logcat都获取不到

```kotlin
	override fun uncaughtException(t: Thread, e: Throwable) {
        Log.e("异常捕捉", "Thread:${t.name} ${t.id} error:${e.message}")
        val countDownLatch = CountDownLatch(1)
        Thread {
            Looper.prepare()
            Toast.makeText(context, "系统崩溃了~\n错误信息:${e.message}\n正在保存日志", 				Toast.LENGTH_SHORT).show()
            //这是我实现了本地保存日志
            collectBaseInfo()
            saveErrorInfo(t, e)
            sleep(1500)
            countDownLatch.countDown()
            Looper.loop()
        }.start()
        countDownLatch.await()
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(0)
    }
```

以后添加对于音乐界面的配置,比如播放页背景使用纯色而不是模糊图,或者是流体

## 遇到的问题

### Player持有Bitmap未释放 导致内存泄漏

#### 初始优化,解决 Leak

在写`Music`功能的时候出现了内存泄漏

在发现运存消耗达到了 1GB 将近 2GB 甚至复现场景无限增长后赶紧就去`Profiler`截取了一个内存快照

发现 `MusicFragment` 发生了内存泄漏

因为第一次遇见内存泄漏页不知从哪下手,因为之前 `Compose` 开发应用的话从来都没有出现内存泄漏的情况

在一番查阅资料学习分析思维后,发现是 `Bitmap` 的引用未释放导致的

一开始我在写数据类时使用的 `Bitmap` 来保存专辑图片,这样不仅让数据对象过大,而且在适配器设置图片后也没法对其释放,因为所有的数据还在 `Player` 的 `MediaItems` 中引用着

之后我先是在 `Fragment` 的 `onDestroy()` 对 `Player` 进行了释放,虽然内存泄漏解决了,但是 `Bitmap` 还是占用了大量的内存,包括在图像大量加载时, `Fragment` 销毁了也无法正常 GC 这些对象

于是我先是把数据类的存储图片的方式改为了存储他的专辑图片id,这样通过一些转换,最终还是可以得到这个 `Bitmap`

然后使用 `Glide` 进行图片的加载,因为之前在 `Compose` 使用的是coil进行的图片的异步加载,于是看了一下 `Glide` 的基本用法

使用 `Glide` 的 `load()` 方法传入 'Activity' 或是 `Fragment` ,当这些页面载体销毁时, `Glide` 会自动的跟着他们的生命周期进行销毁,以此来让 `Bitmap` 得到释放

最终应用在 GC 后能顺利的保持在 200多MB 的内存,在 GC 后还有未释放的 `Bitmap` ,可能是内存中的一些缓存,目前还未找出引用源
#### 再次优化,防止OOM

虽然没有了内存泄露的问题,但是如果在加载图片时一直使用原图进行加载,是非常危险的

应用虽然可能会在执行一段时间自动释放一些用不到的内存,但是如果用户快速的滑动布局导致应用快速的去加载大量图片,就可能导致OOM的问题

~~首先肯定是对于Bitmap本身的优化,可以使用 `BitmapFactory` 的 `Options` 对Bitmap进行进一步的优化,通过 `inSampleSize` 去设置图片的采样率~~

~~比如图片原来是一个 8 * 8 的正方体,那么设置采样率为 2 就会得到一个 4 * 4 的正方体~~

~~如果原来一个图片的长宽为 4000 * 2000 ,而Glide图片是以RGB_565(16bit 每像素2byte)格式加载的,那么他在内存中的大小为 4000 * 2000 * 2 / 1024 /1024 = 15MB 这是非常恐怖的~~

~~设置采样为 2 后会得到一个 4000/2 * 2000/2 * 2 / 1024 / 1024 = 3.8MB 这样下来可以大幅的去降低 `bitmap` 对于内存的影响~~

~~不过我们不可能直接去指定采样率,而是通过判断容器也就是 `ImageView` 的大小去设置~~

~~还可以通过 `Glide` 的 override 对图片再次进行剪裁~~

~~最后在每张音乐专辑仅仅只占用了 100多KB, GC 后内存仅仅只占用了 120MB ~ 160MB~~

后来发现原来获取专辑的方法有点问题,在Android P以下获取专辑图片可能失效,所以最后我直接使用了图片的 `[ByteArry]` 让 `Glide` 进行管理(P以上使用系统的转换方式,P以下通过 `Mp3agic `经过读取,但是通过 `Mp3agic `读取需要存储权限,并且Android13限制了存储权限的使用,如果让用户来选择文件的话,会影响用户体验,所以我在清单文件中添加了一个权限`android.permission.MANAGE_EXTERNAL_STORAGE`,不过此权限会被谷歌商店限制,以后可能会使用改变扫描歌曲文件的方式来获取文件的读取权限,即在Android13的设备上让用户选择扫描文件夹的位置,并以此来获得此文件夹的读取权限)

#### 第三次优化,销毁时取消所有观察者模式

之后再使用中再次遇见了 Leak ,一时难以溯源,所以使用了`LeakCanary`,这次是因为给 player 的监听事件在 fragment 销毁时未被移除,也是从现在开始,每次遇到含有监听事件或者是观察者模式的对象,我都会尝试在片段销毁时尝试释放,之后在子片段中通过 `viewModel` 共享 player,也避免了获取 player 的时间消耗,然后在生命周期作用域中的 `withStarted` 中对 `viewModel` 中的 `player` 进行监听,因为屏幕旋转之后 `viewModel` 未使用 `SaveState` 的数据会丢失

## 关键代码

### 主页

<p id="1">这样就实现了一个复用同一个fragment展示不同的数据</p> 

```kotlin
	//PagerAdapter关键代码
	override fun createFragment(position: Int): Fragment {
        val fragment = HomeRVFragment().apply {
            arguments = Bundle().apply { putInt("flag", position) }
        }
        return fragment
    }

	//RvFragment关键代码
	val flag = when (arguments?.getInt("flag")) {
            0 -> Constants.HomeApiMethod.Home
            1 -> Constants.HomeApiMethod.Square
            else -> Constants.HomeApiMethod.Qa
        }
	lifecycleScope.launch {
            when (flag) {
                Constants.HomeApiMethod.Home -> viewModel?.data(Constants.HomeApiMethod.Home)
                    ?.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }

                Constants.HomeApiMethod.Square -> viewModel?.data(Constants.HomeApiMethod.Square)
                    ?.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }

                Constants.HomeApiMethod.Qa -> viewModel?.data(Constants.HomeApiMethod.Qa)
                    ?.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
            }
        }

	//ViewModel关键代码
	fun data(flag:Constants.HomeApiMethod) = Pager(PagingConfig(pageSize = 20, initialLoadSize = 40)){
        HomePagingSource(flag)
    }.flow.cachedIn(viewModelScope)

	//PagingSource 关键代码
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
```

### 播放器

<p id="2">Palette的使用</p> 

```kotlin
	//传入一个生成的palette,返回一个Pair,第一项是强调色,第二项是根据强调色来返回黑或白,用于防止显示在强调色上看不清
	fun paletteColor(palette: Palette?): Pair<Int, Int>? {
        return palette?.let {
            val swatch = it.dominantSwatch ?: it.lightVibrantSwatch ?: it.lightMutedSwatch
            ?: it.vibrantSwatch ?: it.mutedSwatch ?: it.darkVibrantSwatch ?: it.darkMutedSwatch
            swatch?.let { s ->
                val color = s.rgb
                val onRgb = when {
                    calculateRelativeLuminance(color) > 0.5 -> Color.BLACK
                    else -> Color.WHITE
                }
                Pair(color, onRgb)
            }
        }
    }

	//这块对于颜色的相对亮度是在网上搜索
	private fun calculateRelativeLuminance(color: Int): Double {
        //获取颜色的RGB分量，并转换为0到1之间的数值
        var red = Color.red(color) / 255.0
        var green = Color.green(color) / 255.0
        var blue = Color.blue(color) / 255.0
        //根据公式计算每个分量的新值
        red = if (red <= 0.03928) red / 12.92 else ((red + 0.055) / 1.055).pow(2.4)
        green = if (green <= 0.03928) green / 12.92 else ((green + 0.055) / 1.055).pow(2.4)
        blue = if (blue <= 0.03928) blue / 12.92 else ((blue + 0.055) / 1.055).pow(2.4)
        //将得到的三个新分量分别乘以系数后相加，得到相对亮度
        return red * 0.2126 + green * 0.7152 + blue * 0.0722
    }

	//使用
	Palette.from(img).generate { palette ->
    	val pair = paletteColor(palette)
	}
```

<p id="3">状态栏歌词API实现</p> 

```kotlin
	private val handler = Handler(Looper.getMainLooper())
    private var path: String? = null
    private var isPlaying = false
    private var lyricsList: List<LyricEntry>? = null
	// 当获得player的元数据变化时开始显示歌词
    private val mediaMetadataChange = Runnable {
        when (isPlaying) {
            true -> {
                path?.let { p ->
                    val lyrics = Mp3File(p).takeIf { it.hasId3v2Tag() }?.id3v2Tag?.lyrics
                    this@MediaService.lyricsList = lyrics?.let { parseLrc(it) }
                    handler.removeCallbacks(updateLyrics)
                    handler.post(updateLyrics)
                }
            }

            false -> {
                handler.removeCallbacks(updateLyrics)
                EventTools.stopLyric(applicationContext)
            }
        }
    }
	//利用postDelayed不断更新歌词
    private val updateLyrics = object : Runnable {
        override fun run() {
            lyricsList?.let { list ->
                list.filter { it.time >= player.currentPosition }
                    .minByOrNull { it.time - player.currentPosition }?.let {
                        lyricsList?.indexOf(it)
                    }?.let { index ->
                        if (index + 1 < list.size) {
                            EventTools.sendLyric(
                                applicationContext,
                                list[index].text,
                                application.packageName,
                                (list[index + 1].time - list[index].time).toInt()
                            )
                            handler.postDelayed(this, list[index + 1].time - list[index].time)
                        } else {
                            EventTools.sendLyric(
                                applicationContext, list[index].text, application.packageName
                            )
                            handler.postDelayed(this, 1000)
                        }
                    }
            }
        }
    }
	
	//这些是play的监听事件以及销毁应执行的事件
	override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        handler.post {
            isPlaying = playWhenReady
            handler.removeCallbacks(mediaMetadataChange)
            handler.post(mediaMetadataChange)
        }
    }
    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        handler.post {
            player.mediaMetadata.description?.let {
                path = it.toString()
            }
            handler.removeCallbacks(mediaMetadataChange)
            handler.post(mediaMetadataChange)
        }
    }
	override fun onDestroy() {
        handler.removeCallbacks(mediaMetadataChange)
        handler.removeCallbacks(updateLyrics)
        EventTools.stopLyric(applicationContext)
        unregisterLyricListener(applicationContext)
        player.removeListener(this)
        player.release()
        session.release()
        ...
    }
```



## 鸣谢
[Setruth-BiliBili](https://space.bilibili.com/367514778/?spm_id_from=333.999.0.0)  [Setruth-Github](https://github.com/setruth) 从他这里学习的BaseMVVM的基本框架的创建

 [歌词API](https://github.com/xiaowine/Lyric-Getter-Api) [墨·状态栏歌词](https://github.com/Block-Network/StatusBarLyric) 状态栏歌词API

以及其他项目中用到的三方库


## 推荐Android Up主

挺喜欢Setruth的视频的,还有抛物线(其实是扔物线,还是抛物线好记)

有个老外挺不错的,叫做 PhilippLackner ,是一个油管主[PhilippLackner-Youtube](https://www.youtube.com/@PhilippLackner)

这些Up一般讲的都很精练,并且技术也比较新



<!-- MARKDOWN LINKS & IMAGES -->
[Dokiwei]:https://img.shields.io/badge/Github-DokiWei-blue.svg?style=flat&logo=github&logoColor=#181717
[Dokiwei-Url]:https://github.com/Dokiwei
[Dokiwei-BaseMVVM]:https://img.shields.io/badge/Dokiwei-BaseMVVM-red.svg?style=flat&logo=github&logoColor=#181717
[Dokiwei-BaseMVVM-Url]:https://github.com/Dokiwei/BaseMVVM
