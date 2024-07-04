plugins {
    alias(libs.plugins.propose.android.feature)
    alias(libs.plugins.propose.android.library.compose)
    alias(libs.plugins.propose.android.hilt)
    id("kotlinx-serialization")

//    // Add the Google services Gradle plugin
//    id 'com.google.gms.google-services'
}

android {
    namespace = "com.hanadulset.pro_poseapp.presentation"
}

dependencies {
    implementation(libs.bundles.android.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.android.test)
    implementation(projects.utils)
    implementation(projects.domain)

    implementation(libs.ossLicenses)
    implementation(libs.androidx.compose.navigation)
    api(libs.bundles.androidx.compose)
    //Ar
//    implementation 'com.google.ar:core:1.39.0'
//    implementation 'io.github.sceneview:arsceneview:0.10.2'
    implementation(libs.coil.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.systemUiController)

    implementation(libs.bundles.cameraX)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.splash)

    //이미지 크롭
    implementation(libs.vanniktech.imageCropper)

    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewModelCompose)

//    //업데이트 확인
//    implementation 'com.google.android.play:app-update-ktx:2.1.0'
//    implementation 'com.google.android.play:core:1.10.3'

}