package io.mo.viaport.fmt.trojan

import android.os.Parcelable
import com.esotericsoftware.kryo.io.ByteBufferInput
import com.esotericsoftware.kryo.io.ByteBufferOutput
import io.mo.viaport.fmt.v2ray.StandardV2RayBean

class TrojanBean : StandardV2RayBean() {
    var password: String? = null

    override fun initDefaultValues() {
        super.initDefaultValues()
        if (security == null || security!!.isEmpty()) security = "tls"
        if (password == null) password = ""
    }
}
