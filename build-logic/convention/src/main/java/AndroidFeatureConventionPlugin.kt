import com.android.build.api.dsl.LibraryExtension
import com.hanadulset.pro_poseapp.build_logic.convention.NameAlias
import com.hanadulset.pro_poseapp.build_logic.convention.getLibrary
import com.hanadulset.pro_poseapp.build_logic.convention.implementation
import com.hanadulset.pro_poseapp.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("propose.android.library")
                apply("propose.android.hilt")
            }
            extensions.configure<LibraryExtension> {
                testOptions.animationsDisabled = true
            }

            dependencies {
//                implementation(project(NameAlias.Path.CORE.UI))
//                implementation(project(NameAlias.Path.CORE.DESIGN_SYSTEM))
                implementation(libs.getLibrary(NameAlias.Library.COMPOSE.HILT_NAVIGATION_COMPOSE))
                implementation(libs.getLibrary(NameAlias.Library.COMPOSE.VIEWMODEL_COMPOSE))
                implementation(libs.getLibrary(NameAlias.Library.COMPOSE.RUNTIME_COMPOSE))
            }
        }
    }
}