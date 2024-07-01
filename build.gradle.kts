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


//Properties properties = new Properties()
//properties.load(project.rootProject.file("local.properties").newDataInputStream())
//
//subprojects {
//    tasks.withType(KotlinCompile).configureEach {
//        kotlinOptions {
//
//            freeCompilerArgs += [
//                    "-P",
//                    "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${rootProject.file(".").absolutePath}/report/compose-reports"
//            ]
//
//
//            freeCompilerArgs += [
//                    "-P",
//                    "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination= ${rootProject.file(".").absolutePath}/report/compose-reports"
//            ]
//
//        }
//    }
//    afterEvaluate { project ->
//
//
//        if (project.hasProperty("android")) {
//            android {
//                compileSdk 34
//                buildFeatures {
//                    viewBinding true
//                    if (project.getName() != "opencv") buildConfig true
//                }
//                signingConfigs {
//                    release {
//                        storeFile file("key.keystore")
//                        storePassword ("123456")
//                        keyAlias ("keystore")
//                        keyPassword ("123456")
//                    }
//
//                }
//                buildTypes {
//                    release {
//                        signingConfig signingConfigs.release
//                    }
//                }
//
//                defaultConfig {
//                    minSdk 26
//                    targetSdk 34
//                    versionName "1.0.14"
//                    versionCode 28
//                    testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
//                    consumerProguardFiles "consumer-rules.pro"
//                    if (project.getName() != "opencv") {
//                        for (item in properties.keys()) {
//                            if (item != "sdk.dir")
//                                buildConfigField "String", "${item.toUpperCase()}", properties[item]
//                        }
//                    }
//
//                }
//
//                buildTypes {
//                    release {
//                        minifyEnabled false
//                        proguardFiles getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
//                    }
//                }
//                compileOptions {
//                    sourceCompatibility JavaVersion.VERSION_17
//                    targetCompatibility JavaVersion.VERSION_17
//                }
//                kotlinOptions {
//                    jvmTarget = "17"
//                }
//            }
//        }
//    }
//}