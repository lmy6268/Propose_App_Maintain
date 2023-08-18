package com.example.proposeapplication.presentation.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties


object CustomDialog {
    @Composable
    fun CustomAlertDialog(
        onDismissRequest: () -> Unit,
        properties: DialogProperties = DialogProperties(),
        content: @Composable () -> Unit,
    ) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = properties
        ) {
            content()
        }
    }
}