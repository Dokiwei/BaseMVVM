@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.dokiwei.basemvvm"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dokiwei.basemvvm"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    viewBinding{enable=true}
    dataBinding{enable=true}
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    //glide
    implementation(libs.glide)
    ksp(libs.glide.compiler)

    //blurry
    implementation(libs.blurry)

    //lyricViewX
    implementation(libs.lyricViewX)
    //lyric-getter
    implementation(libs.lyric.getter)

    //mp3agic
    implementation(libs.mp3agic)

    //navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation)

    //room
    implementation(libs.room.core)
    implementation(libs.room.paging)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    //paging3
    implementation(libs.paging3)

    //hilt
    implementation(libs.hilt)
    ksp(libs.hilt.compiler)

    //network
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    //cookie
    implementation(libs.persistentCookieJar)


    //refresh
    implementation(libs.refresh)

    //androidx
    implementation(libs.androidx.core)
    implementation(libs.androidx.media)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media)
    implementation(libs.androidx.media)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.palette)
    implementation(libs.core.ktx)

    implementation(libs.material)

    //test
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.junit)
    debugImplementation(libs.leakcanary.android)


}
kapt {
    correctErrorTypes = true
}