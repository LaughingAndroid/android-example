package com.laughing.lib.net

class ServerException(var code: Int?, message: String) : Exception(message) {
}