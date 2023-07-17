/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.propose_application

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.navigation.Navigation
import com.example.camera.core.dialog.PermissionDialog
import kotlinx.coroutines.launch

private val PERMISSIONS_REQUIRED = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

/**
 * This [Fragment] requests permissions and, once granted, it will navigate to the next fragment
 */
class PermissionFragment : Fragment() {
    private var isDid: Boolean = false
    private var permissionDialog: PermissionDialog? = null

    private fun checkIsGranted(permissions: Map<String, Boolean>) {
        permissions.forEach { actionMap ->

            //Pie 이하인 경우, 저장소 권한(읽기, 쓰기)을 추가로 받아야 함
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                when (actionMap.key) {
                    Manifest.permission.READ_EXTERNAL_STORAGE -> {
                        if (actionMap.value) //저장소 읽기 권한이 허가된 경우
                        {
                            Log.d("저장소 읽기 권한", "허용됨")
                        } else {
                            Log.e("저장소 읽기 권한", "거부됨")
                        }
                    }

                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                        if (actionMap.value) //저장소 쓰기 권한이 허가된 경우
                        {
                            Log.d("저장소 쓰기 권한", "허용됨")
                        } else {
                            Log.e("저장소 쓰기 권한", "거부됨")
                        }
                    }
                }
            }

            //카메라 권한은 공통 필요 권한.
            if (actionMap.key == Manifest.permission.CAMERA) {
                if (actionMap.value) //카메라 권한이 허가된 경우
                {
                    Log.d("카메라 권한", "허용됨")

                } else {
                    Log.e("카메라 권한", "거부됨")
                }
            }
        }
    }

    //권한 허가 요청을 위한 변수
    private val requestMultiplePermissions by lazy {
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            checkIsGranted(permissions)
            //만약 허가가 안된 내용이 있는 경우
            if (!hasPermissions(requireContext())) {
                //권한이 필요하다는 다이얼로그를 표시한다.
                permissionDialog = PermissionDialog(requireContext()).apply {
                    setData()
                    showDialog()
                }
            }
            isDid = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (hasPermissions(requireContext())) {
            //만약 권한을 얻었다면, 카메라 화면으로 이동한다.
            navigateToCamera();
        } else {
            requestMultiplePermissions.launch(PERMISSIONS_REQUIRED)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isDid) {
            //만약 허가가 안된 내용이 있는 경우
            if (!hasPermissions(requireContext())) {
                permissionDialog!!.showDialog()//권한이 필요하다는 다이얼로그를 표시한다.
            } else {
                if (permissionDialog != null) permissionDialog!!.closeDialog()
                navigateToCamera()
            }
        }
    }


    //카메라 화면으로 이동하기
    private fun navigateToCamera() {
        //lifecycleScope.launchWhenStarted가 Deprecated 되어서 변경 해야 함.
        lifecycleScope.launch {
            withStarted {
                Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                    PermissionFragmentDirections.actionPermissionToCamera() //카메라 프레그먼트로 이동
                )
            }
        }

    }

    companion object {
        /** Convenience method used to check if all permissions required by this app are granted */
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(
                context, it
            ) == PackageManager.PERMISSION_GRANTED

        }
    }
}
