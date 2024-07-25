package io.mo.viaport.fmt.socks

import android.os.Parcelable
import com.esotericsoftware.kryo.io.ByteBufferInput
import com.esotericsoftware.kryo.io.ByteBufferOutput
import io.mo.viaport.fmt.AbstractBean
import io.mo.viaport.fmt.KryoConverters

class SOCKSBean : AbstractBean() {
    var protocol: Int = 0

    var sUoT: Boolean? = null

    var username: String? = null
    var password: String? = null

    fun protocolVersion(): Int {
        return when (protocol) {
            0, 1 -> 4
            else -> 5
        }
    }

    fun protocolName(): String {
        return when (protocol) {
            0 -> "SOCKS4"
            1 -> "SOCKS4A"
            else -> "SOCKS5"
        }
    }

    fun protocolVersionName(): String {
        return when (protocol) {
            0 -> "4"
            1 -> "4a"
            else -> "5"
        }
    }

    override fun network(): String? {
        if (protocol < PROTOCOL_SOCKS5) return "tcp"
        return super.network()
    }

    override fun initializeDefaultValues() {
        super.initializeDefaultValues()

        if (username == null) username = ""
        if (password == null) password = ""
        if (sUoT == null) sUoT = false
    }

    override fun serialize(output: ByteBufferOutput) {
        output.writeInt(2)
        super.serialize(output)
        output.writeInt(protocol)
        output.writeString(username)
        output.writeString(password)
        output.writeBoolean(sUoT!!)
    }

    override fun deserialize(input: ByteBufferInput) {
        val version = input.readInt()
        super.deserialize(input)
        if (version >= 1) {
            protocol = input.readInt()
        }
        username = input.readString()
        password = input.readString()
        if (version >= 2) {
            sUoT = input.readBoolean()
        }
    }

    override fun clone(): SOCKSBean {
        return KryoConverters.deserialize(SOCKSBean(), KryoConverters.serialize(this))
    }

    companion object {
        const val PROTOCOL_SOCKS4: Int = 0
        const val PROTOCOL_SOCKS4A: Int = 1
        const val PROTOCOL_SOCKS5: Int = 2

        val CREATOR: Parcelable.Creator<SOCKSBean> = object : CREATOR<SOCKSBean>() {
            override fun newInstance(): SOCKSBean {
                return SOCKSBean()
            }

            override fun newArray(size: Int): Array<SOCKSBean> {
                return arrayOf()
            }
        }
    }
}
