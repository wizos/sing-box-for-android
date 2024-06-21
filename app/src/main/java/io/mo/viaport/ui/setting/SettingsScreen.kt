package io.mo.viaport.ui.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.nekohasekai.sfa.R
import io.mo.viaport.ui.Routor

fun Routor.settingsId() = "settings"
fun NavGraphBuilder.settingsScreen() { //
    // XLog.d("userUiState 变化结束 Failure")
    composable(Routor.settingsId()) {
        SettingsScreen()
    }
}
fun NavController.toSettingsScreen(){
    navigate(Routor.settingsId())
}
@Composable
fun SettingsScreen() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = stringResource(id = R.string.app_name),
        )
    }
}