import androidx.room.gradle.RoomExtension
import com.google.devtools.ksp.gradle.KspExtension
import com.hanadulset.pro_poseapp.build_logic.convention.NameAlias
import com.hanadulset.pro_poseapp.build_logic.convention.getLibrary
import com.hanadulset.pro_poseapp.build_logic.convention.getPluginId
import com.hanadulset.pro_poseapp.build_logic.convention.implementation
import com.hanadulset.pro_poseapp.build_logic.convention.ksp
import com.hanadulset.pro_poseapp.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.getPluginId(NameAlias.Plugins.ROOM))
                apply(libs.getPluginId(NameAlias.Plugins.KSP))
            }
            extensions.configure<KspExtension> {
                arg("room.generateKotlin", "true")
            }

            extensions.configure<RoomExtension> {
                // The schemas directory contains a schema file for each version of the Room database.
                // This is required to enable Room auto migrations.
                // See https://developer.android.com/reference/kotlin/androidx/room/AutoMigration.
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                implementation(libs.getLibrary(NameAlias.Library.ROOM.KTX))
                implementation(libs.getLibrary(NameAlias.Library.ROOM.RUNTIME))
                ksp(libs.getLibrary(NameAlias.Library.ROOM.COMPILER))
            }


        }
    }
}