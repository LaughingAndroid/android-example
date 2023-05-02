package com.laughing.lib.net

open class BaseResult<T> {
    var code: Int? = null
    var msg: String? = null
    var data: T? = null
    fun isOk():Boolean {
        return code == 200
    }
}