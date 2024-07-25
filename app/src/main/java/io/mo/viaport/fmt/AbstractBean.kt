package io.mo.viaport.fmt

import io.mo.viaport.helper.GsonHelper
import io.mo.viaport.ktx.unwrapIPV6Host
import io.mo.viaport.ktx.wrapIPV6Host

abstract class AbstractBean {
    var serverAddress: String? = null
    var serverPort: Int? = null

    var name: String? = null

    //
    var customOutboundJson: String? = null
    var customConfigJson: String? = null

    //
    @Transient
    var finalAddress: String? = null

    @Transient
    var finalPort: Int = 0

    open fun displayName(): String {
        return name?.ifBlank { displayAddress() } ?: displayAddress()
    }

    open fun displayAddress(): String {
        return (serverAddress?.wrapIPV6Host() ?: "") + ":" + serverPort
    }

    open fun network(): String? {
        return "tcp,udp"
    }

    open fun canICMPing(): Boolean {
        return true
    }

    open fun canTCPing(): Boolean {
        return true
    }

    open fun canMapping(): Boolean {
        return true
    }

    open fun initDefaultValues() {
        // if (serverAddress.isNullOrBlank()) {
        //     serverAddress = "127.0.0.1"
        // } else
        if (serverAddress!!.startsWith("[") && serverAddress!!.endsWith("]")) {
            serverAddress = serverAddress?.unwrapIPV6Host()
        }
        if (serverPort == null) {
            serverPort = 1080
        }
        if (name == null) name = displayAddress()

        // finalAddress = serverAddress
        // finalPort = serverPort!!

        if (customOutboundJson == null) customOutboundJson = ""
        if (customConfigJson == null) customConfigJson = ""
    }


    override fun toString(): String {
        return javaClass.simpleName + " " + GsonHelper.gson.toJson(this)
    }

    open fun applyFeatureSettings(other: AbstractBean) {
    }
}
