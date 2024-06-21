package io.mo.viaport.ui


import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.systemuicontroller.SystemUiController
import io.nekohasekai.sfa.App
import io.mo.viaport.ui.element.ViewHostState
import io.mo.viaport.viewmodel.AppViewModel

val LocalActivity = staticCompositionLocalOf<AppActivity> { error("No BasalActivity") }

val LocalApplication = staticCompositionLocalOf<App> { error("no Application provided") }

val LocalSystemUiController = staticCompositionLocalOf<SystemUiController> {
    error("no SystemUiController provided")
}

val LocalNavController = staticCompositionLocalOf<NavHostController> { error("no NavHostController provided") }

val LocalAppViewModel = staticCompositionLocalOf<AppViewModel> {
    error("no view model provided")
}

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("no snackbar host state provided")
}

val LocalViewHostState = staticCompositionLocalOf<ViewHostState> {
    error("no ViewHostState provided")
}

// val LazyViewHostState by lazy { ViewHostState() }

// val LazyCircleCropTransformation by lazy { CircleCropTransformation() }
//
// val LazyRoundedCornersTransformation by lazy { RoundedCornersTransformation(5f, 5f, 5f, 5f)  }

// 不分系统内的隐藏参数
val DefaultIconSize = 24.dp

val LocalIconButtonPadding = staticCompositionLocalOf<Dp> {
    error("no IconButtonPadding provided")
}

val LocalPrompter = staticCompositionLocalOf<Prompter> {
    error("no Prompter provided")
}

val LocalRssFeedIcons = staticCompositionLocalOf<VectorPainter> {
    error("no VectorPainter provided")
}
val LocalSettingIcons = staticCompositionLocalOf<VectorPainter> {
    error("no VectorPainter provided")
}
