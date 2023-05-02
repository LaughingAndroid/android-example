package com.laughing.lib.net

import com.google.gson.Gson
import com.readystatesoftware.chuck.ChuckInterceptor
import com.laughing.lib.utils.application
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object HttpHelp {

    private val DEFAULT_TIMEOUT: Long = 30

    val okHttpClient = OkHttpClient().newBuilder()
        .retryOnConnectionFailure(true)
        .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS) //设置超时时间
        .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS) //设置读取超时时间
        .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS) //设置写入超时时间
        .apply {
            getSSLSocketFactory()?.let { sslSocketFactory(it, MyX509TrustManager()) }
        }
        .addInterceptor(LoggingInterceptor())
        .addInterceptor(HeaderInterceptor())
        .addInterceptor(ChuckInterceptor(application))
        .hostnameVerifier { s, sslSession -> true }
        .build()

    fun getServiceRetrofit(host: String, ok: OkHttpClient? = null): Retrofit {


        return Retrofit.Builder()
            .client(ok ?: okHttpClient)
            .baseUrl(host)
            .addConverterFactory(WrapperConverterFactory(Gson()))
            .build()
    }

    private fun getSSLSocketFactory(): SSLSocketFactory? {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts =
                arrayOf<TrustManager>(
                    object : X509TrustManager {
                        @Throws(CertificateException::class)
                        override fun checkClientTrusted(
                            chain: Array<X509Certificate>,
                            authType: String
                        ) {
                        }

                        @Throws(CertificateException::class)
                        override fun checkServerTrusted(
                            chain: Array<X509Certificate>,
                            authType: String
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> {
                            return arrayOf()
                        }
                    }
                )

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            return sslContext.socketFactory
        } catch (ex: java.lang.Exception) {
        }
        return null
    }
}

inline fun <reified T> getApi(url: String, ok: OkHttpClient? = null): T {
    return HttpHelp.getServiceRetrofit(url, ok).create(T::class.java)
}