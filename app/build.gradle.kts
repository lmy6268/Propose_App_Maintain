import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.propose.android.application)
    id("com.google.android.gms.oss-licenses-plugin")
    alias(libs.plugins.propose.android.hilt)
    alias(libs.plugins.propose.android.application.compose)
}

android {
    namespace = "com.hanadulset.pro_poseapp"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    //opencv모듈과 pytorch 모듈에서 동시에 존재하는 libc++_shared.so 파일 간의 충돌을 막기 위함.
    packaging {
        jniLibs.pickFirsts += "**/libc++_shared.so"
    }
    val properties = Properties().apply {
        load(FileInputStream(rootProject.file("keystore.properties")))
    }

    fun Properties.getValue(key: String) = this[key] as String


    signingConfigs {
        create("release") {
            storeFile = file(properties.getValue("SIGNED_STORE_FILE"))
            storePassword = properties.getValue("SIGNED_STORE_PASSWORD")
            keyAlias = properties.getValue("SIGNED_KEY_ALIAS")
            keyPassword = properties.getValue("SIGNED_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }


}

dependencies {
    implementation(libs.bundles.android.core)
    implementation(libs.junit)
    implementation(libs.bundles.android.test)

    //Paths
    implementation(projects.data)
    implementation(projects.domain)
    implementation(projects.presentation)
    implementation(projects.utils)

    implementation(libs.facebook.ads.sdk)


    //이미지 크롭
    implementation(libs.vanniktech.imageCropper)

    implementation(libs.androidx.splash)
    implementation(libs.bundles.aws.s3)
}