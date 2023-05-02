package com.laughing.lib.player

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.laughing.lib.base.databinding.ActivityViedeoPlayBinding
import com.laughing.lib.utils.Router
import com.laughing.lib.utils.arguments


@Route(path = Router.PAGE_PLAYER_FRAGMENT)
class HVideoPlayFragment : Fragment() {
    var player: ExoPlayer? = null
    private lateinit var binding: ActivityViedeoPlayBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityViedeoPlayBinding.inflate(layoutInflater)
        initCompleted()
        return binding.root
    }

    private val listener: Player.Listener = object : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            // 视频播放状态
            when (playbackState) {
                Player.STATE_IDLE -> {}
                Player.STATE_BUFFERING -> {}
                Player.STATE_READY -> {}
                Player.STATE_ENDED -> {

                }
                else -> {}
            }
        }

        fun onPlayerError(error: ExoPlaybackException) {
            // 报错
            when (error.type) {
                ExoPlaybackException.TYPE_SOURCE -> {}
                ExoPlaybackException.TYPE_RENDERER -> {}
                ExoPlaybackException.TYPE_UNEXPECTED -> {}
            }
        }
    }


    private fun playVideo() {
        player = ExoPlayer.Builder(requireActivity()).build()
        player?.addListener(listener)
        player?.playWhenReady = true
        binding.videoView.player = player
//        val url = "file:///android_asset/" + "video.mp4"
        val url = arguments<String>("url").value

        // 播放
        val mediaItem: MediaItem = MediaItem.fromUri(Uri.parse(url))
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.let {
            player?.pause()
            player?.release()
        }
    }

    fun initCompleted() {
        playVideo()
    }
}