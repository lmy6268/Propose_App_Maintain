import com.hanadulset.pro_poseapp.build_logic.convention.NameAlias
import com.hanadulset.pro_poseapp.build_logic.convention.getLibrary
import com.hanadulset.pro_poseapp.build_logic.convention.getPluginId
import com.hanadulset.pro_poseapp.build_logic.convention.implementation
import com.hanadulset.pro_poseapp.build_logic.convention.ksp
import com.hanadulset.pro_poseapp.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.getPluginId(NameAlias.Plugins.KSP))
                apply(libs.getPluginId(NameAlias.Plugins.HILT))
            }
            dependencies {
                implementation(libs.getLibrary(NameAlias.Library.HILT.ANDROID))
                ksp(libs.getLibrary(NameAlias.Library.HILT.COMPILER))
            }
        }
    }

}