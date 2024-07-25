package io.mo.viaport.fmt.shadowsocks

import com.elvishew.xlog.XLog
import io.mo.viaport.fmt.SingBoxOptions
import io.mo.viaport.ktx.Util
import io.mo.viaport.fmt.decodeBase64UrlSafe
import io.mo.viaport.fmt.getIntNya
import io.mo.viaport.fmt.getStr
import io.mo.viaport.ktx.linkBuilder
import io.mo.viaport.ktx.toLink
import io.mo.viaport.ktx.unUrlSafe
import io.mo.viaport.ktx.urlSafe
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject
import saschpe.kase64.base64UrlDecoded

fun ShadowsocksBean.fixPluginName() {
    if (plugin?.startsWith("simple-obfs") == true) {
        plugin = plugin?.replaceFirst("simple-obfs", "obfs-local")
    }
}

fun parseShadowsocks(url: String): ShadowsocksBean {
    if (url.substringBefore("#").contains("@")) {
        var link = url.replace("ss://", "https://").toHttpUrlOrNull() ?: error("invalid ss-android link $url")

        if (link.username.isBlank()) { // fix justmysocks's shit link
            link = (
                    ("https://" + url.substringAfter("ss://").substringBefore("#").decodeBase64UrlSafe())
                        .toHttpUrlOrNull() ?: error("invalid jms link $url")
                    ).newBuilder().fragment(url.substringAfter("#")).build()
        }

        // ss-android style
        if (link.password.isNotBlank()) {
            return ShadowsocksBean().apply {
                serverAddress = link.host
                serverPort = link.port
                method = link.username
                password = link.password
                plugin = link.queryParameter("plugin") ?: ""
                name = link.fragment
                fixPluginName()
            }
        }

        val methodAndPswd = link.username.decodeBase64UrlSafe()

        return ShadowsocksBean().apply {
            serverAddress = link.host
            serverPort = link.port
            method = methodAndPswd.substringBefore(":")
            password = methodAndPswd.substringAfter(":")
            plugin = link.queryParameter("plugin") ?: ""
            name = link.fragment ?: displayAddress()
            fixPluginName()
        }
    } else {
        // v2rayN style
        var v2Url = url

        if (v2Url.contains("#")) v2Url = v2Url.substringBefore("#")

        val link = ("https://" + v2Url.substringAfter("ss://").decodeBase64UrlSafe()).toHttpUrlOrNull() ?: error("invalid v2rayN link $url")

        return ShadowsocksBean().apply {
            serverAddress = link.host
            serverPort = link.port
            method = link.username
            password = link.password
            plugin = ""
            val remarks = url.substringAfter("#").unUrlSafe()
            if (remarks.isNotBlank()) name = remarks
        }
    }
}

fun parseShadowsocks2(url: String): ShadowsocksBean {
    val mainUrl = url.substringAfter("ss://") //.substringBefore("#")
    // val remark = url.substringAfter("#", "").unUrlSafe()
    if (mainUrl.contains("@")) {
        val httpUrl = ("https://$mainUrl").toHttpUrlOrNull() ?: error("invalid ss-android link $url")
        // // fix justmysocks's shit link
        // if (httpUrl.username.isBlank()) {
        //     httpUrl = ("https://" + mainUrl.decodeBase64UrlSafe()).toHttpUrlOrNull() ?: error("invalid jms link $url")
        // }

        // ss-android style
        if (httpUrl.password.isNotBlank()) {
            return ShadowsocksBean().apply {
                serverAddress = httpUrl.host
                serverPort = httpUrl.port
                method = httpUrl.username
                password = httpUrl.password
                plugin = httpUrl.queryParameter("plugin") ?: ""
                name = httpUrl.fragment
                fixPluginName()
            }
        }

        // XLog.e("需要反编码的：$mainUrl, " + httpUrl.username)
        val methodAndPswd = httpUrl.username.base64UrlDecoded
        return ShadowsocksBean().apply {
            serverAddress = httpUrl.host
            serverPort = httpUrl.port
            method = methodAndPswd.substringBefore(":")
            password = methodAndPswd.substringAfter(":")
            plugin = httpUrl.queryParameter("plugin") ?: ""
            name = httpUrl.fragment ?: displayAddress()
            fixPluginName()
        }
    } else {
        // v2rayN style
        val httpUrl = ("https://" + mainUrl.base64UrlDecoded).toHttpUrlOrNull() ?: error("invalid v2rayN link $url")

        return ShadowsocksBean().apply {
            serverAddress = httpUrl.host
            serverPort = httpUrl.port
            method = httpUrl.username
            password = httpUrl.password
            plugin = ""
            // val remarks = url.substringAfter("#").unUrlSafe()
            // if (remarks.isNotBlank()) name = remarks
            name = httpUrl.fragment ?: displayAddress()
        }
    }
}

fun ShadowsocksBean.toUri(): String {
    val builder = linkBuilder().username(Util.b64EncodeUrlSafe("$method:$password"))
        .host(serverAddress ?: "")
        .port(serverPort ?: 0)

    if (!plugin.isNullOrBlank()) {
        builder.addQueryParameter("plugin", plugin)
    }

    if (!name.isNullOrBlank()) {
        builder.encodedFragment(name?.urlSafe())
    }

    return builder.toLink("ss").replace("$serverPort/", "$serverPort")

}

fun JSONObject.parseShadowsocks(): ShadowsocksBean {
    return ShadowsocksBean().apply {
        serverAddress = getStr("server")
        serverPort = getIntNya("server_port")
        password = getStr("password")
        method = getStr("method")
        name = optString("remarks", "")

        val pId = getStr("plugin")
        if (!pId.isNullOrBlank()) {
            plugin = pId + ";" + optString("plugin_opts", "")
        }
    }
}

fun ShadowsocksBean.buildSingBoxOutboundShadowsocksBean(): SingBoxOptions.Outbound_ShadowsocksOptions {
    return SingBoxOptions.Outbound_ShadowsocksOptions().also { outbound ->
        outbound.type = "shadowsocks"
        outbound.server = serverAddress
        outbound.server_port = serverPort
        outbound.password = password
        outbound.method = method
        outbound.plugin?.ifBlank { null }?.let {
            outbound.plugin = it.substringBefore(";")
            outbound.plugin_opts = it.substringAfter(";")
        }
    }
}
