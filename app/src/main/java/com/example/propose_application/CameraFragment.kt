package com.example.propose_application

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.propose_application.databinding.FragmentCameraBinding


class CameraFragment : Fragment() {
    lateinit var fragmentCameraViewBinding: FragmentCameraBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentCameraBinding.inflate(inflater).apply {
            fragmentCameraViewBinding = this
        }.root

    //뷰가 생성된 후, 각 값 세팅해주기

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}