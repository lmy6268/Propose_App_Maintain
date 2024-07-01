import com.android.build.api.dsl.ApplicationExtension
import com.hanadulset.pro_poseapp.build_logic.convention.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.getByType<ApplicationExtension>().run {
                configureAndroidCompose(this)
            }
        }
    }
}