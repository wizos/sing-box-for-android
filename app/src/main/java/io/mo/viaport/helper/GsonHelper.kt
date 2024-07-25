package io.mo.viaport.helper

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy


object GsonHelper {
    // @JvmStatic
    val gson: Gson by lazy {
        GsonBuilder()
            .setPrettyPrinting()
            .setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .setLenient()
            .disableHtmlEscaping()
            .create() }
}
