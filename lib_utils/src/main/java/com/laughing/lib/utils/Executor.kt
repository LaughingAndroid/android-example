package com.laughing.lib.utils

import android.annotation.SuppressLint
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.laughing.lib.utils.Executor.dispatchers
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.rx2.asCoroutineDispatcher
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

val isMainThread: Boolean get() = Looper.myLooper() == Looper.getMainLooper()

data class AppRxSchedulers(
        val db: Scheduler,
        val bg: Scheduler,
        val io: Scheduler,
        val main: Scheduler)

data class AppCoroutineDispatchers(
        val db: CoroutineDispatcher,
        val bg: CoroutineDispatcher,
        val io: CoroutineDispatcher,
        val main: CoroutineDispatcher
)

object Executor {
    private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()
    val ioExecutor = ThreadPoolExecutor(
        NUMBER_OF_CORES, NUMBER_OF_CORES * 2, 60, TimeUnit.SECONDS,
            LinkedBlockingQueue())
    val schedulers = AppRxSchedulers(
            db = Schedulers.single(),
            bg = Schedulers.computation(),
            io = Schedulers.from(ioExecutor),
            main = AndroidSchedulers.mainThread()
    )

    val dispatchers = AppCoroutineDispatchers(
            db = schedulers.db.asCoroutineDispatcher(),
            bg = schedulers.bg.asCoroutineDispatcher(),
            io = schedulers.io.asCoroutineDispatcher(),
            main = schedulers.main.asCoroutineDispatcher()
    )

    @SuppressLint("CheckResult")
    fun <T> runInBg(block: () -> T,
                    scheduler: Scheduler = schedulers.bg,
                    success: (T) -> Unit = {},
                    error: () -> Unit = {}) {
        Single.fromCallable { block() }
                .subscribeOn(scheduler)
                .observeOn(schedulers.main)
                .subscribe({ resp ->
                    success(resp)
                }, {
                    error()
                })
    }

    fun <T> runInBg(block: () -> T) {
        runInBg(block, schedulers.bg)
    }
}

private var loadjob = SupervisorJob()

private val uiScope = CoroutineScope(
        dispatchers.main
                + loadjob
                + CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
        })

fun <T> runInDispatcher(task: suspend () -> T,
                        dispatcher: CoroutineDispatcher = dispatchers.bg,
                        success: (T) -> Unit = {},
                        error: () -> Unit = {}): Job {
    return uiScope.launch {
        try {
            success(withContext(dispatcher) {
                task()
            })
        } catch (e: Exception) {
            error()
        } finally {
            //do nothing for now
        }

    }
}

fun <T> runInBg(task: suspend () -> T): Job {
    return runInDispatcher(task, dispatchers.bg, {}, {})
}

fun <T> runInMain(task: suspend () -> T): Job {
    return runInDispatcher(task, dispatchers.main, {}, {})
}

fun <T> checkFilter(filter: () -> Boolean, task: suspend () -> T, timeout: Long = 30 * 1000, timeoutCallback: suspend () -> Unit = {}): Job {
    return runInDispatcher({
        withTimeout(timeout) {
            while (true) {
                delay(100)
                if(filter()) {
                    task()
                    return@withTimeout
                }
            }
        }
    }, dispatchers.main, {}, {})
}


fun delay(time: Long, task: () -> Unit): Job {
    val isMainThread = Looper.getMainLooper().thread == Thread.currentThread()
    return runInBg {
        delay(time)
        if (isMainThread) {
            runInMain { task() }
        } else {
            task()
        }
    }
}

fun Job.bindLifeCircle(owner: LifecycleOwner): Job {
    owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            this@bindLifeCircle.cancel()
        }
    })
    return this
}

fun Job.bindLifeCycleOnStop(owner: LifecycleOwner): Job {
    owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            this@bindLifeCycleOnStop.cancel()
        }
    })
    return this
}

fun loop(delay: Long = 3500L, count: Int = Int.MAX_VALUE, task: () -> Unit) {
    runInMain {
        var c = 0
        while (true) {
            c++
            delay(delay)
            task()
            if (c >= count) {
                break
            }
        }
    }
}