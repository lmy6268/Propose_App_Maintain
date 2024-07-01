plugins {
    alias(libs.plugins.propose.android.library)
    alias(libs.plugins.propose.android.hilt)
}

android {
    namespace = "com.hanadulset.pro_poseapp.domain"
}

dependencies {
    implementation(libs.bundles.android.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.android.test)
    implementation(projects.utils)
    implementation(libs.bundles.cameraX)
}