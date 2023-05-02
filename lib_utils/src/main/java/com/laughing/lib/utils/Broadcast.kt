package com.laughing.lib.utils


import android.content.Context
import android.content.Intent

inline fun startBroadcast(
    action: String,
    vararg pairs: Pair<String, Any?>,
    crossinline block: Intent.() -> Unit = {}
) =
    topActivity.startBroadcast(action, pairs = pairs, block = block)

inline fun Context.startBroadcast(
    action: String,
    vararg pairs: Pair<String, Any?>,
    crossinline block: Intent.() -> Unit = {}
) =
    sendBroadcast(intentOf0(*pairs).apply {
        setAction(action)
    }.apply(block))