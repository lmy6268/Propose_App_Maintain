package com.example.proposeapplication.presentation.view

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.navigation.Navigation
import com.example.proposeapplication.presentation.MainViewModel
import com.example.proposeapplication.presentation.uistate.PermissionUiState
import com.example.proposeapplication.presentation.R
import com.example.proposeapplication.utils.PermissionDialog
import com.example.proposeapplication.utils.PermissionDialog.Companion.PERMISSIONS_REQUIRED
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * This [Fragment] requests permissions and, once granted, it will navigate to the next fragment
 */
@AndroidEntryPoint
class PermissionFragment : Fragment() {
    private val mainViewModel: MainViewModel by viewModels()
    private var isDid = false //권한 검사 여부
    private var permissionDialog: PermissionDialog? = null
    private var isMoved = false
    private val requestMultiplePermissions by lazy {
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            isDid = true
            for (i in it.keys) {
                if (it[i] == false) {
                    isDid = false
                    break
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermission()
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    //카메라 화면으로 이동하기
    private fun navigateToCamera() {
        //lifecycleScope.launchWhenStarted가 Deprecated 되어서 변경 해야 함.

        lifecycleScope.launch {
            withStarted {
                if (!isMoved) {
                    isMoved = true
                    Navigation.findNavController(requireActivity(), R.id.fragment_container)
                        .navigate(
                            PermissionFragmentDirections.actionPermissionToCamera() //카메라 프레그먼트로 이동
                        )
                }
            }
        }

    }


    private fun checkPermission() {
        lifecycleScope.launch {
            mainViewModel.apply { checkPermission() }
                .pUiState
                .collect() {
                    if (it is PermissionUiState.Success) {
                        when (it.value) {
                            true -> {
                                navigateToCamera()
                                permissionDialog?.closeDialog()
                            }

                            else -> {

                                if (isDid) permissionDialog.let {
                                    PermissionDialog(requireContext())
                                        .apply { setData() }
                                }.showDialog()
                                else requestMultiplePermissions.launch(PERMISSIONS_REQUIRED)
                            }
                        }

                    }
                }
        }
    }
}
