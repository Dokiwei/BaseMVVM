# BaseMVVM
  
[![Github][Dokiwei]][Dokiwei-Url]
[![BaseMVVM][Dokiwei-BaseMVVM]][Dokiwei-BaseMVVM-Url]


## 简介

基于 Setruth 的 BaseMVVM 进行构建

Home页利用 `TabLayout`+`ViewPager2`+`SwiperRefrshLayout`+`RecyclerView` 进行数据流的分页与展示

Music页利用 `Media3`+`ExoPlayer`+`Service`+`RecyclerView` 进行数据的获取以及展示

Account界面按钮包括几个小功能:测试通知,协程作用域异常捕捉器,全局异常捕捉器,自定义View


## 目录

## 详细

## 遇到的问题

### Player持有Bitmap未释放 导致内存泄漏

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

## 鸣谢
[Setruth-BiliBili](https://space.bilibili.com/367514778/?spm_id_from=333.999.0.0)  [Setruth-Github](https://github.com/setruth)


## 推荐Android Up主

挺喜欢Setruth的视频的,还有抛物线(其实是扔物线,还是抛物线好记)

其他的视频包括什么享学课堂看着很难受,虽然理论知识很多,但就是理论太多了,很繁琐.

有个老外挺不错的,叫做 PhilippLackner ,是一个油管主[PhilippLackner-Youtube](https://www.youtube.com/@PhilippLackner)

我喜欢的这些Up一般讲的都很精练,并且技术也比较新



<!-- MARKDOWN LINKS & IMAGES -->
[Dokiwei]:https://img.shields.io/badge/Github-DokiWei-blue.svg?style=flat&logo=github&logoColor=#181717
[Dokiwei-Url]:https://github.com/Dokiwei
[Dokiwei-BaseMVVM]:https://img.shields.io/badge/Dokiwei-BaseMVVM-red.svg?style=flat&logo=github&logoColor=#181717
[Dokiwei-BaseMVVM-Url]:https://github.com/Dokiwei/BaseMVVM
