package com.laughing.lib.player

import android.os.Build
import android.view.Surface
import android.view.SurfaceHolder
import com.jeremyliao.liveeventbus.LiveEventBus
import com.jeremyliao.liveeventbus.core.Observable
import com.laughing.lib.utils.Logs
import com.laughing.lib.utils.application
import com.laughing.lib.utils.runInBg
import com.laughing.lib.utils.runInMain
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.salient.artplayer.bean.VideoInfo
import org.salient.artplayer.bean.VideoSize
import org.salient.artplayer.conduction.PlayerState
import org.salient.artplayer.player.IMediaPlayer
import org.salient.artplayer.player.SystemMediaPlayer


object AudioPlayerManager : IMediaPlayer<SystemMediaPlayer> {
    override var impl: SystemMediaPlayer = SystemMediaPlayer()
    override var playWhenReady: Boolean = impl.playWhenReady

    override val isPlaying: Boolean
        get() = impl.isPlaying
    override val currentPosition: Long
        get() = impl.currentPosition
    override val duration: Long
        get() = impl.duration
    override val videoHeight: Int
        get() = impl.videoHeight
    override val videoWidth: Int
        get() = impl.videoWidth
    override val playerState: PlayerState
        get() = impl.playerState
    override val playerStateLD: Observable<PlayerState> = impl.playerStateLD
    override val videoSizeLD: Observable<VideoSize> = impl.videoSizeLD
    override val bufferingProgressLD: Observable<Int> = impl.bufferingProgressLD
    override val seekCompleteLD: Observable<Boolean> = impl.seekCompleteLD
    override val videoInfoLD: Observable<VideoInfo> = impl.videoInfoLD
    override val videoErrorLD: Observable<VideoInfo> = impl.videoErrorLD

    val progressLD = LiveEventBus.get<Long>("AudioPlayerManager.progress")

    override fun start() {
        impl.start()
    }

    override fun prepare() {
        impl.prepare()
    }

    override fun prepareAsync() {
        impl.prepareAsync()
    }

    override fun pause() {
        impl.pause()
    }

    override fun stop() {
        impl.stop()
    }

    override fun seekTo(time: Long) {
        impl.seekTo(time)
    }

    override fun reset() {
        impl.reset()
    }

    override fun release() {
        impl.release()
        loopListenerJob?.cancel()
        loopListenerJob = null

    }

    override fun setVolume(volume: Float) {
        impl.setVolume(volume)
    }

    override fun setLooping(isLoop: Boolean) {
        impl.setLooping(isLoop)
    }

    override fun setSurface(surface: Surface?) {
        impl.setSurface(surface)
    }

    override fun setDisplay(surfaceHolder: SurfaceHolder) {
        impl.setDisplay(surfaceHolder)
    }

    /////////////////////////////

    var playList = PlayList()

    init {
        runInMain {
            try {
                playerStateLD.observeForever {
                    Logs.d("init observeForever state $it")

                    when (it) {
                        PlayerState.PREPARED -> {
                            start()
                        }
                        PlayerState.STARTED -> {
                            loopListener()
                        }

                        PlayerState.COMPLETED -> {
                            next()
                        }

                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Logs.d("init observeForever state  error -》 $e")
                e.printStackTrace()
            }
        }
    }

    fun play() {
        play(playList.current())
    }

    fun pre() {
        Logs.d("pre")
        play(playList.pre())
    }

    fun next() {
        Logs.d("next")
        play(playList.next())
    }

    fun play(url: String) {
        play(MediaData(url))
    }

    fun play(mediaData: MediaData?) {
        mediaData ?: return
        playList.currentIndex = playList.add(mediaData)
        currentPositionCache.clear()
        runInBg {
            try {
                val mediaPlayer = impl
                setDataSouce(mediaData)
                Logs.d("prepare 1 index = ${playList.currentIndex} ${mediaData.url}")
                mediaPlayer.prepare()
                Logs.d("prepare 2")
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                reset()
                play(mediaData)
                Logs.d("SystemMediaPlayer error1:$e")
            } catch (e: Exception) {
                e.printStackTrace()
                Logs.d("SystemMediaPlayer error2:$e")
            }
        }
    }

    fun setDataSouce(mediaData: MediaData) {
        when (mediaData.sourceType) {
            SourceType.OSS -> {
                impl.setDataSource(mediaData.url)
            }
            SourceType.ASSETS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val fd = application.assets.openFd(mediaData.url)
                    impl.setDataSource(fd)
                }
            }
            SourceType.RAW -> TODO()
        }

    }


    var loopListenerJob: Job? = null
    var currentPositionCache = mutableListOf<Long>()
    fun loopListener() {
        if (loopListenerJob == null) {
            loopListenerJob = runInMain {
                try {
                    while (true) {
                        delay(1000)
                        currentPositionCache.add(currentPosition)
                        if (isPlaying) {
                            Logs.d("progressLD ->> $currentPosition $duration")
                            progressLD.post(currentPosition)
                        } else {
                            Logs.d("progressLD ->> $playerState $currentPosition $duration")
                            checkTimeOut()
                        }
                    }
                } catch (e: Exception) {
                    Logs.e("loopListener $e")
                }
            }
        }
    }

    private fun checkTimeOut() {
        if (playerState == PlayerState.STARTED
            && currentPositionCache.filter { it == currentPosition }.size > 5
            && currentPosition > 0L
        ) {
            Logs.d("checkTimeOut true")
            impl.onCompletion(impl.impl)
        }
    }

}