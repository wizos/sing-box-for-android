// package io.mo.viaport.fmt.shadowtls
//
// import android.os.Parcelable
// import com.esotericsoftware.kryo.io.ByteBufferInput
// import com.esotericsoftware.kryo.io.ByteBufferOutput
// import io.mo.viaport.fmt.KryoConverters
// import io.mo.viaport.fmt.v2ray.StandardV2RayBean
//
// class ShadowTLSBean : StandardV2RayBean() {
//     var version: Int? = null
//     var password: String? = null
//
//     override fun initializeDefaultValues() {
//         super.initializeDefaultValues()
//
//         security = "tls"
//         if (version == null) version = 3
//         if (password == null) password = ""
//     }
//
//     override fun serialize(output: ByteBufferOutput) {
//         output.writeInt(0)
//         super.serialize(output)
//         output.writeInt(version!!)
//         output.writeString(password)
//     }
//
//     override fun deserialize(input: ByteBufferInput) {
//         val version_ = input.readInt()
//         super.deserialize(input)
//         version = input.readInt()
//         password = input.readString()
//     }
//
//     override fun clone(): ShadowTLSBean {
//         return KryoConverters.deserialize(ShadowTLSBean(), KryoConverters.serialize(this))
//     }
//
//     companion object {
//         val CREATOR: CREATOR<ShadowTLSBean?> = object : CREATOR<ShadowTLSBean?>() {
//             override fun newInstance(): ShadowTLSBean {
//                 return ShadowTLSBean()
//             }
//
//             override fun newArray(size: Int): Array<ShadowTLSBean> {
//                 return arrayOfNulls(size)
//             }
//         }
//     }
// }
