// package io.mo.viaport.fmt
//
// import android.content.Context
// import android.content.Intent
// import androidx.room.*
// import com.esotericsoftware.kryo.io.ByteBufferInput
// import com.esotericsoftware.kryo.io.ByteBufferOutput
// import io.mo.viaport.fmt.http.HttpBean
// import io.mo.viaport.fmt.hysteria.HysteriaBean
// import io.mo.viaport.fmt.hysteria.canUseSingBox
// import io.mo.viaport.fmt.internal.ChainBean
// import io.mo.viaport.fmt.mieru.MieruBean
// import io.mo.viaport.fmt.naive.NaiveBean
// import io.mo.viaport.fmt.shadowsocks.ShadowsocksBean
// import io.mo.viaport.fmt.shadowtls.ShadowTLSBean
// import io.mo.viaport.fmt.socks.SOCKSBean
// import io.mo.viaport.fmt.ssh.SSHBean
// import io.mo.viaport.fmt.trojan.TrojanBean
// import io.mo.viaport.fmt.trojan_go.TrojanGoBean
// import io.mo.viaport.fmt.tuic.TuicBean
// import io.mo.viaport.fmt.v2ray.VMessBean
// import io.mo.viaport.fmt.v2ray.isTLS
// import io.mo.viaport.fmt.v2ray.isVLESS
// import io.mo.viaport.fmt.wireguard.WireGuardBean
//
//
// data class ProxyEntity(
//     var groupId: Long = 0L,
//     var type: Int = 0,
//     var userOrder: Long = 0L,
//     var tx: Long = 0L,
//     var rx: Long = 0L,
//     var status: Int = 0,
//     var ping: Int = 0,
//     var uuid: String = "",
//     var error: String? = null,
//     var socksBean: SOCKSBean? = null,
//     var httpBean: HttpBean? = null,
//     var ssBean: ShadowsocksBean? = null,
//     var vmessBean: VMessBean? = null,
//     var trojanBean: TrojanBean? = null,
//     var trojanGoBean: TrojanGoBean? = null,
//     var mieruBean: MieruBean? = null,
//     var naiveBean: NaiveBean? = null,
//     var hysteriaBean: HysteriaBean? = null,
//     var tuicBean: TuicBean? = null,
//     var sshBean: SSHBean? = null,
//     var wgBean: WireGuardBean? = null,
//     var chainBean: ChainBean? = null,
//     var shadowTLSBean: ShadowTLSBean? = null,
//     var nekoBean: NekoBean? = null,
//     var configBean: ConfigBean? = null,
// ) : Serializable() {
//
//     companion object {
//         const val TYPE_SOCKS = 0
//         const val TYPE_HTTP = 1
//         const val TYPE_SS = 2
//         const val TYPE_VMESS = 4
//         const val TYPE_TROJAN = 6
//
//         const val TYPE_TROJAN_GO = 7
//         const val TYPE_MIERU = 21
//         const val TYPE_NAIVE = 9
//         const val TYPE_HYSTERIA = 15
//         const val TYPE_TUIC = 20
//
//         const val TYPE_SSH = 17
//         const val TYPE_WG = 18
//
//         const val TYPE_SHADOWTLS = 19
//
//         const val TYPE_CONFIG = 998
//         const val TYPE_NEKO = 999
//
//         const val TYPE_CHAIN = 8
//
//         val chainName by lazy { app.getString(R.string.proxy_chain) }
//
//         private val placeHolderBean = SOCKSBean().applyDefaultValues()
//
//         @JvmField
//         val CREATOR = object : Serializable.CREATOR<ProxyEntity>() {
//
//             override fun newInstance(): ProxyEntity {
//                 return ProxyEntity()
//             }
//
//             override fun newArray(size: Int): Array<ProxyEntity?> {
//                 return arrayOfNulls(size)
//             }
//         }
//     }
//
//     @Ignore
//     @Transient
//     var dirty: Boolean = false
//
//     fun displayType(): String = when (type) {
//         TYPE_SOCKS -> socksBean!!.protocolName()
//         TYPE_HTTP -> if (httpBean!!.isTLS()) "HTTPS" else "HTTP"
//         TYPE_SS -> "Shadowsocks"
//         TYPE_VMESS -> if (vmessBean!!.isVLESS) "VLESS" else "VMess"
//         TYPE_TROJAN -> "Trojan"
//         TYPE_TROJAN_GO -> "Trojan-Go"
//         TYPE_MIERU -> "Mieru"
//         TYPE_NAIVE -> "Naïve"
//         TYPE_HYSTERIA -> "Hysteria" + hysteriaBean!!.protocolVersion
//         TYPE_SSH -> "SSH"
//         TYPE_WG -> "WireGuard"
//         TYPE_TUIC -> "TUIC"
//         TYPE_SHADOWTLS -> "ShadowTLS"
//         TYPE_CHAIN -> chainName
//         TYPE_NEKO -> nekoBean!!.displayType()
//         TYPE_CONFIG -> configBean!!.displayType()
//         else -> "Undefined type $type"
//     }
//
//     fun displayName() = requireBean().displayName()
//     fun displayAddress() = requireBean().displayAddress()
//
//     fun requireBean(): AbstractBean {
//         return when (type) {
//             TYPE_SOCKS -> socksBean
//             TYPE_HTTP -> httpBean
//             TYPE_SS -> ssBean
//             TYPE_VMESS -> vmessBean
//             TYPE_TROJAN -> trojanBean
//             TYPE_TROJAN_GO -> trojanGoBean
//             TYPE_MIERU -> mieruBean
//             TYPE_NAIVE -> naiveBean
//             TYPE_HYSTERIA -> hysteriaBean
//             TYPE_SSH -> sshBean
//             TYPE_WG -> wgBean
//             TYPE_TUIC -> tuicBean
//             TYPE_SHADOWTLS -> shadowTLSBean
//             TYPE_CHAIN -> chainBean
//             TYPE_NEKO -> nekoBean
//             TYPE_CONFIG -> configBean
//             else -> error("Undefined type $type")
//         } ?: error("Null ${displayType()} profile")
//     }
//
//     fun haveLink(): Boolean {
//         return when (type) {
//             TYPE_CHAIN -> false
//             else -> true
//         }
//     }
//
//     fun haveStandardLink(): Boolean {
//         return when (requireBean()) {
//             is SSHBean -> false
//             is WireGuardBean -> false
//             is ShadowTLSBean -> false
//             is NekoBean -> nekoBean!!.haveStandardLink()
//             is ConfigBean -> false
//             else -> true
//         }
//     }
//
//     fun toStdLink(compact: Boolean = false): String = with(requireBean()) {
//         when (this) {
//             is SOCKSBean -> toUri()
//             is HttpBean -> toUri()
//             is ShadowsocksBean -> toUri()
//             is VMessBean -> toUriVMessVLESSTrojan(false)
//             is TrojanBean -> toUriVMessVLESSTrojan(true)
//             is TrojanGoBean -> toUri()
//             is NaiveBean -> toUri()
//             is HysteriaBean -> toUri()
//             is TuicBean -> toUri()
//             is NekoBean -> shareLink()
//             else -> toUniversalLink()
//         }
//     }
//
//     fun exportConfig(): Pair<String, String> {
//         var name = "${requireBean().displayName()}.json"
//
//         return with(requireBean()) {
//             StringBuilder().apply {
//                 val config = buildConfig(this@ProxyEntity, forExport = true)
//                 append(config.config)
//
//                 if (!config.externalIndex.all { it.chain.isEmpty() }) {
//                     name = "profiles.txt"
//                 }
//
//                 for ((chain) in config.externalIndex) {
//                     chain.entries.forEachIndexed { index, (port, profile) ->
//                         when (val bean = profile.requireBean()) {
//                             is TrojanGoBean -> {
//                                 append("\n\n")
//                                 append(bean.buildTrojanGoConfig(port))
//                             }
//
//                             is MieruBean -> {
//                                 append("\n\n")
//                                 append(bean.buildMieruConfig(port))
//                             }
//
//                             is NaiveBean -> {
//                                 append("\n\n")
//                                 append(bean.buildNaiveConfig(port))
//                             }
//
//                             is HysteriaBean -> {
//                                 append("\n\n")
//                                 append(bean.buildHysteria1Config(port, null))
//                             }
//                         }
//                     }
//                 }
//             }.toString()
//         } to name
//     }
//
//     fun needExternal(): Boolean {
//         return when (type) {
//             TYPE_TROJAN_GO -> true
//             TYPE_MIERU -> true
//             TYPE_NAIVE -> true
//             TYPE_HYSTERIA -> !hysteriaBean!!.canUseSingBox()
//             TYPE_NEKO -> true
//             else -> false
//         }
//     }
//
//     fun needCoreMux(): Boolean {
//         return when (type) {
//             TYPE_VMESS -> if (vmessBean!!.isVLESS()) {
//                 Protocols.isProfileNeedMux(vmessBean!!) && Protocols.shouldEnableMux("vless")
//             } else {
//                 Protocols.isProfileNeedMux(vmessBean!!) && Protocols.shouldEnableMux("vmess")
//             }
//
//             TYPE_TROJAN -> Protocols.isProfileNeedMux(trojanBean!!)
//                     && Protocols.shouldEnableMux("trojan")
//
//             TYPE_SS -> !ssBean!!.sUoT && Protocols.shouldEnableMux("shadowsocks")
//             else -> false
//         }
//     }
//
//     override fun describeContents(): Int {
//         return 0
//     }
// }