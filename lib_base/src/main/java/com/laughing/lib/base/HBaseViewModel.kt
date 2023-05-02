package com.laughing.lib.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cf.holder.BaseHolder
import com.cf.holder.list.DataLoader
import com.cf.holder.list.ListDataImpl
import com.cf.holder.list.PageData
import com.laughing.lib.net.ServerException
import com.laughing.lib.utils.Executor.dispatchers
import com.laughing.lib.utils.Executor.schedulers
import com.laughing.lib.utils.Logs
import com.laughing.lib.utils.as2
import com.laughing.lib.utils.showToast
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*


open class HBaseViewModel : ViewModel() {
    private val viewModelJob = SupervisorJob()

    private var disposable: Disposable? = null

    protected val uiScope = CoroutineScope(
        dispatchers.main
                + viewModelJob
                + CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
            Logs.e("caught original $exception")
        })

    fun <T> load(loader: suspend () -> T): Deferred<T> {
        return uiScope.async(dispatchers.io) {
            loader()
        }
    }


    fun <T> Single<T>.result(
        success: (T) -> Unit,
        error: (Throwable) -> Unit = { },
        escapeHideSpinner: Boolean = false
    ) {
        disposable = subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .subscribe({

                success.invoke(it)
            }, {

            })
    }

    fun <T> Deferred<T>.result(showSpinner: Boolean = false): Observable<T> {
        return Observable.create<T> { emitter ->
            result({
                emitter.onNext(it)
                emitter.onComplete()
            }, {
                emitter.onError(it)
            }, showSpinner = showSpinner)
        }
    }

    fun <T> Deferred<T>.result(
        success: (T) -> Unit,
        error: (ServerException) -> Unit = { },
        complete: () -> Unit = {},
        showSpinner: Boolean = true,
        showErrorToast: Boolean = true
    ) {
        if (showSpinner) {
            showLoading()
        }
        val start = System.currentTimeMillis()
        if (isActive) uiScope.launch {
            try {
                success(this@result.await().apply {
                    Logs.d("duration:${System.currentTimeMillis() - start} $this")
                    dismissLoading()
                })
            } catch (err: ServerException) {
                dismissLoading()

                when (err.code) {
                    407, 401 -> {
                        // todo 登录过期
//                        showToast("登录过期")
//                        getService<CommonService>()?.logout()
                    }
                    else -> {
                        if (showErrorToast) {
                            showToast(err.message)
                        }
                    }
                }
                error(err)
            } finally {
                complete.invoke()
            }
        }
    }


    // old

    val mShowLoading: MutableLiveData<Boolean> = MutableLiveData() //显示/隐藏loading框


    fun showLoading() {
        mShowLoading.value = true
    }

    fun dismissLoading() {
        mShowLoading.value = false
    }

    fun dispose() {
        uiScope.coroutineContext.cancelChildren()
        disposable?.dispose()
    }

    override fun onCleared() {
        super.onCleared()
        dispose()
    }

}

abstract class HBaseListViewModel : HBaseViewModel(), DataLoader {
    val dataManager = ListDataImpl<Any>()
    val dataManagerLiveData: MutableLiveData<ListDataImpl<Any>> = MutableLiveData()

    override fun load(
        refresh: Boolean,
        result: (PageData) -> Unit,
        exception: (Exception) -> Unit
    ) {
        if (refresh) {
            dataManager.resetPage()
        }
        val page = dataManager.page
        loadData({
            val data = PageData(page, it)
            result(data)
            dataManager.putCache(data)
            dataManager.pageAdd()
            dataManagerLiveData.postValue(dataManager)
        }, exception)
    }

    abstract fun loadData(result: (List<*>) -> Unit, exception: (Exception) -> Unit)
}

interface ViewModelFactory<V : ViewModel> {
    fun createViewModel(): V
}

fun <VM : HBaseViewModel> getVm(activity: FragmentActivity, clz: Class<VM>): VM {
    return ViewModelProvider(activity).get(clz)
}

fun <VM : HBaseViewModel> getVm(fragment: Fragment, clz: Class<VM>): VM {
    return ViewModelProvider(fragment).get(clz)
}

inline fun <reified VM : HBaseViewModel> getVm(holder: BaseHolder<*>, clz: Class<VM>): VM? {
    return holder.adapterContext?.target().as2<ViewModelFactory<*>>()?.createViewModel() as VM?
}

inline fun <reified VM : HBaseViewModel> FragmentActivity.getVm() = getVm(this, VM::class.java)

inline fun <reified VM : HBaseViewModel> Fragment.getActivityVm() = requireActivity().getVm<VM>()

inline fun <reified VM : HBaseViewModel> Fragment.getVm() = getVm(this, VM::class.java)

inline fun <reified VM : HBaseViewModel> BaseHolder<*>.getVm() = getVm(this, VM::class.java)


