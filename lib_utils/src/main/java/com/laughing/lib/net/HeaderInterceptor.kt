package com.laughing.lib.net

import com.laughing.lib.utils.SPUtil
import com.laughing.lib.utils.appVersionCode
import com.laughing.lib.utils.packageName
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class HeaderInterceptor : Interceptor {
    companion object {
        fun saveToken(token: String?) {
            token ?: return
            SPUtil.put("token", token)
        }

        fun getToken(): String {
            return SPUtil.getString("token") ?: ""
        }
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return chain.proceed(addParam(request))
    }

    private fun addParam(request: Request): Request {
        //添加公共header
        val requestBuilder = request.newBuilder()

        val token = getToken()
        if (token.isNotEmpty()) {
            requestBuilder.addHeader("X-Signature", token)
        }
        requestBuilder.addHeader("X-Vno", packageName + "_Android_" + appVersionCode)
        val builder = request.url.newBuilder()
        return requestBuilder
            .method(request.method, request.body)
            .url(builder.build())
            .build()
    }
}

