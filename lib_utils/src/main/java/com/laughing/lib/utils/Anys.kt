package com.laughing.lib.utils

import android.text.TextUtils
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.jeremyliao.liveeventbus.core.Observable
import org.json.JSONObject

val gson = Gson()

inline fun <reified T> String?.toObject(): T? {
    val json = this
    if (TextUtils.isEmpty(json)) {
        return null
    }
    return try {
        gson.fromJson(json, T::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun Any?.toJson(): String {
    this?.let {
        return gson.toJson(this)
    }
    return ""
}

fun Any?.toJsonObject(): JSONObject? {
    this?.let {
        return try {
            JSONObject(toJson())
        } catch (e: Exception) {
            null
        }
    }
    return null
}


inline fun <reified T> Any?.getField(name: String): T? {
    val obj = this ?: return null
    val field = try {
        obj.javaClass.getDeclaredField(name)
    } catch (e: Exception) {
        return null
    }
    field.isAccessible = true
    return field.get(obj) as? T
}

fun Any?.setField(name: String, value: Any?) {
    val obj = this ?: return
    val field = try {
        obj.javaClass.getDeclaredField(name)
    } catch (e: Exception) {
        return
    }
    field.isAccessible = true
    return field.set(obj, value)
}


inline fun <reified T> Any?.as2(): T? {
    return this as? T
}


fun Any?.log() {
    Logs.d(this)
}

fun <T> Observable<T>.value(): T? {
    return this.getField<LiveData<T>>("liveData")?.value
}