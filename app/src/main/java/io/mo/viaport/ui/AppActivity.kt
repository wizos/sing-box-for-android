package io.mo.viaport.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.elvishew.xlog.XLog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.mo.viaport.helper.exceptionHandler
import io.mo.viaport.ktx.exclusiveFilesDir
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.ProfileContent
import io.nekohasekai.sfa.App
import io.nekohasekai.sfa.BuildConfig
import io.nekohasekai.sfa.R
import io.nekohasekai.sfa.bg.BoxService
import io.nekohasekai.sfa.bg.ServiceConnection
import io.nekohasekai.sfa.bg.ServiceNotification
import io.nekohasekai.sfa.constant.Alert
import io.nekohasekai.sfa.constant.ServiceMode
import io.nekohasekai.sfa.constant.Status
import io.nekohasekai.sfa.database.Profile
import io.nekohasekai.sfa.database.ProfileManager
import io.nekohasekai.sfa.database.Settings
import io.nekohasekai.sfa.database.TypedProfile
import io.nekohasekai.sfa.ktx.errorDialogBuilder
import io.nekohasekai.sfa.ktx.hasPermission
import io.mo.viaport.ui.element.BottomNavItem
import io.mo.viaport.ui.element.BottomNavigationBar
import io.mo.viaport.ui.element.TopBar
import io.mo.viaport.ui.element.ViewHost
import io.mo.viaport.ui.element.ViewHostState
import io.mo.viaport.ui.home.HomeViewModel
import io.mo.viaport.ui.home.homeId
import io.mo.viaport.ui.home.homeScreen
import io.mo.viaport.ui.log.logsId
import io.mo.viaport.ui.log.logsScreen
import io.mo.viaport.ui.setting.settingsId
import io.mo.viaport.ui.setting.settingsScreen
import io.nekohasekai.sfa.ui.profile.NewProfileActivity
import io.nekohasekai.sfa.ui.profile.QRScanActivity
import io.mo.viaport.ui.theme.ScreenTheme
import io.nekohasekai.sfa.utils.MIUIUtils
import io.nekohasekai.sfa.vendor.Vendor
import io.mo.viaport.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import java.util.LinkedList
import kotlin.coroutines.resume

class AppActivity: BasalActivity(), ServiceConnection.Callback {
    private val appViewModel by viewModels<AppViewModel>()
    private val homeViewModel by viewModels<HomeViewModel>()

