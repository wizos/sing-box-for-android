package io.mo.viaport.fmt.v2ray

import android.text.TextUtils
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import io.mo.viaport.fmt.SingBoxOptions
import io.mo.viaport.fmt.trojan.TrojanBean
import io.mo.viaport.ktx.NGUtil
import io.mo.viaport.fmt.decodeBase64UrlSafe
import io.mo.viaport.fmt.getStr
import io.mo.viaport.ktx.linkBuilder
import io.mo.viaport.ktx.listByLineOrComma
import io.mo.viaport.ktx.readableMessage
import io.mo.viaport.ktx.toLink
import io.mo.viaport.ktx.urlSafe
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject
import saschpe.kase64.base64UrlDecoded

data class VmessQRCode(
    var v: String = "",
    var ps: String = "",
    var add: String = "",
    var port: String = "",
    var id: String = "",
    var aid: String = "0",
    var scy: String = "",
    var net: String = "",
    var type: String = "",
    var host: String = "",
    var path: String = "",
    var tls: String = "",
    var sni: String = "",
    var alpn: String = "",
    var fp: String = "",
)


fun parseV2Ray(link: String): StandardV2RayBean {
    // Try parse stupid formats first
    if (!link.contains("?")) {
        try {
            return parseV2RayN(link)
        } catch (e: Exception) {
            XLog.i("try v2rayN: " + e.readableMessage)
        }
    }

    // try {
    //     return tryResolveVmess4Kitsunebi(link)
    // } catch (e: Exception) {
    //     XLog.i("try Kitsunebi: " + e.readableMessage)
    // }

    // "std" format

    val bean = VMessBean().apply { if (link.startsWith("vless://")) alterId = -1 }
    val url = link.replace("vmess://", "https://").replace("vless://", "https://").toHttpUrl()

    if (url.password.isNotBlank()) {
        // https://github.com/v2fly/v2fly-github-io/issues/26 (rarely use)
        bean.serverAddress = url.host
        bean.serverPort = url.port
        bean.name = url.fragment

        var protocol = url.username
        bean.type = protocol
        bean.alterId = url.password.substringAfterLast('-').toInt()
        bean.uuid = url.password.substringBeforeLast('-')

        if (protocol.endsWith("+tls")) {
            bean.security = "tls"
            protocol = protocol.substring(0, protocol.length - 4)

            url.queryParameter("tlsServerName")?.let {
                if (it.isNotBlank()) {
                    bean.sni = it
                }
            }
        }

        when (protocol) {
//            "tcp" -> {
//                url.queryParameter("type")?.let { type ->
//                    if (type == "http") {
//                        bean.headerType = "http"
//                        url.queryParameter("host")?.let {
//                            bean.host = it
//                        }
//                    }
//                }
//            }
            "http" -> {
                url.queryParameter("path")?.let {
                    bean.path = it
                }
                url.queryParameter("host")?.let {
                    bean.host = it.split("|").joinToString(",")
                }
            }

            "ws" -> {
                url.queryParameter("path")?.let {
                    bean.path = it
                }
                url.queryParameter("host")?.let {
                    bean.host = it
                }
            }

            "grpc" -> {
                url.queryParameter("serviceName")?.let {
                    bean.path = it
                }
            }

            "httpupgrade" -> {
                url.queryParameter("path")?.let {
                    bean.path = it
                }
                url.queryParameter("host")?.let {
                    bean.host = it
                }
            }
        }
    } else {
        // also vless format
        bean.parseDuckSoft(url)
    }

    return bean
}

