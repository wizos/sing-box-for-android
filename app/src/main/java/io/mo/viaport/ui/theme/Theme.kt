package io.mo.viaport.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.mo.viaport.ui.LocalSystemUiController

private val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Grey500,
    onSecondary = Color.White,
    onPrimary = Color.White,
    background = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    primaryContainer = Purple700,
    secondary = Grey500,
    onSecondary = Color.White,
    onPrimary = Color.White,
    background = Color.White
)

private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Grey100,
    onSecondary = Color.White,
    onPrimary = Color.White,
    background = Black100
)
private val DarkColorScheme = darkColorScheme(
    primary = Purple200,
    primaryContainer = Purple700,
    secondary = Grey100,
    onSecondary = Color.White,
    onPrimary = Color.White,
    background = Black100
)

@Composable
fun ScreenTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    useDynamicColors: Boolean = true,
    enableEdgeToEdge: (Boolean, Int, Int) -> Unit,
    content: @Composable () -> Unit) {

    val colorScheme = when {
        useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    // https://stackoverflow.com/questions/65610216/how-to-change-statusbar-color-in-jetpack-compose
    // val view = LocalView.current
    // if (!view.isInEditMode) {
    //     SideEffect {
    //         val activity  = view.context as Activity
    //         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    //             activity.window.navigationBarColor = colorScheme.primary.copy(alpha = 0.08f).compositeOver(colorScheme.surface.copy()).toArgb()
    //             activity.window.statusBarColor = colorScheme.background.toArgb()
    //             WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = !useDarkTheme
    //             WindowCompat.getInsetsController(activity.window, view).isAppearanceLightNavigationBars = !useDarkTheme
    //         }
    //     }
    // }
    DisposableEffect(useDarkTheme) {
        // navigationBarContrastEnforced - 当需要完全透明的背景时，系统是否应该确保导航栏有足够的对比度。仅在API 29+上支持。
        // systemUiController.setSystemBarsColor(
        //     color = colorScheme.background,
        //     // darkIcons = colorScheme.background.luminance() > 0.5,
        //     // isNavigationBarContrastEnforced = useDarkTheme,
        //     // transformColorForLightContent = { Color.Black.copy(alpha = 0.6F) }
        // )
        // // 该属性保存状态和导航栏图标+内容是否为“黑色”。
        // systemUiController.systemBarsDarkContentEnabled = !isThemeDark

        enableEdgeToEdge.invoke(useDarkTheme, lightScrim, darkScrim)
        onDispose {}
    }

    val systemUiController = rememberSystemUiController()
    CompositionLocalProvider(LocalSystemUiController provides systemUiController) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                        // .statusBarsPadding()
                        // .navigationBarsPadding()
                        // .imePadding()
                    ,
                    color = colorScheme.background,
                    contentColor = colorScheme.onBackground,
                    content = content
                )
            }
        )
    }
}

/**
 * The default light scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

/**
 * The default dark scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)