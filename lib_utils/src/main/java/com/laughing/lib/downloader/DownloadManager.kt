package com.laughing.lib.downloader

import android.content.Context
import com.laughing.lib.utils.Logs.d
import com.liulishuo.okdownload.OkDownload
import com.liulishuo.okdownload.core.dispatcher.CallbackDispatcher
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher
import com.liulishuo.okdownload.core.file.DownloadUriOutputStream
import com.liulishuo.okdownload.core.download.DownloadStrategy
import com.liulishuo.okdownload.core.file.ProcessFileStrategy
import com.liulishuo.okdownload.DownloadMonitor
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.Util
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.cause.EndCause
import com.laughing.lib.utils.application
import java.lang.Exception
import com.liulishuo.okdownload.core.listener.DownloadListener3
import com.laughing.lib.utils.FilePathManager
import com.laughing.lib.utils.encryptMD5
import io.reactivex.Observable
import java.io.File

class DownloadManager {
    fun initOkDownload(context: Context = application) {
        val builder = OkDownload.Builder(context)
            .downloadStore(Util.createDefaultDatabase(context)) // //断点信息存储的位置，默认是SQLite数据库
            .callbackDispatcher(object : CallbackDispatcher() {})
            .downloadDispatcher(object : DownloadDispatcher() {})
            .connectionFactory(Util.createDefaultConnectionFactory()) //选择网络请求框架，默认是OkHttp
            .outputStreamFactory(DownloadUriOutputStream.Factory()) // //构建文件输出流DownloadOutputStream，是否支持随机位置写入
            .downloadStrategy(DownloadStrategy()) //下载策略，文件分为几个线程下载
            .processFileStrategy(ProcessFileStrategy()) //多文件写文件的方式，默认是根据每个线程写文件的不同位置，支持同时写入
            .monitor(object : DownloadMonitor {
                //开始任务
                override fun taskStart(task: DownloadTask) {
                    d("taskStart")
                }

                //断点下载任务
                override fun taskDownloadFromBreakpoint(task: DownloadTask, info: BreakpointInfo) {
                    d("断点任务下载taskDownloadFromBreakpoint:" + task.filename)
                }

                //新任务
                override fun taskDownloadFromBeginning(
                    task: DownloadTask, info: BreakpointInfo,
                    cause: ResumeFailedCause?
                ) {
                    d("新建任务下载taskDownloadFromBeginning:" + task.filename)
                }

                //任务结束
                override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?) {
                    d("下载完成taskEnd")
                    // 此处可以实现自己的逻辑,比如修改下载记录的任务状态,对下载文件进行加密
                }
            })
        OkDownload.setSingletonInstance(builder.build())
    }
}


/**
 *
 * @ClassName: DownloadsD
 * @Description:
 * @Author: Laughing
 * @CreateDate: 2019/8/28 11:14
 * @Version: 1.7.0
 */
fun download(
    url: String,
    name: String? = null,
    downloadDir: String = "download",
    progressCallback: ((Float) -> Unit)? = null
): Observable<String> {
    val dir = FilePathManager.getDirByName(downloadDir)
    val fileName = name ?: url.encryptMD5()
    val path = "$dir/$fileName"
    if (File(path).exists()) {
        return Observable.just(path)
    }
    val task = DownloadTask.Builder(url, File(dir))
        .setConnectionCount(1)
        .setFilename(fileName)
        .setMinIntervalMillisCallbackProcess(16)
        .setPassIfAlreadyCompleted(true)
        .build()
    return Observable.create {
        task.enqueue(object : DownloadListener3() {
            var start = System.currentTimeMillis()
            override fun completed(task: DownloadTask) {
                it.onNext(path)
            }

            override fun canceled(task: DownloadTask) {
                it.onError(Exception("cancel"))
            }

            override fun error(task: DownloadTask, e: java.lang.Exception) {
                it.onError(e)
            }

            override fun warn(task: DownloadTask) {
            }


            override fun retry(task: DownloadTask, cause: ResumeFailedCause) {
            }

            override fun connected(
                task: DownloadTask,
                blockCount: Int,
                currentOffset: Long,
                totalLength: Long
            ) {
            }

            override fun progress(task: DownloadTask, currentOffset: Long, totalLength: Long) {
                if (System.currentTimeMillis() - start > 300) {
                    start = System.currentTimeMillis()
                    progressCallback?.invoke(currentOffset.toFloat() / totalLength)
                }
            }

            override fun started(task: DownloadTask) {
            }
        })
    }
}

fun getDownloadFile(
    url: String,
    name: String? = null,
    downloadDir: String = FilePathManager.getDirByName("download")
): File {
    val fileName = name ?: url.encryptMD5()
    return File("$downloadDir/$fileName")
}



