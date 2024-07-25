package io.mo.viaport.ui.home

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elvishew.xlog.XLog
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import io.mo.viaport.helper.exceptionHandler
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.ProfileContent
import io.nekohasekai.libbox.StatusMessage
import io.nekohasekai.libbox.SystemProxyStatus
import io.nekohasekai.sfa.constant.Status
import io.nekohasekai.sfa.database.Profile
import io.nekohasekai.sfa.database.ProfileManager
import io.nekohasekai.sfa.database.Settings
import io.nekohasekai.sfa.database.TypedProfile
import io.mo.viaport.helper.ioScope
import io.nekohasekai.sfa.ktx.dp2px
import io.nekohasekai.sfa.utils.CommandClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

// data class ServerStatus(
//     val memory:Long,
//     val goroutines: Int,
//     val connectionsIn: Int,
//     val connectionsOut: Int,
//     val trafficAvailable: Boolean,
//     val uplink: Long,
//     val Downlink: Long,
//     val UplinkTotal: Long,
//     val DownlinkTotal: Long,
//     )
class HomeViewModel: ViewModel() {
    private val _items = MutableStateFlow<List<Profile>>(mutableListOf())
    val profiles = _items.asStateFlow()

    private var selectedProfileID = -1L
    // var lastSelectedIndex: Int? = null

    private val _lastSelectedIndex = MutableStateFlow<Int?>(null)
    val lastSelectedIndex = _lastSelectedIndex.asStateFlow()

    // private val _serverStatus = MutableStateFlow<ServerStatus?>(null)
    // val serverStatus = _serverStatus.asStateFlow()

    private val _memory = MutableStateFlow<String?>(null)
    val memory = _memory.asStateFlow()

    private val _goroutines = MutableStateFlow<String?>(null)
    val goroutines = _goroutines.asStateFlow()

    private val _connectionsIn = MutableStateFlow<String?>(null)
    val connectionsIn = _connectionsIn.asStateFlow()

    private val _connectionsOut = MutableStateFlow<String?>(null)
    val connectionsOut = _connectionsOut.asStateFlow()

    private val _trafficAvailable = MutableStateFlow<Boolean>(false)
    val trafficAvailable = _trafficAvailable.asStateFlow()

    private val _uplink = MutableStateFlow<String?>(null)
    val uplink = _uplink.asStateFlow()

    private val _downlink = MutableStateFlow<String?>(null)
    val downlink = _downlink.asStateFlow()

    private val _uplinkTotal = MutableStateFlow<String?>(null)
    val uplinkTotal = _uplinkTotal.asStateFlow()

    private val _downlinkTotal = MutableStateFlow<String?>(null)
    val downlinkTotal = _downlinkTotal.asStateFlow()


    private val _initialStartup = MutableStateFlow<Boolean>(false)
    val initialStartup = _initialStartup.asStateFlow()

    private val _serviceStatus = MutableStateFlow(Status.Stopped)
    val serviceStatus = _serviceStatus.asStateFlow()


    private val statusClient =
        CommandClient(viewModelScope, CommandClient.ConnectionType.Status, StatusClient())
    // private val clashModeClient = CommandClient(viewModelScope, CommandClient.ConnectionType.ClashMode, ClashModeClient())


    private val _logList = MutableStateFlow(emptyList<String>())
    val logList = _logList.asStateFlow()

    var logCallback: ((Boolean) -> Unit)? = null

    private val _paused = MutableStateFlow(false)
    val paused = _paused.asStateFlow()

