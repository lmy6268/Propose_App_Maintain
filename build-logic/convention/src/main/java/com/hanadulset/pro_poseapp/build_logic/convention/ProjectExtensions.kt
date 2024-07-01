package com.hanadulset.pro_poseapp.build_logic.convention

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugin.use.PluginDependency
import java.util.Optional

/**
 * *
 * 플러그인 내부에서 사용되는 Kotlin Extension 함수
 *
 * p.s.
 * - [internal] 은 모듈 내에서 만 사용할 수 있도록 제한하는 접근 제한자.
 *
 * */
internal val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun DependencyHandler.testImplementation(dependencyNotation: Any): Dependency? =
    add("testImplementation", dependencyNotation)

internal fun DependencyHandler.testRuntimeOnly(dependencyNotation: Any): Dependency? =
    add("testRuntimeOnly", dependencyNotation)

 fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? =
    add("implementation", dependencyNotation)

internal fun DependencyHandler.androidTestImplementation(dependencyNotation: Any): Dependency? =
    add("androidTestImplementation", dependencyNotation)

internal fun DependencyHandler.debugImplementation(dependencyNotation: Any): Dependency? =
    add("debugImplementation", dependencyNotation)

internal fun DependencyHandler.ksp(dependencyNotation: Any): Dependency? =
    add("ksp", dependencyNotation)

private val Optional<Provider<PluginDependency>>.getPluginId
    get() = get().get().pluginId

internal fun VersionCatalog.getPluginId(alias: String) = findPlugin(alias).getPluginId

internal fun VersionCatalog.getLibrary(alias: String): Provider<MinimalExternalModuleDependency> = findLibrary(alias).get()
