package io.mo.viaport.fmt.trojan_go

import io.mo.viaport.fmt.LOCAL_HOST
import io.mo.viaport.ktx.isIpAddress
import io.mo.viaport.ktx.linkBuilder
import io.mo.viaport.ktx.toLink
import io.mo.viaport.fmt.toStringPretty
import io.mo.viaport.ktx.urlSafe
import io.mo.viaport.persist.LocalSettings
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray
import org.json.JSONObject

fun parseTrojanGo(server: String): TrojanGoBean {
    val link = server.replace("trojan-go://", "https://").toHttpUrlOrNull() ?: error(
        "invalid trojan-link link $server"
    )
    return TrojanGoBean().apply {
        serverAddress = link.host
        serverPort = link.port
        password = link.username
        link.queryParameter("sni")?.let {
            sni = it
        }
        link.queryParameter("type")?.let { lType ->
            type = lType

            when (type) {
                "ws" -> {
                    link.queryParameter("host")?.let {
                        host = it
                    }
                    link.queryParameter("path")?.let {
                        path = it
                    }
                }
                else -> {
                }
            }
        }
        link.queryParameter("encryption")?.let {
            encryption = it
        }
        link.queryParameter("plugin")?.let {
            plugin = it
        }
        link.fragment.takeIf { !it.isNullOrBlank() }?.let {
            name = it
        }
    }
}

fun JSONObject.parseTrojanGo(): TrojanGoBean {
    return TrojanGoBean().apply {
        serverAddress = optString("remote_addr", serverAddress ?: "")
        serverPort = optInt("remote_port", serverPort ?: 0)
        when (val pass = get("password")) {
            is String -> {
                password = pass
            }
            is List<*> -> {
                password = pass[0] as String
            }
        }
        optJSONArray("ssl")?.apply {
            sni = optString("sni", sni?:"")
        }
        optJSONArray("websocket")?.apply {
            if (optBoolean("enabled", false)) {
                type = "ws"
                host = optString("host", host?:"")
                path = optString("path", path?:"")
            }
        }
        optJSONArray("shadowsocks")?.apply {
            if (optBoolean("enabled", false)) {
                encryption = "ss;${optString("method", "")}:${optString("password", "")}"
            }
        }
    }
}

/**
 * 规范：https://github.com/p4gefau1t/trojan-go/issues/132
 * https://p4gefau1t.github.io/trojan-go/developer/url/
 * trojan-go://f@uck.me/?sni=microsoft.com&type=ws&path=%2Fgo&encryption=ss%3Baes-256-gcm%3Afuckgfw
 * 没有 mux 这个字段。
 */
fun TrojanGoBean.toUri(): String {
    val builder = linkBuilder().username(password ?: "").host(serverAddress ?: "").port(serverPort ?: 0)
    if (!sni.isNullOrBlank()) {
        builder.addQueryParameter("sni", sni)
    }
    if (!type.isNullOrBlank() && type != "original") {
        builder.addQueryParameter("type", type)

        when (type) {
            "ws" -> {
                if (!host.isNullOrBlank()) {
                    builder.addQueryParameter("host", host)
                }
                if (!path.isNullOrBlank()) {
                    builder.addQueryParameter("path", path)
                }
            }
        }
    }
    if (!type.isNullOrBlank() && type != "none") {
        builder.addQueryParameter("encryption", encryption)
    }
    if (!plugin.isNullOrBlank()) {
        builder.addQueryParameter("plugin", plugin)
    }

    if (!name.isNullOrBlank()) {
        builder.encodedFragment(name?.urlSafe())
    }

    return builder.toLink("trojan-go")
}


fun TrojanGoBean.toTrojanGoConfig(port: Int): String {
    return JSONObject().apply {
        put("run_type", "client")
        put("local_addr", LOCAL_HOST)
        put("local_port", port)
        put("remote_addr", finalAddress)
        put("remote_port", finalPort)
        put("password", JSONArray().apply {
            put(password)
        })
        put("log_level", if (LocalSettings.logLevel > 0) 0 else 2)
        // if (Protocols.shouldEnableMux("trojan-go")) put("mux", JSONObject().apply {
        //     put("enabled", true)
        //     put("concurrency", DataStore.muxConcurrency)
        // })
        // put("tcp", JSONObject().apply {
        //     put("prefer_ipv4", DataStore.ipv6Mode <= IPv6Mode.ENABLE)
        // })

        when (type) {
            "original" -> {
            }
            "ws" -> put("websocket", JSONObject().apply {
                put("enabled", true)
                put("host", host)
                put("path", path)
            })
        }

        if (sni?.isBlank() == true && finalAddress == LOCAL_HOST && serverAddress?.isIpAddress() == false) {
            sni = serverAddress
        }

        put("ssl", JSONObject().apply {
            if (sni?.isNotBlank() == true) put("sni", sni)
            if (allowInsecure == true) put("verify", false)
        })

        when {
            encryption == "none" -> {
            }
            encryption?.startsWith("ss;") == true -> put("shadowsocks", JSONObject().apply {
                put("enabled", true)
                put("method", encryption?.substringAfter(";")?.substringBefore(":"))
                put("password", encryption?.substringAfter(":"))
            })
        }
    }.toStringPretty()
}

// {
//     "type": "trojan",
//     "tag": "trojan-out",
//     "server": "127.0.0.1",
//     "server_port": 1080,
//     "password": "8JCsPssfgS8tiRwiMlhARg==",
//     "network": "tcp",
//     "tls": {},
//     "multiplex": {},
//     "transport": {},
//     ... // 拨号字段
// }
fun TrojanGoBean.toSingBoxConfig(): String {
    return JSONObject().apply {
        put("type", "trojan")
        put("server", serverAddress)
        put("server_port", serverPort)
        put("password", password)
        put("network", network())
        // if (Protocols.shouldEnableMux("trojan-go")) put("mux", JSONObject().apply {
        //     put("enabled", true)
        //     put("concurrency", DataStore.muxConcurrency)
        // })
        when (type) {
            "original" -> {
            }
            "ws" -> put("websocket", JSONObject().apply {
                put("enabled", true)
                put("host", host)
                put("path", path)
            })
        }

        if (sni?.isBlank() == true && finalAddress == LOCAL_HOST && serverAddress?.isIpAddress() == false) {
            sni = serverAddress
        }

        put("ssl", JSONObject().apply {
            if (sni?.isNotBlank() == true) put("sni", sni)
            if (allowInsecure == true) put("verify", false)
        })

        when {
            encryption == "none" -> {
            }
            encryption?.startsWith("ss;") == true -> put("shadowsocks", JSONObject().apply {
                put("enabled", true)
                put("method", encryption?.substringAfter(";")?.substringBefore(":"))
                put("password", encryption?.substringAfter(":"))
            })
        }
    }.toStringPretty()
}
