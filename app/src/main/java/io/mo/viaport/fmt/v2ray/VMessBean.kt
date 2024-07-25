package io.mo.viaport.fmt.v2ray

class VMessBean : StandardV2RayBean() {
    var alterId: Int? = null // alterID == -1 --> VLESS

    override fun initDefaultValues() {
        super.initDefaultValues()

        alterId = if (alterId != null) alterId else 0
        encryption = encryption?.ifBlank { "auto" } ?:  "auto"
    }
}
