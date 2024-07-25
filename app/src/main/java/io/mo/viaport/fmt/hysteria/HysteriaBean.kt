package io.mo.viaport.fmt.hysteria

import android.os.Parcelable
import com.esotericsoftware.kryo.io.ByteBufferInput
import com.esotericsoftware.kryo.io.ByteBufferOutput
import io.mo.viaport.fmt.AbstractBean
import io.mo.viaport.ktx.wrapIPV6Host

class HysteriaBean : AbstractBean() {
    var protocolVersion: Int? = null

    // Use serverPorts instead of serverPort
    var serverPorts: String? = null

    // HY1 & 2
    var authPayload: String? = null
    var obfuscation: String? = null
    var sni: String? = null
    var caText: String? = null
    var uploadMbps: Int? = null
    var downloadMbps: Int? = null
    var allowInsecure: Boolean? = null
    var streamReceiveWindow: Int? = null
    var connectionReceiveWindow: Int? = null
    var disableMtuDiscovery: Boolean? = null
    var hopInterval: Int? = null

    // HY1
    var alpn: String? = null

    var authPayloadType: Int? = null

    var protocol: Int? = null

    override fun canMapping(): Boolean {
        return protocol != PROTOCOL_FAKETCP
    }

    override fun initDefaultValues() {
        super.initDefaultValues()
        if (protocolVersion == null) protocolVersion = 2

        if (authPayloadType == null) authPayloadType = TYPE_NONE
        if (authPayload == null) authPayload = ""
        if (protocol == null) protocol = PROTOCOL_UDP
        if (obfuscation == null) obfuscation = ""
        if (sni == null) sni = ""
        if (alpn == null) alpn = ""
        if (caText == null) caText = ""
        if (allowInsecure == null) allowInsecure = false

        if (protocolVersion == 1) {
            if (uploadMbps == null) uploadMbps = 10
            if (downloadMbps == null) downloadMbps = 50
        } else {
            if (uploadMbps == null) uploadMbps = 0
            if (downloadMbps == null) downloadMbps = 0
        }

        if (streamReceiveWindow == null) streamReceiveWindow = 0
        if (connectionReceiveWindow == null) connectionReceiveWindow = 0
        if (disableMtuDiscovery == null) disableMtuDiscovery = false
        if (hopInterval == null) hopInterval = 10
        if (serverPorts == null) serverPorts = "443"
    }

    override fun displayAddress(): String {
        return serverAddress?.wrapIPV6Host() + ":" + serverPorts
    }

    override fun canTCPing(): Boolean {
        return false
    }

    companion object {
        const val TYPE_NONE: Int = 0
        const val TYPE_STRING: Int = 1
        const val TYPE_BASE64: Int = 2
        const val PROTOCOL_UDP: Int = 0
        const val PROTOCOL_FAKETCP: Int = 1
        const val PROTOCOL_WECHAT_VIDEO: Int = 2
    }
}
