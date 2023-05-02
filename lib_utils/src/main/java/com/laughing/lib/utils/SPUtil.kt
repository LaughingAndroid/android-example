package com.laughing.lib.utils

import com.tencent.mmkv.MMKV

object SPUtil {
    init {
        val root = MMKV.initialize(application)
        Logs.d("mmkv root:$root")
    }

    var kv: MMKV = MMKV.defaultMMKV()

    fun put(key: String, value: Any) {
        value.javaClass
        when (value.javaClass) {
            String::class.java -> {
                kv.putString(key, value as String)
            }
            Long::class.java -> {
                kv.putLong(key, value as Long)
            }
            Float::class.java -> {
                kv.putFloat(key, value as Float)
            }
            Int::class.java -> {
                kv.putInt(key, value as Int)
            }
            Boolean::class.java -> {
                kv.putBoolean(key, value as Boolean)
            }
            else -> {
                kv.putString(key, value.toJson())
            }
        }
    }


    fun getInt(key: String, default: Int = 0): Int {
        return kv.getInt(key, default)
    }

    fun getString(key: String, default: String = ""): String? {
        return kv.getString(key, default)
    }

    fun getFloat(key: String, default: Float = 0f): Float {
        return kv.getFloat(key, default)
    }

    fun getLong(key: String, default: Long = 0): Long {
        return kv.getLong(key, default)
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return kv.getBoolean(key, default)
    }

    inline fun <reified T> getObject(key: String, default: T? = null): T? {
        val jsonValue = getString(key, "")
        return jsonValue.toObject() ?: default
    }

    fun remove(key: String) {
        kv.remove(key)
    }

}


