package com.hanadulset.pro_poseapp.presentation.feature.setting

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.hanadulset.pro_poseapp.presentation.BuildConfig
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.component.LocalColors
import com.hanadulset.pro_poseapp.presentation.component.LocalTypography

object SettingScreen {
    @Composable
    fun Screen(
        modifier: Modifier = Modifier,
    ) {
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val ossLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {
            })
        val privacyLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {

            })
        val context = LocalContext.current
        val iconPainter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.app_icon_rounded)
                .build()
        )



        Surface(
            color = LocalColors.current.primaryGreen100,
            modifier = modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.3F)
                ) {
                    Row(
                        Modifier
                            .align(Alignment.Center)
                            .width(240.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Image(
                            painter = iconPainter,
                            modifier = Modifier.size(80.dp),
                            contentDescription = "앱 아이콘 "
                        )
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(text = "Pro_Pose")
                            Text(
                                text = "v${
                                    context.packageManager.getPackageInfo(
                                        context.packageName,
                                        0
                                    ).versionName
                                }"
                            )
                        }
                    }


                }

                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(screenHeight * 0.7F)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
                    elevation = 10.dp
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(
                            54.dp,
                            Alignment.Top
                        )
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(text = "앱 정보", style = LocalTypography.current.heading01)
                            Divider()
                        }
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = LocalColors.current.subPrimaryBlack80,
                                contentColor = LocalColors.current.secondaryWhite80
                            ),
                            onClick = {
                                ossLauncher.launch(
                                    Intent(
                                        context.applicationContext,
                                        OssLicensesMenuActivity::class.java
                                    )
                                )
                            }) {
                            Text(text = "오픈 라이선스 확인", style = LocalTypography.current.sub01)
                        }
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = LocalColors.current.subPrimaryBlack80,
                                contentColor = LocalColors.current.secondaryWhite80
                            ),
                            onClick = {
                                privacyLauncher.launch(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://sites.google.com/view/privacyhanadulset/%ED%99%88/privacy")
                                    )
                                )
                            }) {
                            Text(text = "개인정보 처리 방침", style = LocalTypography.current.sub01)
                        }
                    }
                }
            }


        }
    }

    //개인정보 처리 방침 안내 페이지
    @Composable
    fun PrivacyPageScreen(
        modifier: Modifier = Modifier,

        ) {

    }


    //약관 안내 페이지
    @Composable
    fun AgreementPageScreen() {

    }
}


//@Preview(name = "NEXUS_5", device = Devices.NEXUS_5)
//@Preview(name = "NEXUS_6", device = Devices.NEXUS_6)
//@Preview(name = "NEXUS_5X", device = Devices.NEXUS_5X)
//@Preview(name = "NEXUS_6P", device = Devices.NEXUS_6P)
//@Preview(name = "PIXEL", device = Devices.PIXEL)
//@Preview(name = "PIXEL_2", device = Devices.PIXEL_2)
//@Preview(name = "PIXEL_3", device = Devices.PIXEL_3)
//@Preview(name = "PIXEL_3_XL", device = Devices.PIXEL_3_XL)
//@Preview(name = "PIXEL_3A", device = Devices.PIXEL_3A)
//@Preview(name = "PIXEL_3A_XL", device = Devices.PIXEL_3A_XL)
//@Preview(name = "PIXEL_4", device = Devices.PIXEL_4)
//@Preview(name = "PIXEL_4_XL", device = Devices.PIXEL_4_XL)
@Composable
fun TestSettingScreen() {
    SettingScreen.Screen()

}