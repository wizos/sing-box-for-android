package io.mo.viaport.fmt.shadowsocks

import io.mo.viaport.fmt.AbstractBean

class ShadowsocksBean : AbstractBean() {
    var method: String? = null
    var password: String? = null
    var plugin: String? = null

    var sUoT: Boolean? = null

    override fun initDefaultValues() {
        super.initDefaultValues()

        if (method.isNullOrBlank()) method = "aes-256-gcm"
        if (password == null) password = ""
        if (plugin == null) plugin = ""
        if (sUoT == null) sUoT = false
    }
}
// https://sing-box.sagernet.org/zh/configuration/outbound/shadowsocks/
data class SingBoxShadowsocks(val type: String = "shadowsocks", val method: String, val password: String, val multiplex: SingBoxShadowsocksMultiplex?)
data class SingBoxShadowsocksMultiplex(val enabled: Boolean, val padding: Boolean, val brutal: SingBoxShadowsocksMultiplexBrutal?)
data class SingBoxShadowsocksMultiplexBrutal(val enabled: Boolean, val upMbps: Int, val downMbps: Int)