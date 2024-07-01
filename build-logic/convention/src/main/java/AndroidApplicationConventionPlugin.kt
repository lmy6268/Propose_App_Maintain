import com.android.build.api.dsl.ApplicationExtension
import com.hanadulset.pro_poseapp.build_logic.convention.AppConfigure
import com.hanadulset.pro_poseapp.build_logic.convention.NameAlias
import com.hanadulset.pro_poseapp.build_logic.convention.configureKotlinAndroid
import com.hanadulset.pro_poseapp.build_logic.convention.getPluginId
import com.hanadulset.pro_poseapp.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** 안드로이드 앱의 기본적인 의존성을 제공하는 플러그인
 *
 *
 * */

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.getPluginId(NameAlias.Plugins.ANDROID_APP))
                apply(libs.getPluginId(NameAlias.Plugins.KOTLIN_ANDROID))
            }

            extensions.configure<ApplicationExtension> {
                defaultConfig {
                    applicationId = AppConfigure.APPLICATION_ID
                    versionName = AppConfigure.Version.NAME
                    versionCode = AppConfigure.Version.CODE
                }

                defaultConfig.targetSdk = AppConfigure.TARGET_SDK
                configureKotlinAndroid(this)
            }

        }
    }
}