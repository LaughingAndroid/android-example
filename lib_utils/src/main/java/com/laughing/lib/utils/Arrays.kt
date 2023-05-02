@file:Suppress("unused")

package com.laughing.lib.utils

inline fun <T> Array<T>.percentage(predicate: (T) -> Boolean) =
  filter(predicate).size.toFloat() / size
