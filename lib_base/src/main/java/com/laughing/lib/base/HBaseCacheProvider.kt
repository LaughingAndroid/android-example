package com.laughing.lib.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.laughing.lib.utils.runInMain
import com.google.gson.Gson
import com.laughing.lib.utils.Logs
import com.laughing.lib.utils.SPUtil
import io.reactivex.Observable

abstract class HBaseCacheProvider<T> : ICacheProvider<T> {
    val tag = "data_cache_${javaClass.simpleName}"

    companion object {
        val gson = Gson()
    }

    var cacheData: MutableLiveData<T> = MutableLiveData()

    override fun getLiveData(): LiveData<T?> {
            getData(false).subscribe({}, {
                Logs.d("PlanConfigProvider getLiveData Error : $it")
            })
        return cacheData
    }

    var isLoading = false

    override fun getData(useCache: Boolean): Observable<T?> {
        return if (cacheData.value != null && useCache) {
            Logs.d("$tag getData from memory cache $cacheData")
            Observable.just(cacheData.value)
        } else {
            if(isLoading) {
                return Observable.create { e ->
                    var ob: Observer<T>? = null
                    ob = Observer() {
                        e.onNext(it)
                        ob?.let { it1 -> cacheData.removeObserver(it1) }
                    }
                    runInMain { cacheData.observeForever(ob) }
                }
            }
            isLoading = true
            val fromServer: Observable<T?> = loadDataFromServer().map { dataT ->
                Logs.d("$tag getData from server $dataT")
                save(dataT)
                isLoading = false
                dataT
                //处理网络异常问题,网络异常时除去第一个请求,其他接口走isLoading为true的流程,而cacheData用远得不到更新,又无法正常走onerror流程
            }.onErrorReturn {
                isLoading = false
                cacheData.postValue(null)
                null
            }
            fromServer
        }
    }

    open fun save(data: T?) {
        this.cacheData.postValue(data)
    }

    open fun clear() {
        SPUtil.remove(key())
        this.cacheData.postValue(null)
    }

    override fun key(): String = javaClass.name
}

interface ICacheProvider<T> {
    fun loadDataFromServer(): Observable<T?>
    fun getData(useCache: Boolean = true): Observable<T?>
    fun getLiveData(): LiveData<T?>
    fun key(): String
}