    // private val snackBarHostState = SnackbarHostState()
    // lateinit var prompter: Prompter

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Providers{
                ScreenTheme (
                    enableEdgeToEdge = {isThemeDark, lightScrim, darkScrim ->
                        enableEdgeToEdge(
                            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT,) { isThemeDark },
                            navigationBarStyle = SystemBarStyle.auto(lightScrim, darkScrim) { isThemeDark },
                        )
                    }
                ) {
                    val navController = LocalNavController.current

                    val items = listOf(
                        BottomNavItem(stringResource(id = R.string.home), Icons.Default.Home, Routor.homeId()),
                        BottomNavItem(stringResource(id = R.string.log), Icons.Default.BugReport, Routor.logsId()),
                        BottomNavItem(stringResource(id = R.string.setting), Icons.Default.Navigation, Routor.settingsId())
                    )

                    Scaffold(
                        topBar = {
                            TopBar(navController, items)
                        },
                        snackbarHost = {
                            SnackbarHost(modifier = Modifier.wrapContentWidth(), hostState = LocalSnackbarHostState.current)
                        },
                        floatingActionButton = {
                            val profiles by homeViewModel.profiles.collectAsStateWithLifecycle()
                            if (profiles.isNotEmpty()){
                                val serviceStatus by homeViewModel.serviceStatus.collectAsStateWithLifecycle()
                                // var lastServiceStatus by remember { mutableStateOf(Status.Stopped) }
                                FloatingActionButton(
                                    onClick = {
                                        XLog.e("点击后开始启动服务：$serviceStatus")
                                        if(serviceStatus == Status.Started){
                                            BoxService.stop()
                                        }else if(serviceStatus == Status.Stopped){
                                            startService()
                                        }
                                    }
                                ){
                                    if ( serviceStatus == Status.Started || serviceStatus == Status.Stopped){
                                        Icon(imageVector = if (serviceStatus == Status.Started) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = stringResource(id = R.string.action_start))
                                    }else{
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        },
                        floatingActionButtonPosition = FabPosition.Center,
                        bottomBar = { BottomNavigationBar(navController, items) }
                    ) {
                        NavHost(navController = navController, startDestination = Routor.homeId(), modifier = Modifier.padding(it)) {
                            homeScreen(homeViewModel)
                            logsScreen(homeViewModel)
                            settingsScreen()
                        }
                    }
                    ViewHost(LocalViewHostState.current)
                }
            }
        }
        reconnect()
        startIntegration()
        openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()){ openDocumentCallback.invoke(it) }
        getContentLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { getContentCallback.invoke(it) }
    }

    @Composable
    fun Providers(content: @Composable () -> Unit) {
        XLog.d("Providers 准备")

        val iconButtonPadding = (LocalViewConfiguration.current.minimumTouchTargetSize.width - DefaultIconSize) / 2

        val settingIcons = toVectorPainter(LocalContentColor.current, Icons.Default.Settings)

        val navController = rememberNavController()

        // CompositionLocal 类位于 androidx.compose.runtime 包下，总的来说是用于在 composition 树中共享变量的值。在 Compose 构建的 composition 树中，如果需要将顶层的 Composable 函数中的某个变量传递到最底层的 Composable 函数，通常最简单有效的方法就是：1）定义一个全局变量，通过全局变量传值；2）中间层的 Composable 函数添加一个形参，层层传递。
        // 但是这两种方式都不太优雅，尤其是嵌套过深，或者数据比较敏感，不想暴露给中间层的函数时，这种情况下，就可以使用 CompositionLocal 来隐式的将数据传递给所需的 composition 树节点。
        // CompositionLocal 在本质上就是分层的，它可以将数据限定在以某个 Composable 作为根结点的子树中，而且数据默认会向下传递，当然，当前子树中的某个 Composable 函数可以对该 CompositionLocal 的数据进行覆盖，从而使得新值会在这个 Composable 层级中继续向下传递。
        CompositionLocalProvider(
            LocalApplication provides App.instance,
            LocalNavController provides navController,
            LocalLifecycleOwner provides this,
            LocalActivity provides this,
            LocalPrompter provides Prompter(App.instance),
            LocalAppViewModel provides appViewModel,
            LocalSnackbarHostState provides SnackbarHostState(),
            LocalViewHostState provides ViewHostState(),
            LocalIconButtonPadding provides iconButtonPadding,
            LocalSettingIcons provides settingIcons,
            content = content
        )
    }

    private lateinit var openDocumentLauncher: ActivityResultLauncher<Array<String>>
    private var openDocumentCallback: (Uri?) -> Unit = {}

    private lateinit var getContentLauncher: ActivityResultLauncher<String>
    private var getContentCallback: (Uri?) -> Unit = {}
    val importFromFile = registerForActivityResult(ActivityResultContracts.GetContent()){ onNewIntent(Intent(Intent.ACTION_VIEW, it)) }
    val scanQrCode = registerForActivityResult(QRScanActivity.Contract()){ onNewIntent(it ?: return@registerForActivityResult) }

    suspend fun requestOpenDocument(mimeTypes: Array<String>): Uri? {
        return suspendCancellableCoroutine { continuation ->
            openDocumentCallback = { continuation.resume(it) }
            openDocumentLauncher.launch(mimeTypes)
        }
    }
    suspend fun requestGetContent(mimeType: String): Uri? {
        return suspendCancellableCoroutine { continuation ->
            getContentCallback = { continuation.resume(it) }
            getContentLauncher.launch(mimeType)
        }
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri = intent.data ?: return
        if (uri.scheme == BuildConfig.applicationName && uri.host == "import-remote-profile") {
            val profile = try {
                Libbox.parseRemoteProfileImportLink(uri.toString())
            } catch (e: Exception) {
                errorDialogBuilder(e).show()
                return
            }
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.import_remote_profile)
                .setMessage(
                    getString(
                        R.string.import_remote_profile_message,
                        profile.name,
                        profile.host
                    )
                )
                .setPositiveButton(R.string.ok) { _, _ ->
                    startActivity(Intent(this, NewProfileActivity::class.java).apply {
                        putExtra("importName", profile.name)
                        putExtra("importURL", profile.url)
                    })
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } else if (intent.action == Intent.ACTION_VIEW) {
            try {
                val data = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return
                val content = Libbox.decodeProfileContent(data)
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.import_profile)
                    .setMessage(
                        getString(
                            R.string.import_profile_message,
                            content.name
                        )
                    )
                    .setPositiveButton(R.string.ok) { _, _ ->
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                runCatching {
                                    importProfile(content)
                                }.onFailure {
                                    withContext(Dispatchers.Main) {
                                        errorDialogBuilder(it).show()
                                    }
                                }
                            }
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            } catch (e: Exception) {
                errorDialogBuilder(e).show()
            }
        }
    }

    private suspend fun importProfile(content: ProfileContent) {
        val typedProfile = TypedProfile()
        val profile = Profile(name = content.name, typed = typedProfile)
        profile.userOrder = ProfileManager.nextOrder()
        when (content.type) {
            Libbox.ProfileTypeLocal -> {
                typedProfile.type = TypedProfile.Type.Local
            }

            Libbox.ProfileTypeiCloud -> {
                errorDialogBuilder(R.string.icloud_profile_unsupported).show()
                return
            }

            Libbox.ProfileTypeRemote -> {
                typedProfile.type = TypedProfile.Type.Remote
                typedProfile.remoteURL = content.remotePath
                typedProfile.autoUpdate = content.autoUpdate
                typedProfile.autoUpdateInterval = content.autoUpdateInterval
                typedProfile.lastUpdated = Date(content.lastUpdated)
            }
        }
        val configDirectory = File(filesDir, "configs").also { it.mkdirs() }
        val configFile = File(configDirectory, "${profile.userOrder}.json")
        configFile.writeText(content.config)
        typedProfile.path = configFile.path
        ProfileManager.create(profile)
    }

    fun reconnect() {
        connection.reconnect()
    }

    private fun startIntegration() {
        if (Vendor.checkUpdateAvailable()) {
            lifecycleScope.launch(Dispatchers.IO + exceptionHandler) {
                if (Settings.checkUpdateEnabled) {
                    Vendor.checkUpdate(this@AppActivity, false)
                }
            }
        }
    }

    @SuppressLint("NewApi")
    fun startService() {
        if (!ServiceNotification.checkPermission()) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            if (Settings.rebuildServiceMode()) {
                reconnect()
            }
            if (Settings.serviceMode == ServiceMode.VPN) {
                if (prepare()) {
                    return@launch
                }
            }
            val intent = Intent(App.instance, Settings.serviceClass())
            withContext(Dispatchers.Main) {
                ContextCompat.startForegroundService(App.instance, intent)
            }
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            startService()
        } else {
            onServiceAlert(Alert.RequestNotificationPermission, null)
        }
    }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestBackgroundLocationPermission()
                } else {
                    startService()
                }
            }
        }

    private val backgroundLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                startService()
            }
        }

    private val prepareLauncher = registerForActivityResult(PrepareService()) {
        if (it) {
            startService()
        } else {
            onServiceAlert(Alert.RequestVPNPermission, null)
        }
    }

    private class PrepareService : ActivityResultContract<Intent, Boolean>() {
        override fun createIntent(context: Context, input: Intent): Intent {
            return input
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == RESULT_OK
        }
    }

    private suspend fun prepare() = withContext(Dispatchers.Main) {
        try {
            val intent = VpnService.prepare(this@AppActivity)
            if (intent != null) {
                prepareLauncher.launch(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            onServiceAlert(Alert.RequestVPNPermission, e.message)
            false
        }
    }

    override fun onServiceStatusChanged(status: Status) = homeViewModel.onServiceStatusChanged(status)

    override fun onServiceAlert(type: Alert, message: String?) {
        when (type) {
            Alert.RequestLocationPermission -> {
                return requestLocationPermission()
            }

            else -> {}
        }

        val builder = MaterialAlertDialogBuilder(this)
        builder.setPositiveButton(R.string.ok, null)
        when (type) {
            Alert.RequestVPNPermission -> {
                builder.setMessage(getString(R.string.service_error_missing_permission))
            }

            Alert.RequestNotificationPermission -> {
                builder.setMessage(getString(R.string.service_error_missing_notification_permission))
            }

            Alert.EmptyConfiguration -> {
                builder.setMessage(getString(R.string.service_error_empty_configuration))
            }

            Alert.StartCommandServer -> {
                builder.setTitle(getString(R.string.service_error_title_start_command_server))
                builder.setMessage(message)
            }

            Alert.CreateService -> {
                builder.setTitle(getString(R.string.service_error_title_create_service))
                builder.setMessage(message)
            }

            Alert.StartService -> {
                builder.setTitle(getString(R.string.service_error_title_start_service))
                builder.setMessage(message)

            }

            else -> {}
        }
        builder.show()
    }

    private fun requestLocationPermission() {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestFineLocationPermission()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestBackgroundLocationPermission()
        }
    }

    private fun requestFineLocationPermission() {
        val message = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(
                getString(R.string.location_permission_description),
                Html.FROM_HTML_MODE_LEGACY
            )
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(getString(R.string.location_permission_description))
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.location_permission_title)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { _, _ ->
                requestFineLocationPermission0()
            }
            .setNegativeButton(R.string.no_thanks, null)
            .setCancelable(false)
            .show()
    }

    private fun requestFineLocationPermission0() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            openPermissionSettings()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.location_permission_title)
            .setMessage(
                Html.fromHtml(
                    getString(R.string.location_permission_background_description),
                    Html.FROM_HTML_MODE_LEGACY
                )
            )
            .setPositiveButton(R.string.ok) { _, _ ->
                backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            .setNegativeButton(R.string.no_thanks, null)
            .setCancelable(false)
            .show()
    }

    private fun openPermissionSettings() {
        if (MIUIUtils.isMIUI) {
            try {
                MIUIUtils.openPermissionSettings(this)
                return
            } catch (ignored: Exception) {
            }
        }

        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (e: Exception) {
            errorDialogBuilder(e).show()
        }
    }

    private val connection = ServiceConnection(this, this)


    val logList = LinkedList<String>()
    var logCallback: ((Boolean) -> Unit)? = null

    private var paused = false
    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onResume() {
        super.onResume()
        paused = false
        logCallback?.invoke(true)
    }

    override fun onServiceWriteLog(message: String?) {
        homeViewModel.onServiceWriteLog(message)
        // if (paused) {
        //     if (logList.size > 300) {
        //         logList.removeFirst()
        //     }
        // }
        // logList.addLast(message)
        // if (!paused) {
        //     logCallback?.invoke(false)
        // }
    }

    override fun onServiceResetLogs(messages: MutableList<String>) {
        homeViewModel.onServiceResetLogs(messages)
        // logList.clear()
        // logList.addAll(messages)
        // if (!paused) logCallback?.invoke(true)
    }

    override fun onDestroy() {
        connection.disconnect()
        super.onDestroy()
    }

}