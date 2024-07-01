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