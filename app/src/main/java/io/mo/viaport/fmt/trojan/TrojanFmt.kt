package io.mo.viaport.fmt.trojan

import io.mo.viaport.fmt.v2ray.parseDuckSoft
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

fun parseTrojan(server: String): TrojanBean {

    val httpUrl = server.replace("trojan://", "https://").toHttpUrlOrNull() ?: error("invalid trojan link $server")

    return TrojanBean().apply {
        parseDuckSoft(httpUrl)
        httpUrl.queryParameter("allowInsecure")?.apply { if (this == "1" || this == "true") allowInsecure = true }
        httpUrl.queryParameter("peer")?.apply { if (this.isNotBlank()) sni = this }
    }
}
