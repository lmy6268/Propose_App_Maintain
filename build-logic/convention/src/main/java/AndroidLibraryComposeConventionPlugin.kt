import com.android.build.api.dsl.LibraryExtension
import com.hanadulset.pro_poseapp.build_logic.convention.NameAlias
import com.hanadulset.pro_poseapp.build_logic.convention.configureAndroidCompose
import com.hanadulset.pro_poseapp.build_logic.convention.getPluginId
import com.hanadulset.pro_poseapp.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = libs.getPluginId(NameAlias.Plugins.ANDROID_LIB))
            val extension = extensions.getByType<LibraryExtension>()
            configureAndroidCompose(extension)
        }
    }
}