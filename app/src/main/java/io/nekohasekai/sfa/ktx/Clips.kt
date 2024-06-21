package io.nekohasekai.sfa.ktx

import android.content.ClipData
import io.nekohasekai.sfa.App

var clipboardText: String?
    get() = App.clipboard.primaryClip?.getItemAt(0)?.text?.toString()
    set(plainText) {
        if (plainText != null) {
            App.clipboard.setPrimaryClip(ClipData.newPlainText(null, plainText))
        }
    }