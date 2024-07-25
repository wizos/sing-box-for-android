package io.mo.viaport.persist

import androidx.core.content.edit
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


object LocalSettings {
    private val pref by lazy { MMKV.defaultMMKV() } // MMKV.mmkvWithID("DefaultMMKV", MMKV.MULTI_PROCESS_MODE)
    private val gson by lazy { Gson() }


    private val _logLevelFlow: MutableStateFlow<Int> = MutableStateFlow(logLevel)
    val logLevelFlow: StateFlow<Int> = _logLevelFlow.asStateFlow()
    var logLevel: Int
        get() = pref.getInt("logLevel", 0)
        set(value) {
            pref.edit { putInt("logLevel", value) }
            _logLevelFlow.value = value
        }
}