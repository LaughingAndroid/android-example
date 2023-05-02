@file:Suppress("unused")

package com.laughing.lib.utils

inline fun <T> List<T>.percentage(predicate: (T) -> Boolean) =
  filter(predicate).size.toFloat() / size
