package com.hanadulset.pro_poseapp.presentation.core

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.component.LocalColors
import com.hanadulset.pro_poseapp.presentation.component.LocalTypography

object TermOfUseScreen {
    @Composable
    fun TermOfUseScreen(
        onSuccess: () -> Unit
    ) {
        val iconPainter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current).data(R.drawable.right_arrow)
                .placeholder(R.drawable.right_arrow).build()
        )
        val localTypography = LocalTypography.current
        val privacyLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(),
                onResult = {})
        val isChecked = remember {
            mutableStateOf(false)
        }
        val buttonSize = 15.dp

        Surface(
            modifier = Modifier.fillMaxSize(), color = LocalColors.current.primaryGreen100
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = "약관 동의",
                    style = localTypography.heading01,
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = 30.dp)
                        .background(
                            color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 20.dp)
                        )
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 50.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = "프로_포즈 에 오신 것을 환영합니다. \n" + "서비스 이용을 위해 아래의 약관 동의가 필요합니다.",
                        style = LocalTypography.current.sub02.copy(
                            textAlign = TextAlign.Start,
                            lineHeight = LocalDensity.current.run { (buttonSize + 5.dp).toSp() }
                        )
                    )
                    Divider()
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Checkbox(
                            modifier = Modifier.size(15.dp),
                            checked = isChecked.value,
                            onCheckedChange = {
                                isChecked.value = it
                            })
                        Text(
                            text = "(필수)",
                            style = LocalTypography.current.heading02,
                            fontSize = LocalDensity.current.run {
                                buttonSize.toSp()
                            }
                        )
                        Text(
                            text = "프로_포즈 서비스 이용 약관",
                            style = LocalTypography.current.sub02,
                            fontSize = LocalDensity.current.run {
                                (buttonSize - 2.dp).toSp()
                            }
                        )
                        Icon(painter = iconPainter,
                            contentDescription = "",
                            modifier = Modifier
                                .size(buttonSize + 5.dp)
                                .clickable {
                                    privacyLauncher.launch(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://sites.google.com/view/privacyhanadulset/%ED%99%88/privacy")
                                        )
                                    )
                                })
                    }
                }

                Button(
                    enabled = isChecked.value,
                    onClick = onSuccess,
                    colors = ButtonDefaults.buttonColors(
                        Color(0xFFFFFFFF)
                    ), shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "이용 동의",
                        Modifier.padding(vertical = 10.dp, horizontal = 30.dp),
                        style = localTypography.heading02
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewScreen() {
    TermOfUseScreen.TermOfUseScreen {

    }
}