// https://github.com/XTLS/Xray-core/issues/91
// https://github.com/XTLS/Xray-core/discussions/716
fun StandardV2RayBean.parseDuckSoft(url: HttpUrl) {
    serverAddress = url.host
    serverPort = url.port
    name = url.fragment

    if (this is TrojanBean) {
        password = url.username
    } else {
        uuid = url.username
    }

    // not ducksoft fmt path
    if (url.pathSegments.size > 1 || url.pathSegments[0].isNotBlank()) {
        path = url.pathSegments.joinToString("/")
    }

    type = url.queryParameter("type") ?: "tcp"
    if (type == "h2") type = "http"

    security = url.queryParameter("security")
    if (security.isNullOrBlank()) {
        security = if (this is TrojanBean) "tls" else "none"
    }

    when (security) {
        "tls", "reality" -> {
            security = "tls"
            url.queryParameter("sni")?.let {
                sni = it
            }
            url.queryParameter("host")?.let {
                if (sni.isNullOrBlank()) sni = it
            }
            url.queryParameter("alpn")?.let {
                alpn = it
            }
            url.queryParameter("cert")?.let {
                certificates = it
            }
            url.queryParameter("pbk")?.let {
                realityPubKey = it
            }
            url.queryParameter("sid")?.let {
                realityShortId = it
            }
        }
    }

    when (type) {
        "tcp" -> {
            // v2rayNG
            if (url.queryParameter("headerType") == "http") {
                url.queryParameter("host")?.let {
                    type = "http"
                    host = it
                }
            }
        }

        "http" -> {
            url.queryParameter("host")?.let {
                host = it
            }
            url.queryParameter("path")?.let {
                path = it
            }
        }

        "ws" -> {
            url.queryParameter("host")?.let {
                host = it
            }
            url.queryParameter("path")?.let {
                path = it
            }
            url.queryParameter("ed")?.let { ed ->
                wsMaxEarlyData = ed.toInt()

                url.queryParameter("eh")?.let {
                    earlyDataHeaderName = it
                }
            }
        }

        "grpc" -> {
            url.queryParameter("serviceName")?.let {
                path = it
            }
        }

        "httpupgrade" -> {
            url.queryParameter("host")?.let {
                host = it
            }
            url.queryParameter("path")?.let {
                path = it
            }
        }
    }

    // maybe from matsuri vmess exoprt
    if (this is VMessBean && !isVLESS()) {
        url.queryParameter("encryption")?.let {
            encryption = it
        }
    }

    url.queryParameter("packetEncoding")?.let {
        when (it) {
            "packet" -> packetEncoding = 1
            "xudp" -> packetEncoding = 2
        }
    }

    url.queryParameter("flow")?.let {
        if (isVLESS()) {
            encryption = it.removeSuffix("-udp443")
        }
    }

    url.queryParameter("fp")?.let {
        utlsFingerprint = it
    }
}

// // 不确定是谁的格式
// private fun tryResolveVmess4Kitsunebi(server: String): VMessBean {
//     // vmess://YXV0bzo1YWY1ZDBlYy02ZWEwLTNjNDMtOTNkYi1jYTMwMDg1MDNiZGJAMTgzLjIzMi41Ni4xNjE6MTIwMg
//     // ?remarks=*%F0%9F%87%AF%F0%9F%87%B5JP%20-355%20TG@moon365free&obfsParam=%7B%22Host%22:%22183.232.56.161%22%7D&path=/v2ray&obfs=websocket&alterId=0
//
//     var result = server.replace("vmess://", "")
//     val indexSplit = result.indexOf("?")
//     if (indexSplit > 0) {
//         result = result.substring(0, indexSplit)
//     }
//     result = NGUtil.decodeBase64Compat(result)
//
//     val arr1 = result.split('@')
//     if (arr1.count() != 2) {
//         XLog.e("错误的文本：" + result)
//         throw IllegalStateException("invalid kitsunebi format")
//     }
//     val arr21 = arr1[0].split(':')
//     val arr22 = arr1[1].split(':')
//     if (arr21.count() != 2) {
//         throw IllegalStateException("invalid kitsunebi format")
//     }
//
//     return VMessBean().apply {
//         serverAddress = arr22[0]
//         serverPort = NGUtil.parseInt(arr22[1])
//         uuid = arr21[1]
//         encryption = arr21[0]
//         if (indexSplit < 0) return@apply
//
//         val url = ("https://localhost/path?" + server.substringAfter("?")).toHttpUrl()
//         url.queryParameter("remarks")?.apply { name = this }
//         url.queryParameter("alterId")?.apply { alterId = this.toInt() }
//         url.queryParameter("path")?.apply { path = this }
//         url.queryParameter("tls")?.apply { security = "tls" }
//         url.queryParameter("allowInsecure")
//             ?.apply { if (this == "1" || this == "true") allowInsecure = true }
//         url.queryParameter("obfs")?.apply {
//             type = this.replace("websocket", "ws").replace("none", "tcp")
//             if (type == "ws") {
//                 url.queryParameter("obfsParam")?.apply {
//                     if (this.startsWith("{")) {
//                         host = JSONObject(this).getStr("Host")
//                     } else if (security == "tls") {
//                         sni = this
//                     }
//                 }
//             }
//         }
//     }
// }

