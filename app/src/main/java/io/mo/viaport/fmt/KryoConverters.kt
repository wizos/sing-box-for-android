// package io.mo.viaport.fmt
//
// import androidx.room.TypeConverter
// import com.elvishew.xlog.XLog
// import com.esotericsoftware.kryo.KryoException
// import com.esotericsoftware.kryo.io.ByteBufferInput
// import com.esotericsoftware.kryo.io.ByteBufferOutput
// import io.mo.viaport.fmt.http.HttpBean
// import io.mo.viaport.fmt.shadowsocks.ShadowsocksBean
// import io.mo.viaport.fmt.socks.SOCKSBean
// import io.mo.viaport.ktx.byteBuffer
// import java.io.ByteArrayInputStream
// import java.io.ByteArrayOutputStream
//
// object KryoConverters {
//     private val NULL = ByteArray(0)
//
//     @TypeConverter
//     fun serialize(bean: Serializable?): ByteArray {
//         if (bean == null) return NULL
//         val out = ByteArrayOutputStream()
//         val buffer: ByteBufferOutput = out.byteBuffer()
//         bean.serializeToBuffer(buffer)
//         buffer.flush()
//         buffer.close()
//         return out.toByteArray()
//     }
//
//     fun <T : Serializable> deserialize(bean: T, bytes: ByteArray?): T {
//         if (bytes == null) return bean
//         val input = ByteArrayInputStream(bytes)
//         val buffer: ByteBufferInput = input.byteBuffer()
//         try {
//             bean.deserializeFromBuffer(buffer)
//         } catch (e: KryoException) {
//             XLog.w(e)
//         }
//         bean.initializeDefaultValues()
//         return bean
//     }
//
//     @TypeConverter
//     fun socksDeserialize(bytes: ByteArray): SOCKSBean? {
//         if (bytes.isEmpty()) return null
//         return deserialize(SOCKSBean(), bytes)
//     }
//
//     @TypeConverter
//     fun httpDeserialize(bytes: ByteArray?): HttpBean? {
//         if (bytes?.isEmpty() == true) return null
//         return deserialize(HttpBean(), bytes)
//     }
//
//     @TypeConverter
//     fun shadowsocksDeserialize(bytes: ByteArray?): ShadowsocksBean? {
//         if (bytes?.isEmpty() == true) return null
//         return deserialize(ShadowsocksBean(), bytes)
//     }
//
//     @TypeConverter
//     fun configDeserialize(bytes: ByteArray?): ConfigBean? {
//         if (bytes?.isEmpty() == true) return null
//         return deserialize(ConfigBean(), bytes)
//     }
//
//     @TypeConverter
//     fun vmessDeserialize(bytes: ByteArray?): VMessBean? {
//         if (bytes?.isEmpty() == true) return null
//         return deserialize(VMessBean(), bytes)
//     }
//
//     @TypeConverter
//     fun trojanDeserialize(bytes: ByteArray?): TrojanBean? {
//         if (bytes?.isEmpty() == true) return null
//         return deserialize(TrojanBean(), bytes)
//     }
//
//     @TypeConverter
//     fun trojanGoDeserialize(bytes: ByteArray?): TrojanGoBean? {
//         if (bytes?.isEmpty() == true) return null
//         return deserialize(TrojanGoBean(), bytes)
//     }
//
//     @TypeConverter
//     fun mieruDeserialize(bytes: ByteArray?): MieruBean? {
//         if (bytes?.isEmpty() == true) return null
//         return deserialize(MieruBean(), bytes)
//     }
//
//     @TypeConverter
//     fun naiveDeserialize(bytes: ByteArray?): NaiveBean? {
//         if (bytes?.isEmpty() == true) return null
//         return deserialize(NaiveBean(), bytes)
//     }
//
//     @TypeConverter
//     fun hysteriaDeserialize(bytes: ByteArray?): HysteriaBean? {
//         if (bytes?.isEmpty() == true) return null
//         return deserialize(HysteriaBean(), bytes)
//     }
//
//     @TypeConverter
//     fun sshDeserialize(bytes: ByteArray?): SSHBean? {
//         if (bytes?.isEmpty() == true) return null
//         return deserialize(SSHBean(), bytes)
//     }
//
//     @TypeConverter
//     fun wireguardDeserialize(bytes: ByteArray?): WireGuardBean? {
//         if (bytes?.isEmpty() == true) return null
//         return deserialize(WireGuardBean(), bytes)
//     }
//
//     @TypeConverter
//     fun tuicDeserialize(bytes: ByteArray?): TuicBean? {
//         if (bytes?.isEmpty() == true) return null
//         return deserialize(TuicBean(), bytes)
//     }
//
//     @TypeConverter
//     fun shadowTLSDeserialize(bytes: ByteArray?): ShadowTLSBean? {
//         if (bytes?.isEmpty() == true) return null
//         return deserialize(ShadowTLSBean(), bytes)
//     }
//
//     @TypeConverter
//     fun chainDeserialize(bytes: ByteArray?): ChainBean? {
//         if (bytes?.isEmpty() == true) return null
//         return deserialize(ChainBean(), bytes)
//     }
//
//     @TypeConverter
//     fun nekoDeserialize(bytes: ByteArray?): NekoBean? {
//         if (bytes?.isEmpty() == true) return null
//         return deserialize(NekoBean(), bytes)
//     }
//
//     @TypeConverter
//     fun subscriptionDeserialize(bytes: ByteArray?): SubscriptionBean? {
//         if (bytes?.isEmpty() == true) return null
//         return deserialize(SubscriptionBean(), bytes)
//     }
// }
