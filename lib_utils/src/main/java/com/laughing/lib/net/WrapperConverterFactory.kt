package com.laughing.lib.net

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonToken
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


class WrapperConverterFactory(val gson: Gson) : Converter.Factory() {

    private val gsonConverterFactory: GsonConverterFactory = GsonConverterFactory.create(gson)

    override fun responseBodyConverter(type: Type,
                                       annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *>? {

        annotations.forEach {
            if (it.annotationClass == NoWrapJson::class){
                return GsonResponseBodyConverter(gson, gson.getAdapter(TypeToken.get(type)))
            }

            if (it.annotationClass == WrapJson::class){
                return getWrapperResponseBodyConverter(type, annotations, retrofit)
            }
        }
        return getWrapperResponseBodyConverter(type, annotations, retrofit)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getWrapperResponseBodyConverter(type: Type,
                                                annotations: Array<Annotation>,
                                                retrofit: Retrofit): WrapperResponseBodyConverter<*> {
        val wrappedType = object : ParameterizedType {
            override fun getActualTypeArguments(): Array<Type> = arrayOf(type)
            override fun getOwnerType(): Type? = null
            override fun getRawType(): Type = BaseResult::class.java
        }
        val gsonConverter: Converter<ResponseBody, *>? = gsonConverterFactory.responseBodyConverter(wrappedType, annotations, retrofit)
        return WrapperResponseBodyConverter(gsonConverter as Converter<ResponseBody, BaseResult<Any>>)
    }

    override fun requestBodyConverter(type: Type?, parameterAnnotations: Array<Annotation>,
                                      methodAnnotations: Array<Annotation>, retrofit: Retrofit): Converter<*, RequestBody>? {
        return gsonConverterFactory.requestBodyConverter(type!!, parameterAnnotations, methodAnnotations, retrofit)
    }
}

class WrapperResponseBodyConverter<T>(private val converter: Converter<ResponseBody, BaseResult<T>>)
    : Converter<ResponseBody, T> {

    @Throws(IOException::class)
    override fun convert(responseBody: ResponseBody): T? {
        val response = converter.convert(responseBody)
        return if (response?.isOk() == true) response.data
        else throw ServerException(response!!.code, response.msg?:"")
    }
}

class GsonResponseBodyConverter<T>(private val gson: Gson,
                                   private val adapter: TypeAdapter<T>)
    : Converter<ResponseBody, T> {

    @Throws(IOException::class)
    override fun convert(value: ResponseBody): T {
        val jsonReader = gson.newJsonReader(value.charStream())
        value.use {
            val result = adapter.read(jsonReader)
            if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw JsonIOException("JSON document was not fully consumed.")
            }
            return result
        }
    }
}