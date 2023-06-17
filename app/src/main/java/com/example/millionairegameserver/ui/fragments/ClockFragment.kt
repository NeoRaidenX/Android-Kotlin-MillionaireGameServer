package com.example.millionairegameserver.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.navigateUp
import com.example.millionairegameserver.Actions
import com.example.millionairegameserver.App
import com.example.millionairegameserver.ui.viewmodel.ClockViewModel
import com.example.millionairegameserver.R
import com.example.millionairegameserver.databinding.FragmentClockBinding
import com.example.millionairegameserver.ui.viewmodel.ChartViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@UnstableApi class ClockFragment : Fragment() {

    companion object {
        fun newInstance() = ClockFragment()
        private const val TAG = "ClockFragment"
    }

    private lateinit var viewModel: ClockViewModel
    private lateinit var binding: FragmentClockBinding
    private lateinit var playerview: PlayerView
    private lateinit var exoPlayer: ExoPlayer

    private val actionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                Actions.NAVIGATE_UP -> {
                    findNavController().navigateUp()
                }
                Actions.PLAY_CLOCK -> {
                    exoPlayer.play()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProvider(this).get(ClockViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentClockBinding.inflate(inflater, container, false)
        initPlayer()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerReceiver()
        lifecycleScope.launch {
            delay(3000)
            viewModel.startClock()
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver()
    }

    private fun initPlayer() {
        playerview = binding.playerview

        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        playerview.player = exoPlayer

        val uri = RawResourceDataSource.buildRawResourceUri(R.raw.clock)
        val mediaItem = MediaItem.fromUri(uri)
        val mediaSource = ProgressiveMediaSource.Factory(DefaultDataSource.Factory(requireContext()))
            .createMediaSource(mediaItem)
        exoPlayer.setMediaItem(mediaItem)
        //exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        exoPlayer.prepare()
        //exoPlayer.play()

        exoPlayer.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                Log.d(TAG, "onPlaybackStateChanged: $playbackState")
                if (playbackState == Player.STATE_ENDED) {
                    navigateUp()
                }
            }
        })
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }


    private fun getLoginIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Actions.NAVIGATE_UP)
        intentFilter.addAction(Actions.PLAY_CLOCK)
        return intentFilter
    }
    private fun registerReceiver() {
        Log.d(TAG, "registerReceiver: ")
        App.applicationContext().registerReceiver(actionReceiver, getLoginIntentFilter())
    }

    private fun unregisterReceiver() {
        Log.d(TAG, "unregisterReceiver: ")
        App.applicationContext().unregisterReceiver(actionReceiver)
    }

}