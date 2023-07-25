package com.example.proposeapplication.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

class PermissionDialog(private val cxt: Context) : AlertDialog.Builder(cxt) {
    //어떤 권한이 부족한지 데이터를 세팅한다.
    private lateinit var dialog: AlertDialog
    fun setData() {
        this.setTitle("애플리케이션을 사용하기 위해 권한이 필요합니다.")
        this.setPositiveButton(
            "권한 설정하러 가기"
        ) { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS) //설정화면에서 권한을 요청하러 감.
            intent.setData(
                Uri.fromParts("package", context.packageName, null)
            ).apply {
                cxt.startActivity(this) //설정화면으로 이동한다.
            }
        }.setNegativeButton("앱 종료") { _, _ ->
            (cxt as Activity).finish()

        }
        dialog = this.create()
    }

    fun showDialog() =
        dialog.show()
    fun closeDialog() = dialog.dismiss()

    companion object{
        val PERMISSIONS_REQUIRED = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) else arrayOf(
            Manifest.permission.CAMERA
        )
    }
}