// SagerNet's
// Do not support some format and then throw exception
fun parseV2RayN(link: String): VMessBean {
    val result = link.substringAfter("vmess://").base64UrlDecoded
    if (result.contains("= vmess")) {
        return parseCsvVMess(result)
    }
    val bean = VMessBean()
    val vmessQRCode = Gson().fromJson(result, VmessQRCode::class.java)

    // Although VmessQRCode fields are non null, looks like Gson may still create null fields
    if (vmessQRCode.add.isBlank()
        || vmessQRCode.port.isBlank()
        || vmessQRCode.id.isBlank()
        || vmessQRCode.net.isBlank()
    ) {
        throw Exception("invalid VmessQRCode")
    }

    bean.name = vmessQRCode.ps
    bean.serverAddress = vmessQRCode.add
    bean.serverPort = vmessQRCode.port.toIntOrNull()
    bean.encryption = vmessQRCode.scy
    bean.uuid = vmessQRCode.id
    bean.alterId = vmessQRCode.aid.toIntOrNull()
    bean.type = vmessQRCode.net
    bean.host = vmessQRCode.host
    bean.path = vmessQRCode.path
    val headerType = vmessQRCode.type

    when (bean.type) {
        "tcp" -> {
            if (headerType == "http") {
                bean.type = "http"
            }
        }
    }
    when (vmessQRCode.tls) {
        "tls", "reality" -> {
            bean.security = "tls"
            bean.sni = vmessQRCode.sni
            if (bean.sni.isNullOrBlank()) bean.sni = bean.host
            bean.alpn = vmessQRCode.alpn
            bean.utlsFingerprint = vmessQRCode.fp
        }
    }

    return bean
}

private fun parseCsvVMess(csv: String): VMessBean {

    val args = csv.split(",")

    val bean = VMessBean()

    bean.serverAddress = args[1]
    bean.serverPort = args[2].toInt()
    bean.encryption = args[3]
    bean.uuid = args[4].replace("\"", "")

    args.subList(5, args.size).forEach {

        when {
            it == "over-tls=true" -> bean.security = "tls"
            it.startsWith("tls-host=") -> bean.host = it.substringAfter("=")
            it.startsWith("obfs=") -> bean.type = it.substringAfter("=")
            it.startsWith("obfs-path=") || it.contains("Host:") -> {
                runCatching {
                    bean.path = it.substringAfter("obfs-path=\"").substringBefore("\"obfs")
                }
                runCatching {
                    bean.host = it.substringAfter("Host:").substringBefore("[")
                }

            }

        }

    }

    return bean

}

fun VMessBean.toV2rayN(): String {
    val bean = this
    return "vmess://" + VmessQRCode().apply {
        v = "2"
        ps = bean.name ?: ""
        add = bean.serverAddress ?: ""
        port = bean.serverPort.toString()
        id = bean.uuid ?: ""
        aid = bean.alterId.toString()
        net = bean.type ?: ""
        host = bean.host ?: ""
        path = bean.path ?: ""

        when (net) {
            "http" -> {
                if (!isTLS()) {
                    type = "http"
                    net = "tcp"
                }
            }
        }

        if (isTLS()) {
            tls = "tls"
            if (!bean.realityPubKey.isNullOrBlank()) {
                tls = "reality"
            }
        }

        scy = bean.encryption ?: ""
        sni = bean.sni ?: ""
        alpn = bean.alpn?.replace("\n", ",")  ?: ""
        fp = bean.utlsFingerprint ?: ""
    }.let {
        NGUtil.encode(Gson().toJson(it))
    }
}