    fun receiveLog(pause: Boolean){
        _paused.value = pause
    }
    fun onServiceWriteLog(message: String?) = viewModelScope.launch {
        if (paused.value) return@launch
        message?.let { msg ->
            launch(Dispatchers.IO + exceptionHandler) {
                _logList.update { it + msg }
            }
        }
    }
    fun onServiceResetLogs(messages: MutableList<String>) = viewModelScope.launch{
        if (paused.value) return@launch
        launch(Dispatchers.IO + exceptionHandler) {
            _logList.value = emptyList()
            _logList.update { it + messages }

            // _logList.value.plus(messages)

            // _logList.value.addAll(messages)
            // messages.forEach{
            //     _logList.value.addLast(ColorUtils.ansiEscapeToSpannable(context, it))
            // }
        }
        // _logList.value.clear()
        // _logList.value.addAll(messages)
        // if (!paused) logCallback?.invoke(true)
    }


    fun onServiceStatusChanged(status: Status) {
        _serviceStatus.value = status
    }

    fun reload() = viewModelScope.launch(Dispatchers.IO + exceptionHandler){
        ProfileManager.list().let { profiles ->
            _items.value = profiles
            if (profiles.isNotEmpty()){
                selectedProfileID = Settings.selectedProfile

                profiles
                    .indexOfFirst { it.id == selectedProfileID }
                    .let { profileIndex ->
                        if (profileIndex == -1) {
                            _lastSelectedIndex.value = 0
                            profiles.first().id.let { profileId ->
                                selectedProfileID = profileId
                                Settings.selectedProfile = profileId
                            }
                        }else{
                            _lastSelectedIndex.value = profileIndex
                        }
                    }
            }
        }
    }

    fun importProfiles(contentResolver: ContentResolver, uri: Uri){

    }

    suspend fun selectProfile(index:Int, profile: Profile, onRestart:suspend ()->Unit, onThrow: (Throwable)->Unit){
        selectedProfileID = profile.id
        Settings.selectedProfile = profile.id

        _lastSelectedIndex.value = index

        val started = serviceStatus.value == Status.Started
        if (!started) {
            return
        }
        val restart = Settings.rebuildServiceMode()
        if (restart) {
            onRestart.invoke()
            return
        }
        runCatching {
            Libbox.newStandaloneCommandClient().serviceReload()
        }.onFailure {
            onThrow.invoke(it)
        }
    }
    fun shareProfileToQR(profile: Profile): Bitmap{
        val link = Libbox.generateRemoteProfileImportLink(
            profile.name,
            profile.typed.remoteURL
        )
        val imageSize = dp2px(256)

        val image = QRCodeWriter().encode(link, BarcodeFormat.QR_CODE, imageSize, imageSize, null)
        val imageWidth = image.width
        val imageHeight = image.height
        val imageArray = IntArray(imageWidth * imageHeight)
        for (y in 0 until imageHeight) {
            val offset = y * imageWidth
            for (x in 0 until imageWidth) {
                // imageArray[offset + x] = if (image.get(x, y)) color else Color.TRANSPARENT
                imageArray[offset + x] = Color.TRANSPARENT
            }
        }
        return Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
    }
    fun shareProfileToUri(context: Context, profile: Profile): Uri {
        val content = ProfileContent()
        content.name = profile.name
        when (profile.typed.type) {
            TypedProfile.Type.Local -> {
                content.type = Libbox.ProfileTypeLocal
            }

            TypedProfile.Type.Remote -> {
                content.type = Libbox.ProfileTypeRemote
            }
        }
        content.config = File(profile.typed.path).readText()
        content.remotePath = profile.typed.remoteURL
        content.autoUpdate = profile.typed.autoUpdate
        content.autoUpdateInterval = profile.typed.autoUpdateInterval
        content.lastUpdated = profile.typed.lastUpdated.time

        val configDirectory = File(context.cacheDir, "share").also { it.mkdirs() }
        val profileFile = File(configDirectory, "${profile.name}.bpf")
        profileFile.writeBytes(content.encode())
        return FileProvider.getUriForFile(context, "${context.packageName}.cache", profileFile)
    }

    // private val _errorMsg = MutableStateFlow<SystemProxyStatus?>(null)
    // val errorMsg = _errorMsg.asStateFlow()

