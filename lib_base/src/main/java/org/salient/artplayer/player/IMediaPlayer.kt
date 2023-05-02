package org.salient.artplayer.player

import android.view.Surface
import android.view.SurfaceHolder
import androidx.lifecycle.LiveData
import com.jeremyliao.liveeventbus.core.Observable
import org.salient.artplayer.bean.VideoInfo
import org.salient.artplayer.bean.VideoSize
import org.salient.artplayer.conduction.PlayerState

/**
 * description: 视频播放器的抽象基类
 *
 * @author Maiwenchang
 * email: cv.stronger@gmail.com
 * date: 2020-05-04 10:06 AM.
 */
interface IMediaPlayer<T> {

    /**
     * 播放器实例
     */
    var impl: T
    /**
     * 准备后是否播放
     */
    var playWhenReady: Boolean
    /**
     * 是否正在播放
     */
    val isPlaying: Boolean
    /**
     * 当前位置
     */
    val currentPosition: Long
    /**
     * 视频长度
     */
    val duration: Long
    /**
     * 视频高度
     */
    val videoHeight: Int
    /**
     * 视频宽度
     */
    val videoWidth: Int
    /**
     * 当前播放器状态
     */
    val playerState: PlayerState
    /**
     * 播放器状态监听
     */
    val playerStateLD: Observable<PlayerState>
    /**
     * 播放器尺寸
     */
    val videoSizeLD: Observable<VideoSize>
    /**
     * 加载进度
     */
    val bufferingProgressLD: Observable<Int>
    /**
     * 是否跳转完成
     */
    val seekCompleteLD: Observable<Boolean>
    /**
     * 视频信息
     */
    val videoInfoLD: Observable<VideoInfo>
    /**
     * 视频报错
     */
    val videoErrorLD: Observable<VideoInfo>

    /**
     * 开始
     */
    fun start()

    /**
     * 准备
     */
    fun prepare()

    /**
     * 异步准备
     */
    fun prepareAsync()

    /**
     * 暂停
     */
    fun pause()

    /**
     * 停止
     */
    fun stop()

    /**
     * 跳转到指定到位置
     */
    fun seekTo(time: Long)

    /**
     * 重置
     */
    fun reset()

    /**
     * 释放
     */
    fun release()

    /**
     * 设置音量
     */
    fun setVolume(volume: Float)

    /**
     * 设置循环播放
     */
    fun setLooping(isLoop: Boolean)

    /**
     * 设置播放容器
     */
    fun setSurface(surface: Surface?)

    /**
     * 设置播放容器
     */
    fun setDisplay(surfaceHolder: SurfaceHolder)

}