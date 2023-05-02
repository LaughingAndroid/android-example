package com.laughing.lib.utils

/**
 *
 * @ClassName: Lists
 * @Description:
 * @Author: Laughing
 * @CreateDate: 2019/9/11 10:12
 * @Version: 1.7.0
 */
inline fun <reified T> List<*>.getFirstAsT(): T? {
    for (any in this) {
        if (any is T) {
            return any
        }
    }
    return null
}

fun <T> ArrayList<T>.addNotRepeat(t: T) {
    if (!this.contains(t)) {
        t?.let { this.add(it) }
    }
}

fun <T> MutableList<T>.addNotRepeat(t: T) {
    if (!this.contains(t)) {
        t?.let { this.add(it) }
    }
}

fun <T> List<T>.saveGet(index: Int): T? {
    if (index < 0 || index > size - 1) {
        return null
    }
    return get(index)
}