    private val _systemProxyStatus = MutableStateFlow<SystemProxyStatus?>(null)
    val systemProxyStatus = _systemProxyStatus.asStateFlow()

    @Throws
    fun changeSystemProxyStatus(isChecked: Boolean){
        Settings.systemProxyEnabled = isChecked
        Libbox.newStandaloneCommandClient().setSystemProxyEnabled(isChecked)
    }

    private fun reloadSystemProxyStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val systemProxyStatus = Libbox.newStandaloneCommandClient().systemProxyStatus
            XLog.e("重载系统代理状态：" + systemProxyStatus.enabled + ". " + systemProxyStatus.available )
            _systemProxyStatus.value = systemProxyStatus
        }
    }

    inner class StatusClient : CommandClient.Handler {
        override fun onConnected() {
            viewModelScope.launch {
                _memory.value = null
                _goroutines.value = null
            }
            // lifecycleScope.launch(Dispatchers.Main) {
            //     binding.memoryText.text = getString(R.string.loading)
            //     binding.goroutinesText.text = getString(R.string.loading)
            // }
        }

        override fun onDisconnected() {
            viewModelScope.launch {
                _memory.value = null
                _goroutines.value = null
            }
            // val binding = binding ?: return
            // lifecycleScope.launch(Dispatchers.Main) {
            //     binding.memoryText.text = getString(R.string.loading)
            //     binding.goroutinesText.text = getString(R.string.loading)
            // }
        }

        override fun updateStatus(status: StatusMessage) {
            viewModelScope.launch {
                _memory.value = Libbox.formatBytes(status.memory)
                _goroutines.value = status.goroutines.toString()
                _trafficAvailable.value = status.trafficAvailable
                if (status.trafficAvailable){
                    _connectionsIn.value = status.connectionsIn.toString()
                    _connectionsOut.value = status.connectionsOut.toString()
                    _uplink.value = Libbox.formatBytes(status.uplink) + "/s"
                    _downlink.value = Libbox.formatBytes(status.downlink) + "/s"
                    _uplinkTotal.value = Libbox.formatBytes(status.uplinkTotal)
                    _downlinkTotal.value = Libbox.formatBytes(status.downlinkTotal)
                }
            }
        }

    }

    init {
        ProfileManager.registerCallback(this::reload)

        viewModelScope.launch {
            reload()
        }

        viewModelScope.launch {
            serviceStatus.collect{
                XLog.i("收集服务状态：" + it.name)
                when (it) {
                    Status.Started -> {
                        XLog.i("开始链接")
                        _initialStartup.value = true
                        _logList.value = emptyList()
                        statusClient.connect()
                        // clashModeClient.connect()
                        reloadSystemProxyStatus()
                    }
                    else -> {}
                }
            }
        }
    }


    inner class ClashModeClient : CommandClient.Handler {
        override fun initializeClashMode(modeList: List<String>, currentMode: String) {
            if (modeList.size > 1) {
                viewModelScope.launch(Dispatchers.Main) {
                    // binding.clashModeCard.isVisible = true
                    // binding.clashModeList.adapter = ClashModeAdapter(modeList, currentMode)
                    // binding.clashModeList.layoutManager =
                    //     GridLayoutManager(
                    //         requireContext(),
                    //         if (modeList.size < 3) modeList.size else 3
                    //     )
                }
            } else {
                // lifecycleScope.launch(Dispatchers.Main) {
                //     binding.clashModeCard.isVisible = false
                // }
            }
        }

        // @SuppressLint("NotifyDataSetChanged")
        // override fun updateClashMode(newMode: String) {
        //     val binding = binding ?: return
        //     val adapter = binding.clashModeList.adapter as? OverviewFragment.ClashModeAdapter ?: return
        //     adapter.selected = newMode
        //     lifecycleScope.launch(Dispatchers.Main) {
        //         adapter.notifyDataSetChanged()
        //     }
        // }
    }
}