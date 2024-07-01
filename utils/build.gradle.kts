plugins {
    alias(libs.plugins.propose.android.library)
    alias(libs.plugins.propose.android.hilt)
    alias(libs.plugins.propose.android.room)
    id("kotlinx-serialization")
}

android {
    namespace = "com.hanadulset.pro_poseapp.utils"
}

dependencies {
    implementation(libs.bundles.android.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.android.test)

    implementation(libs.zip4j)

    //For Google Analytics
//    var firebase_version = "32.5.0"
//    // Import the Firebase BoM
//    implementation platform("com.google.firebase:firebase-bom:$firebase_version")
//    // Add the dependencies for the Remote Config and Analytics libraries
//    // When using the BoM, you don't specify versions in Firebase library dependencies
//    implementation 'com.google.firebase:firebase-analytics'

    implementation(libs.kotlinx.serialization.json)
}