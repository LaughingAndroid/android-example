package com.laughing.lib.base

import com.cf.holder.IHolderBuilder
import com.cf.holder.QuickAdapter
import com.laughing.lib.utils.as2

inline fun <reified T> getHolderBuilder(): IHolderBuilder<*> {
    val className = T::class.java.simpleName + "Builder"
    return Class.forName(className).newInstance().as2()!!
}

inline fun <reified T> QuickAdapter.addHolder(key: Int? = null) {
    addHolder(getHolderBuilder<T>(), key)
}

