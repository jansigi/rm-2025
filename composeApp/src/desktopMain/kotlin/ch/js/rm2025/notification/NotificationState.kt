package ch.js.rm2025.notification

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object NotificationState {
    var dbInfoShown by mutableStateOf(false)
}