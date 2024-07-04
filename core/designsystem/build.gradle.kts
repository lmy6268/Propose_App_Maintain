plugins {
    alias(libs.plugins.propose.android.library)
    alias(libs.plugins.propose.android.library.compose)
}
android {
    namespace = "com.hanadulset.pro_poseapp.core.designsystem"
}

dependencies {
    api(libs.bundles.androidx.compose)
    debugApi(libs.bundles.androidx.compose.preview)
    implementation(libs.androidx.compose.iconExtension)
    implementation(libs.androidx.animation.android)
}