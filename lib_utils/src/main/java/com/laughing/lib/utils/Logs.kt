package com.laughing.lib.utils

import android.util.Log
import com.laughing.lib.isBaseLibDebug

object Logs {
    private const val tag = "【laughing】"

    @JvmStatic
    fun d(t:String, any: Any?) {
        if (isBaseLibDebug) {
            Log.d(tag, "$t $any")
        }
    }
    @JvmStatic
    fun d(any: Any?) {
        if (isBaseLibDebug) {
            Log.d(tag, "$any")
        }
    }

    @JvmStatic
    fun e(any: Any?) {
        if (isBaseLibDebug) {
            Log.e(tag, "$any")
        }
    }
}