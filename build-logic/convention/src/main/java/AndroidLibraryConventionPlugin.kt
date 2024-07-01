import com.android.build.api.dsl.LibraryExtension
import com.hanadulset.pro_poseapp.build_logic.convention.AppConfigure
import com.hanadulset.pro_poseapp.build_logic.convention.NameAlias
import com.hanadulset.pro_poseapp.build_logic.convention.configureKotlinAndroid
import com.hanadulset.pro_poseapp.build_logic.convention.getPluginId
import com.hanadulset.pro_poseapp.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure


/** Plugin For Library ( Core )
 *
 * */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.getPluginId(NameAlias.Plugins.ANDROID_LIB))
                apply(libs.getPluginId(NameAlias.Plugins.KOTLIN_ANDROID))
            }
            extensions.configure<LibraryExtension> {
                lint.targetSdk = AppConfigure.TARGET_SDK
                configureKotlinAndroid(this)
            }
        }
    }
}