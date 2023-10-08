# 指定代码的压缩级别 0 - 7(指定代码进行迭代优化的次数，在Android里面默认是5，这条指令也只有在可以优化时起作用。)
-optimizationpasses 5
# 混淆时不会产生形形色色的类名(混淆时不使用大小写混合类名)
-dontusemixedcaseclassnames
# 指定不去忽略非公共的库类(不跳过library中的非public的类)
-dontskipnonpubliclibraryclasses
# 指定不去忽略非公共的的库类的成员
-dontskipnonpubliclibraryclassmembers
# 混淆时记录日志(打印混淆的详细信息)
# 这句话能够使我们的项目混淆后产生映射文件
# 包含有类名->混淆后类名的映射关系
-verbose
-printmapping proguardMapping.txt
# 指定混淆是采用的算法，后面的参数是一个过滤器
# 这个过滤器是谷歌推荐的算法，一般不做更改
-optimizations !code/simplification/cast,!field/*,!class/merging/*
#保护代码中的 Annotation 内部类不被混淆
-keepattributes *Annotation*,InnerClasses
-ignorewarnings
# 避免混淆泛型，这在 JSON 实体映射时非常重要，比如 fastJson
-keepattributes Signature
# 抛出异常时保留代码行号，在异常分析中可以方便定位
-keepattributes SourceFile,LineNumberTable

-keep class cn.lyric.getter.api.data.*{*;}
-keep class cn.lyric.getter.api.tools.EventTools{*;}
-keep class com.dokiwei.basemvvm.model.entity.**{*;}
-keep class com.dokiwei.basemvvm.model.data.**{*;}
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep class android.support.** {*;}
#----------------------------------------------------------------------------

#---------------------------------默认保留区---------------------------------
# 保留所有的本地 native 方法不被混淆
-keepclasseswithmembernames class * {native <methods>;
}
# 保留在 Activity 中的方法参数是 view 的方法，
# 从而我们在 layout 里面编写 onClick 就不会被影响
-keepclassmembers class * extends android.app.Activity{public void *(android.view.View);
}
# 枚举类不能被混淆
-keepclassmembers enum * {public static **[] values();public static ** valueOf(java.lang.String);
}
# 保留自定义控件（继承自 View）不被混淆
-keep public class * extends android.view.View{*** get*();void set*(***);public <init>(android.content.Context);public <init>(android.content.Context, android.util.AttributeSet);public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {public <init>(android.content.Context, android.util.AttributeSet);public <init>(android.content.Context, android.util.AttributeSet, int);
}
# 保留 Parcelable 序列化的类不被混淆
-keep class * implements android.os.Parcelable {*;
}
# 保留 Serializable 序列化的类不被混淆
-keep class * implements java.io.Serializable { *;}
-keepclassmembers class * implements java.io.Serializable {static final long serialVersionUID;private static final java.io.ObjectStreamField[] serialPersistentFields;private void writeObject(java.io.ObjectOutputStream);private void readObject(java.io.ObjectInputStream);java.lang.Object writeReplace();java.lang.Object readResolve();
}
# 对于 R（资源）下的所有类及其方法，都不能被混淆
-keep class **.R$* {
 *;
}
# 对于带有回调函数 onXXEvent 的，不能被混淆
-keepclassmembers class * {void *(**On*Event);
}

-keepclassmembers class * { public <init>(org.json.JSONObject);
}



# 保留继承的
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.support.annotation.**
#表示不混淆任何包含native方法的类的类名以及native方法名，这个和我们刚才验证的结果是一致
-keepclasseswithmembernames class * {
    native <methods>;
}
#表示不混淆任何一个View中的setXxx()和getXxx()方法，
#因为属性动画需要有相应的setter和getter的方法实现，混淆了就无法工作了。
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留R下面的资源
-keep class **.R$* {
 *;
}
#不混淆资源类下static的
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 保留我们自定义控件（继承自View）不被混淆
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class com.example.odm.garbagesorthelper.widget.** { *; }
#
#----------------------------- WebView(项目中没有可以忽略) -----------------------------
#
#webView需要进行特殊处理
#在app中与HTML5的JavaScript的交互进行特殊处理
#我们需要确保这些js要调用的原生方法不能够被混淆，于是我们需要做如下处理：
#
#---------------------------------实体类---------------------------
#--------(实体Model不能混淆，否则找不到对应的属性获取不到值)-----
#
-keep class com.example.odm.garbagesorthelper.model.entity.** { *; }
-dontwarn  com.example.odm.garbagesorthelper.model.entity.**
#对含有反射类的处理
-keep class com.example.odm.garbagesorthelper.utils.** { *; }
#
# ----------------------------- 其他的 -----------------------------
#
# 删除代码中Log相关的代码
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
# 保持测试相关的代码
-dontnote junit.framework.**
-dontnote junit.runner.**
-dontwarn android.test.**
-dontwarn android.support.test.**
-dontwarn org.junit.**
#
# ----------------------------- 第三方库、框架、SDK -----------------------------
#logger
-dontwarn com.orhanobut.logger.**
-keep class com.orhanobut.logger.**{*;}
-keep interface com.orhanobut.logger.**{*;}
# Gson
-keep class com.google.gson.stream.** { *; }
-keepattributes EnclosingMethod
-dontwarn com.google.gson.**
-keep class com.google.gson.**{*;}
-keep interface com.google.gson.**{*;}
#gson
#如果用到Gson解析包的，直接添加下面这几行就能成功混淆，不然会报错。
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }
# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# Application classes that will be serialized/deserialized over Gso
# OkHttp3
-dontwarn okhttp3.logging.**
-keep class okhttp3.internal.**{*;}
-dontwarn okio.**
# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
#-keepattributes Signature-keepattributes Exceptions
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Android
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
#LiveEventBus
-keep class androidx.lifecycle.** { *; }
-keep class androidx.arch.core.** { *; }
#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
# Media3 ExoPlayer
-dontwarn com.google.android.exoplayer3.**
-keep class com.google.android.exoplayer3.** { *; }
-keep interface com.google.android.exoplayer3.** { *; }
# AndroidX
-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# Coroutines
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }
-keep interface kotlinx.coroutines.** { *; }
