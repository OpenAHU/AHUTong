plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}


android {
    namespace = "com.ahu.ahutong"
    compileSdk = 36


    lint {
        //即使报错也不会停止打包
        abortOnError = false
        //打包release版本的时候是否进行检测
        checkReleaseBuilds = false
    }
    //关闭PNG合法性检查
    // aaptOptions.useNewCruncher = false
    defaultConfig {
        applicationId = "com.ahu.ahutong"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0-beta6"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
            abiFilters.add("x86")
            abiFilters.add("x86_64")
        }

    }

    defaultConfig {
        applicationId = "com.ahu.ahutong"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "2.0.0-beta1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isShrinkResources = true  // 移除无用的resource文件
            isMinifyEnabled = true //是否对代码进行混淆，true表示混淆
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            ndk {
                abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
            }
//            signingConfig = signingConfigs.getByName("my_custom_debug_sign")
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
//            signingConfig = signingConfigs.getByName("my_custom_debug_sign")
        }
    }
//    packagingOptions {
//        resources {
//            excludes += ['META-INF/ASL2.0', 'META-INF/LICENSE', 'META-INF/NOTICE', 'META-INF/MANIFEST.MF']
//        }
//    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
        compose = true
        buildConfig = true
    }
    kapt.includeCompileClasspath = false

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.0-alpha02"
    }
}

dependencies {
    implementation(libs.crashreport)
    implementation(libs.ads.mobile.sdk)

    val composeBom = platform("androidx.compose:compose-bom:2022.12.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.persistentcookiejar)
    implementation(libs.coil)
    implementation(libs.mmkv.static)
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.jsoup)

    implementation(libs.androidx.runtime.livedata)
    implementation(libs.ui)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.accompanist.flowlayout)
    implementation(libs.coil.compose)
    implementation(libs.monet)

    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.zxing.android.embedded)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)
}