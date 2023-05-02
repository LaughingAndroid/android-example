package com.laughing.lib.player

class MediaData(var url: String, var sourceType: SourceType = SourceType.OSS)

enum class SourceType {
    OSS, ASSETS, RAW
}