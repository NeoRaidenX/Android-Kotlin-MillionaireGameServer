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
import android.widget.Toast
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
import com.example.millionairegameserver.Actions
import com.example.millionairegameserver.App
import com.example.millionairegameserver.R
import com.example.millionairegameserver.databinding.FragmentRewardBinding
import com.example.millionairegameserver.ui.viewmodel.RewardUiState
import com.example.millionairegameserver.ui.viewmodel.RewardViewModel
import kotlinx.coroutines.launch

@UnstableApi class RewardFragment : Fragment() {

    companion object {
        fun newInstance() = RewardFragment()
        private const val TAG = "RewardFragment"
    }

    private lateinit var viewModel: RewardViewModel
    private lateinit var binding: FragmentRewardBinding
    private lateinit var playerview: PlayerView
    private lateinit var exoPlayer: ExoPlayer

    private val actionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                Actions.NAVIGATE_UP -> {
                    findNavController().navigateUp()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProvider(this).get(RewardViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRewardBinding.inflate(inflater, container, false)

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
        exoPlayer.play()

        exoPlayer.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                Log.d(TAG, "onPlaybackStateChanged: $playbackState")
            }
        })

        lifecycleScope.launch {
            viewModel.getCurrentReward()
            viewModel.uiState.collect { currentReward ->
                Log.d(TAG, "onCreateView: ")
                when (currentReward) {
                    is RewardUiState.Error -> showError(currentReward.e)
                    is RewardUiState.Success -> showReward(currentReward.reward)
                }

            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        App.applicationContext().registerReceiver(actionReceiver, getLoginIntentFilter())
    }

    private fun getLoginIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Actions.NAVIGATE_UP)
        return intentFilter
    }

    private fun navigateUp() {
        Log.d(TAG, "navigateUp: ")
        findNavController().navigateUp()
    }

    private fun showReward(reward: String) {
        binding.rewardTitle.text = reward
    }

    private fun showError(e: Throwable) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
        App.applicationContext().unregisterReceiver(actionReceiver)
        exoPlayer?.stop()
        exoPlayer.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
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