import kotlin.Suppress

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.ossLicensesPlugin)
        classpath(libs.kotlinx.serialization.jsonPlugin)
    }
}
////    // Add the dependency for the Google services Gradle plugin
////    id "com.google.gms.google-services" version "4.4.0" apply false
//}
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.dagger.hilt) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.google.devtool.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.navigation.saveargs) apply false
}


apply {
    from("./gradle/projectDependencyGraph.gradle")
}
