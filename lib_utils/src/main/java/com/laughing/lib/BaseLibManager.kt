package com.laughing.lib


var isBaseLibDebug = true

object BaseLibManager {
    fun init(debug: Boolean) {
        isBaseLibDebug = debug
    }
}