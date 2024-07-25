package io.mo.viaport.fmt.v2ray

import com.esotericsoftware.kryo.io.ByteBufferInput
import com.esotericsoftware.kryo.io.ByteBufferOutput
import io.mo.viaport.fmt.AbstractBean
import io.mo.viaport.fmt.trojan.TrojanBean
import java.util.Locale

abstract class StandardV2RayBean : AbstractBean() {
    var uuid: String? = null
    @JvmField
    var encryption: String? = null // or VLESS flow

    //////// End of VMess & VLESS ////////
    // "V2Ray Transport" tcp/http/ws/quic/grpc/httpUpgrade
    var type: String? = null

    var host: String? = null

    var path: String? = null

    // --------------------------------------- tls?
    var security: String? = null

    var sni: String? = null

    var alpn: String? = null

    var utlsFingerprint: String? = null

    var allowInsecure: Boolean? = null


    // --------------------------------------- reality
    var realityPubKey: String? = null

    var realityShortId: String? = null


    // --------------------------------------- //
    var wsMaxEarlyData: Int? = null
    var earlyDataHeaderName: String? = null

    var certificates: String? = null

    // --------------------------------------- ech
    var enableECH: Boolean? = null

    var enablePqSignature: Boolean? = null

    var disabledDRS: Boolean? = null

    var echConfig: String? = null

    // --------------------------------------- //
    var packetEncoding: Int? = null // 1:packet 2:xudp

    override fun initDefaultValues() {
        super.initDefaultValues()

        if (uuid == null) uuid = ""

        if (type.isNullOrBlank()) type = "tcp" else if ("h2" == type) type = "http"

        type?.lowercase(Locale.getDefault())?.let { type =  it }

        if (host.isNullOrBlank()) host = ""
        if (path.isNullOrBlank()) path = ""

        if (security.isNullOrBlank()) {
            security = if (this is TrojanBean || this.isVLESS()) {
                "tls"
            } else {
                "none"
            }
        }
        if (sni.isNullOrBlank()) sni = ""
        if (alpn.isNullOrBlank()) alpn = ""

        if (certificates.isNullOrBlank()) certificates = ""
        if (earlyDataHeaderName.isNullOrBlank()) earlyDataHeaderName = ""
        if (utlsFingerprint.isNullOrBlank()) utlsFingerprint = ""

        if (wsMaxEarlyData == null) wsMaxEarlyData = 0
        if (allowInsecure == null) allowInsecure = false
        if (packetEncoding == null) packetEncoding = 0

        if (realityPubKey == null) realityPubKey = ""
        if (realityShortId == null) realityShortId = ""

        if (enableECH == null) enableECH = false
        if (echConfig.isNullOrBlank()) echConfig = ""
        if (enablePqSignature == null) enablePqSignature = false
        if (disabledDRS == null) disabledDRS = false
    }

    // val isVLESS: Boolean
    //     get() {
    //         if (this is VMessBean) {
    //             val aid = this.alterId
    //             return aid != null && aid == -1
    //         }
    //         return false
    //     }
}

// val StandardV2RayBean.isVLESS: Boolean
//     get() {
//         if (this is VMessBean) {
//             val aid = this.alterId
//             return aid != null && aid == -1
//         }
//         return false
//     }

fun StandardV2RayBean.isVLESS(): Boolean {
    if (this is VMessBean) {
        val aid = this.alterId
        return aid != null && aid == -1
    }
    return false
}
fun StandardV2RayBean.isTLS(): Boolean {
    return security == "tls"
}
fun StandardV2RayBean.setTLS(boolean: Boolean) {
    security = if (boolean) "tls" else ""
}