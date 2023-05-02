package com.laughing.lib.player

import com.laughing.lib.utils.saveGet
import kotlin.math.max
import kotlin.math.min



class PlayList {
    var mediaList = mutableListOf<MediaData>()
    var playModel = PlayMode.LIST
    var currentIndex = 0

    fun pre(): MediaData? {
        currentIndex--
        currentIndex = max(0, currentIndex)
        return current()
    }

    fun next(): MediaData? {
        currentIndex++
        currentIndex = min(mediaList.size - 1, currentIndex)
        return current()
    }

    fun current(): MediaData? {
        return mediaList.saveGet(currentIndex)
    }

    fun add(mediaData: MediaData): Int {
        val hasData = mediaList.filter { it.url == mediaData.url }.isNotEmpty()
        if (!hasData) {
            mediaList.add(mediaData)
        }
        return mediaList.indexOf(mediaData)
    }
}

enum class PlayMode {
    LIST, SINGLE, LIST_LOOPER, RANDOM
}