// fun StandardV2RayBean.toUriVMessVLESSTrojan(isTrojan: Boolean): String {
//     // VMess
//     if (this is VMessBean && !isVLESS()) {
//         return toV2rayN()
//     }
//
//     // VLESS & Trojan (ducksoft fmt)
//     val builder = linkBuilder()
//         .username((if (this is TrojanBean) password else uuid) ?: "")
//         .host(serverAddress ?: "")
//         .port(serverPort ?: 0)
//         .addQueryParameter("type", type)
//
//     if (isVLESS()) {
//         builder.addQueryParameter("encryption", "none")
//         if (encryption != "auto") builder.addQueryParameter("flow", encryption)
//     }
//
//     when (type) {
//         "tcp" -> {}
//         "ws", "http", "httpupgrade" -> {
//             if (!host.isNullOrBlank()) {
//                 builder.addQueryParameter("host", host)
//             }
//             if (!path.isNullOrBlank()) {
//                 builder.addQueryParameter("path", path)
//             }
//             if (type == "ws") {
//                 wsMaxEarlyData?.takeIf { it > 0 }.let {
//                     builder.addQueryParameter("ed", "$wsMaxEarlyData")
//                     if (!earlyDataHeaderName.isNullOrBlank()) {
//                         builder.addQueryParameter("eh", earlyDataHeaderName)
//                     }
//                 }
//             } else if (type == "http" && !isTLS()) {
//                 builder.setQueryParameter("type", "tcp")
//                 builder.addQueryParameter("headerType", "http")
//             }
//         }
//
//         "grpc" -> {
//             if (!path.isNullOrBlank()) {
//                 builder.setQueryParameter("serviceName", path)
//             }
//         }
//     }
//
//     if (!security.isNullOrBlank() && security != "none") {
//         builder.addQueryParameter("security", security)
//         when (security) {
//             "tls" -> {
//                 if (!sni.isNullOrBlank()) {
//                     builder.addQueryParameter("sni", sni)
//                 }
//                 if (!alpn.isNullOrBlank()) {
//                     builder.addQueryParameter("alpn", alpn?.replace("\n", ","))
//                 }
//                 if (!certificates.isNullOrBlank()) {
//                     builder.addQueryParameter("cert", certificates)
//                 }
//                 if (allowInsecure == true) {
//                     builder.addQueryParameter("allowInsecure", "1")
//                 }
//                 if (!utlsFingerprint.isNullOrBlank()) {
//                     builder.addQueryParameter("fp", utlsFingerprint)
//                 }
//                 if (!realityPubKey.isNullOrBlank() && !realityShortId.isNullOrBlank()) {
//                     builder.setQueryParameter("security", "reality")
//                     builder.addQueryParameter("pbk", realityPubKey)
//                     builder.addQueryParameter("sid", realityShortId)
//                 }
//             }
//         }
//     }
//
//     when (packetEncoding) {
//         1 -> {
//             builder.addQueryParameter("packetEncoding", "packetaddr")
//         }
//
//         2 -> {
//             builder.addQueryParameter("packetEncoding", "xudp")
//         }
//     }
//
//     if (!name.isNullOrBlank()) {
//         builder.encodedFragment(name?.urlSafe())
//     }
//
//     return builder.toLink(if (isTrojan) "trojan" else "vless")
// }


fun buildSingBoxOutboundStreamSettings(bean: StandardV2RayBean): SingBoxOptions.V2RayTransportOptions? {
    when (bean.type) {
        "tcp" -> {
            return null
        }

        "ws" -> {
            return SingBoxOptions.V2RayTransportOptions_WebsocketOptions().apply {
                type = "ws"
                headers = mutableMapOf<String, String>().apply {
                    bean.host?.ifBlank { null }?.let { this["Host"] = it }
                }

                if (bean.path?.contains("?ed=") == true) {
                    path = bean.path?.substringBefore("?ed=")
                    max_early_data = bean.path?.substringAfter("?ed=")?.toIntOrNull() ?: 2048
                    early_data_header_name = "Sec-WebSocket-Protocol"
                } else {
                    path = bean.path?.takeIf { it.isNotBlank() } ?: "/"
                }

                bean.wsMaxEarlyData?.takeIf { it > 0 }.let { max_early_data = it }

                if (!bean.earlyDataHeaderName.isNullOrBlank()) {
                    early_data_header_name = bean.earlyDataHeaderName
                }
            }
        }

        "http" -> {
            return SingBoxOptions.V2RayTransportOptions_HTTPOptions().apply {
                type = "http"
                if (!bean.isTLS()) method = "GET" // v2ray tcp header
                bean.host?.ifBlank { null }?.let { host = it.split(",") }
                path = bean.path?.takeIf { it.isNotBlank() } ?: "/"
            }
        }

        "quic" -> {
            return SingBoxOptions.V2RayTransportOptions().apply {
                type = "quic"
            }
        }

        "grpc" -> {
            return SingBoxOptions.V2RayTransportOptions_GRPCOptions().apply {
                type = "grpc"
                service_name = bean.path
            }
        }

        "httpupgrade" -> {
            return SingBoxOptions.V2RayTransportOptions_HTTPUpgradeOptions().apply {
                type = "httpupgrade"
                host = bean.host
                path = bean.path
            }
        }
    }

//    if (needKeepAliveInterval) {
//        sockopt = StreamSettingsObject.SockoptObject().apply {
//            tcpKeepAliveInterval = keepAliveInterval
//        }
//    }

    return null
}

