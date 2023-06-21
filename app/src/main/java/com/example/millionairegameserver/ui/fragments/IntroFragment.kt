package com.example.millionairegameserver.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import androidx.navigation.fragment.findNavController
import com.example.millionairegameserver.Actions
import com.example.millionairegameserver.App
import com.example.millionairegameserver.R
import com.example.millionairegameserver.databinding.FragmentIntroBinding
import com.example.millionairegameserver.databinding.FragmentPersonBinding
import kotlin.system.measureNanoTime


@UnstableApi class IntroFragment : Fragment() {

    companion object {
        fun newInstance() = IntroFragment()
        private const val TAG = "IntroFragment"
    }

    private lateinit var binding: FragmentIntroBinding
    private lateinit var playerview: PlayerView
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaPlayer: MediaPlayer

    private val actionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive: ${intent.action}")
            when(intent.action) {
                Actions.NAVIGATE_NEXT -> {
                    navigateNext()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentIntroBinding.inflate(inflater, container, false)
        initPlayer()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exoPlayer.play()
        mediaPlayer.start()
        registerReceiver()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.stop()
        exoPlayer.release()
        mediaPlayer.stop()
        mediaPlayer.release()
        unregisterReceiver()
    }

    private fun initPlayer() {
        playerview = binding.playerview

        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        playerview.player = exoPlayer
        val uri = RawResourceDataSource.buildRawResourceUri(R.raw.bg_loop)
        val mediaItem = MediaItem.fromUri(uri)
        val mediaSource = ProgressiveMediaSource.Factory(DefaultDataSource.Factory(requireContext()))
            .createMediaSource(mediaItem)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        exoPlayer.prepare()

        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.intro)
        mediaPlayer.isLooping = false


    }

    private fun navigateNext() {
        val action = IntroFragmentDirections.actionIntroFragmentToQuestionFragment()
        findNavController().navigate(action)
    }

    private fun getIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Actions.NAVIGATE_NEXT)
        return intentFilter
    }
    private fun registerReceiver() {
        Log.d(TAG, "registerReceiver: ")
        App.applicationContext().registerReceiver(actionReceiver, getIntentFilter())
    }

    private fun unregisterReceiver() {
        Log.d(TAG, "unregisterReceiver: ")
        App.applicationContext().unregisterReceiver(actionReceiver)
    }


}