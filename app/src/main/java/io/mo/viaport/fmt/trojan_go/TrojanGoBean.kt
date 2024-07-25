package io.mo.viaport.fmt.trojan_go

import com.esotericsoftware.kryo.io.ByteBufferInput
import com.esotericsoftware.kryo.io.ByteBufferOutput
import io.mo.viaport.fmt.AbstractBean

class TrojanGoBean : AbstractBean() {
    /**
     * Trojan 的密码。
     * 不可省略，不能为空字符串，不建议含有非 ASCII 可打印字符。
     * 必须使用 encodeURIComponent 编码。
     */
    var password: String? = null

    /**
     * 自定义 TLS 的 SNI。
     * 省略时默认与 trojan-host 同值。不得为空字符串。
     *
     *
     * 必须使用 encodeURIComponent 编码。
     */
    var sni: String? = null

    /**
     * 传输类型。
     * 省略时默认为 original，但不可为空字符串。
     * 目前可选值只有 original 和 ws，未来可能会有 h2、h2+ws 等取值。
     *
     *
     * 当取值为 original 时，使用原始 Trojan 传输方式，无法方便通过 CDN。
     * 当取值为 ws 时，使用 wss 作为传输层。
     */
    var type: String? = null

    /**
     * 自定义 HTTP Host 头。
     * 可以省略，省略时值同 trojan-host。
     * 可以为空字符串，但可能带来非预期情形。
     *
     *
     * 警告：若你的端口非标准端口（不是 80 / 443），RFC 标准规定 Host 应在主机名后附上端口号，例如 example.com:44333。至于是否遵守，请自行斟酌。
     *
     *
     * 必须使用 encodeURIComponent 编码。
     */
    var host: String? = null

    /**
     * 当传输类型 type 取 ws、h2、h2+ws 时，此项有效。
     * 不可省略，不可为空。
     * 必须以 / 开头。
     * 可以使用 URL 中的 & # ? 等字符，但应当是合法的 URL 路径。
     *
     *
     * 必须使用 encodeURIComponent 编码。
     */
    var path: String? = null

    /**
     * 用于保证 Trojan 流量密码学安全的加密层。
     * 可省略，默认为 none，即不使用加密。
     * 不可以为空字符串。
     *
     *
     * 必须使用 encodeURIComponent 编码。
     *
     *
     * 使用 Shadowsocks 算法进行流量加密时，其格式为：
     *
     *
     * ss;method:password
     *
     *
     * 其中 ss 是固定内容，method 是加密方法，必须为下列之一：
     *
     *
     * aes-128-gcm
     * aes-256-gcm
     * chacha20-ietf-poly1305
     */
    var encryption: String? = null

    /**
     * 额外的插件选项。本字段保留。
     * 可省略，但不可以为空字符串。
     */
    // not used in NB4A
    var plugin: String? = null

    // ---
    var allowInsecure: Boolean? = null

    override fun initDefaultValues() {
        super.initDefaultValues()

        if (password == null) password = ""
        if (sni == null) sni = ""
        if (type.isNullOrBlank()) type = "original"
        if (host == null) host = ""
        if (path == null) path = ""
        if (encryption.isNullOrBlank()) encryption = "none"
        if (plugin == null) plugin = ""
        if (allowInsecure == null) allowInsecure = false
    }
}