fun buildSingBoxOutboundTLS(bean: StandardV2RayBean): SingBoxOptions.OutboundTLSOptions? {
    if (bean.security != "tls") return null
    return SingBoxOptions.OutboundTLSOptions().apply {
        enabled = true
        insecure = bean.allowInsecure == true // || DataStore.globalAllowInsecure
        if (!bean.sni.isNullOrBlank()) server_name = bean.sni
        if (!bean.alpn.isNullOrBlank()) alpn = bean.alpn?.listByLineOrComma()
        if (!bean.certificates.isNullOrBlank()) certificate = bean.certificates
        var fp = bean.utlsFingerprint
        if (!bean.realityPubKey.isNullOrBlank()) {
            reality = SingBoxOptions.OutboundRealityOptions().apply {
                enabled = true
                public_key = bean.realityPubKey
                short_id = bean.realityShortId
            }
            if (fp.isNullOrBlank()) fp = "chrome"
        }
        if (!fp.isNullOrBlank()) {
            utls = SingBoxOptions.OutboundUTLSOptions().apply {
                enabled = true
                fingerprint = fp
            }
        }
        if (bean.enableECH == true) {
            ech?.enabled = true
            ech?.pq_signature_schemes_enabled = bean.enablePqSignature
            ech?.dynamic_record_sizing_disabled = bean.disabledDRS
            ech?.config = bean.echConfig?.lines()
        }
    }
}

fun buildSingBoxOutboundStandardV2RayBean(bean: StandardV2RayBean): SingBoxOptions.Outbound {
    when (bean) {
        // is HttpBean -> {
        //     return SingBoxOptions.Outbound_HTTPOptions().apply {
        //         type = "http"
        //         server = bean.serverAddress
        //         server_port = bean.serverPort
        //         username = bean.username
        //         password = bean.password
        //         tls = buildSingBoxOutboundTLS(bean)
        //     }
        // }

        is VMessBean -> {
            if (bean.isVLESS()) return SingBoxOptions.Outbound_VLESSOptions().apply {
                type = "vless"
                server = bean.serverAddress
                server_port = bean.serverPort
                uuid = bean.uuid
                if (!bean.encryption.isNullOrBlank() && bean.encryption != "auto") {
                    flow = bean.encryption
                }
                when (bean.packetEncoding) {
                    0 -> packet_encoding = ""
                    1 -> packet_encoding = "packetaddr"
                    2 -> packet_encoding = "xudp"
                }
                tls = buildSingBoxOutboundTLS(bean)
                transport = buildSingBoxOutboundStreamSettings(bean)
            }
            return SingBoxOptions.Outbound_VMessOptions().apply {
                type = "vmess"
                server = bean.serverAddress
                server_port = bean.serverPort
                uuid = bean.uuid
                alter_id = bean.alterId
                security = bean.encryption?.takeIf { it.isNotBlank() } ?: "auto"
                when (bean.packetEncoding) {
                    0 -> packet_encoding = ""
                    1 -> packet_encoding = "packetaddr"
                    2 -> packet_encoding = "xudp"
                }
                tls = buildSingBoxOutboundTLS(bean)
                transport = buildSingBoxOutboundStreamSettings(bean)
            }
        }

        is TrojanBean -> {
            return SingBoxOptions.Outbound_TrojanOptions().apply {
                type = "trojan"
                server = bean.serverAddress
                server_port = bean.serverPort
                password = bean.password
                tls = buildSingBoxOutboundTLS(bean)
                transport = buildSingBoxOutboundStreamSettings(bean)
            }
        }

        else -> throw IllegalStateException("can't reach")
    }
}
